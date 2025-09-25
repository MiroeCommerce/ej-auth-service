package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private static final String AUTHORIZATION_NAMESPACE = "auth:authorization";
    // A metadata attribute name for the old refresh token, used during rotation
    private static final String OLD_REFRESH_TOKEN_METADATA_NAME = "org.springframework.security.oauth2.server.authorization.OAuth2Authorization.metadata.previous-refresh-token";

    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    /**
     * Saves or updates an {@link OAuth2Authorization} object in Redis.
     * <p>
     * This implementation uses a primary and secondary index pattern for efficient lookups.
     * The full, serialized {@code OAuth2Authorization} object is stored against a primary key
     * derived from its unique ID (e.g., {@code "auth:authorization:id:[ID]"}).
     * <p>
     * To allow finding the authorization by its associated tokens, secondary index keys are also created.
     * Keys for the authorization code, access token, and refresh token are mapped to the authorization's
     * unique ID. This avoids data duplication and saves space. All created keys are set with a
     * Time-To-Live (TTL) to ensure they expire automatically.
     *
     * @param authorization the {@link OAuth2Authorization} object to be saved. Must not be null.
     * @throws IllegalArgumentException if the provided authorization is null.
     * @throws RuntimeException         if an error occurs during JSON serialization.
     */
    @Override
    public void save(OAuth2Authorization authorization) {
        // Ensure the authorization object is not null before processing.
        Assert.notNull(authorization, "authorization cannot be null");

        // Check if this is a refresh token rotation
        OAuth2RefreshToken oldRefreshToken = authorization.getAttribute(OLD_REFRESH_TOKEN_METADATA_NAME);
        if (oldRefreshToken != null) {
            // If so, mark the old token as revoked for a brief period to detect reuse
            String revokedKey = buildKey(KeyType.REVOKED, oldRefreshToken.getTokenValue());
            this.cacheService.createWithTTL(revokedKey, "true", 2, TimeUnit.DAYS);
        }

        // A list to hold all Redis keys associated with this authorization.
        List<String> keys = new ArrayList<>();

        // 1. Create the primary key using the authorization's unique ID.
        // This key will map directly to the full serialized authorization object.
        String idKey = buildKey(KeyType.ID, authorization.getId()); // auth:authorization:id:[authorization.getId()]
        keys.add(idKey);

        // 2. Check for an authorization code and create a secondary index key for it.
        // This allows finding the authorization by its authorization code.
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authCodeToken = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authCodeToken != null) {
            String authCode = authCodeToken.getToken().getTokenValue();
            keys.add(buildKey(KeyType.AUTH_CODE, authCode)); // auth:authorization:auth_code:[authCode]
        }

        // 3. Check for an access token and create a secondary index key for it.
        // This allows finding the authorization by its access token.
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null) {
            String accessTokenValue = accessToken.getToken().getTokenValue();
            keys.add(buildKey(KeyType.ACCESS_TOKEN, accessTokenValue)); // auth:authorization:access_token:[accessTokenValue]
        }

        // 4. Check for a refresh token and create a secondary index key for it.
        // This allows finding the authorization by its refresh token.
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if (refreshToken != null) {
            String refreshTokenValue = refreshToken.getToken().getTokenValue();
            // auth:authorization:refresh_token:[refreshTokenValue]
            keys.add(buildKey(KeyType.REFRESH_TOKEN, refreshTokenValue));
        }

        try {
            // Serialize the entire OAuth2Authorization object into a JSON string for storage.
            String serializedAuth = this.objectMapper.writeValueAsString(authorization);
            // Store the main entry in Redis: the ID key maps to the full serialized object.
            this.cacheService.createWithTTL(idKey, serializedAuth, 1, TimeUnit.HOURS);
            // Store the secondary index entries. Each token-based key is mapped to the authorization's unique ID.
            keys
                    .stream()
                    .filter(key -> !key.equals(idKey)) // Exclude the primary key itself
                    .forEach(key -> this.cacheService.createWithTTL(key, authorization.getId(), 1, TimeUnit.HOURS));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing OAuth2Authorization", e);
        }
    }

    /**
     * Removes an {@link OAuth2Authorization} and all its associated index entries from Redis.
     * <p>
     * This method ensures a complete cleanup by first gathering all keys related to the
     * given authorization object. This includes the primary key (based on the authorization's ID)
     * and all secondary index keys (for authorization code, access token, and refresh token).
     * <p>
     * Once all keys are collected, it iterates through the list and deletes each one from Redis,
     * preventing any orphaned index entries from being left in the database.
     *
     * @param authorization the {@link OAuth2Authorization} object to be removed. Must not be null.
     * @throws IllegalArgumentException if the provided authorization is null.
     */
    @Override
    public void remove(OAuth2Authorization authorization) {
        // Ensures the authorization object is not null before processing.
        Assert.notNull(authorization, "authorization cannot be null");

        // Create a list to hold all Redis keys that need to be deleted.
        List<String> keysToDelete = new ArrayList<>();

        // 1. Add the primary key (based on the unique ID) to the deletion list.
        keysToDelete.add(buildKey(KeyType.ID, authorization.getId()));

        // 2. If an authorization code exists, add its secondary index key to the list.
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authCodeToken = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authCodeToken != null) {
            String authCode = authCodeToken.getToken().getTokenValue();
            keysToDelete.add(buildKey(KeyType.AUTH_CODE, authCode));
        }

        // 3. If an access token exists, add its secondary index key to the list.
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getToken(OAuth2AccessToken.class);
        if (accessToken != null) {
            String accessTokenValue = accessToken.getToken().getTokenValue();
            keysToDelete.add(buildKey(KeyType.ACCESS_TOKEN, accessTokenValue));
        }

        // 4. If a refresh token exists, add its secondary index to the list.
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getToken(OAuth2RefreshToken.class);
        if (refreshToken != null) {
            String refreshTokenValue = refreshToken.getToken().getTokenValue();
            keysToDelete.add(buildKey(KeyType.REFRESH_TOKEN, refreshTokenValue));
        }

        // 5. Iterate over the collected list of keys and delete each one from Redis.
        // This performs the actual cleanup of the authorization and its indexes.
        for (String key : keysToDelete) {
            this.cacheService.delete(key);
        }
    }

    /**
     * Finds and returns an {@link OAuth2Authorization} by its unique identifier.
     * <p>
     * This method serves as the public entry point for retrieving an authorization using its primary key.
     * It validates the input and delegates the core logic to the private {@code findBy} helper method.
     *
     * @param id the unique identifier of the authorization. Must not be null or empty.
     * @return the {@link OAuth2Authorization} if found, otherwise {@code null}.
     * @throws IllegalArgumentException if the provided id is null or empty.
     */
    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return findBy(KeyType.ID, id);
    }

    /**
     * Finds an {@link OAuth2Authorization} by a given token value and type.
     * <p>
     * This method facilitates lookups using secondary indexes (access tokens, refresh tokens, etc.).
     * It includes a specific security check for refresh tokens: if a refresh token is found in the
     * cache, it's considered to have been revoked (likely due to token rotation), and an exception is thrown
     * to prevent its reuse.
     * <p>
     * After the optional security check, it maps the provided {@link OAuth2TokenType} to an internal
     * {@code KeyType} and delegates the actual lookup to the generic {@code findBy} helper method.
     *
     * @param token the string value of the token to search for. Must not be null or empty.
     * @param tokenType the type of the token (e.g., access_token, refresh_token). If null, it defaults
     * to refresh token behavior for the revocation check.
     * @return the {@link OAuth2Authorization} associated with the token, or {@code null} if not found.
     * @throws IllegalArgumentException if the token is null or empty.
     * @throws RuntimeException if a refresh token is found in the cache, indicating it's a revoked token.
     */
    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        // 1. Ensure the token string is not null of empty.
        Assert.hasText(token, "token cannot be empty");

        // 2. Performs a security check specifically for refresh tokens to detect reuse.
        // This logic assumes that if a refresh token key exists, the token has been rotated and the old one is now considered revoked.
        if (tokenType == null || Objects.equals(tokenType.getValue(), OAuth2TokenType.REFRESH_TOKEN.getValue())) {
            // Build the key that a refresh token would have.
            String revokedKey = buildKey(KeyType.REFRESH_TOKEN, token);
            // If this key exists in the cache, it means an attempt is being made to use a revoked token.
            if (this.cacheService.read(revokedKey) != null) {
                // Throw an exception to prevent a security breach.
                throw new RuntimeException("Attempt to use a revoked refresh token.");
            }
        }

        // 3. Map the public OAuth2TokenType to the internal KeyType enum used for building Redis keys.
        KeyType keyType = mapTokenTypeToKeyType(tokenType);

        // 4. Delegate the actual lookup to the generic findBy() helper method.
        return findBy(keyType, token);
    }

    /**
     * A generic helper method to find an {@link OAuth2Authorization} using various key types.
     * <p>
     * This method handles the logic for both primary and secondary index lookups.
     * <ul>
     * <li><b>Primary Lookup (by ID):</b> It directly fetches the serialized object from Redis.</li>
     * <li><b>Secondary Lookup (by token):</b> It performs a two-step fetch. First, it uses the token
     * to find the authorization's ID. Second, it uses that ID to fetch the full, serialized object.</li>
     * </ul>
     *
     * @param keyType the type of key to use for the lookup (e.g., ID, ACCESS_TOKEN).
     * @param value the value of the key to search for (e.g., the authorization ID or a token string).
     * @return the deserialized {@link OAuth2Authorization} object, or {@code null} if not found.
     * @throws RuntimeException if an error occurs during JSON deserialization.
     */
    private OAuth2Authorization findBy(KeyType keyType, String value) {
        // 1. Build the initial lookup key based on the specified type and value.
        String key = buildKey(keyType, value);

        // 2. Perform the first read from the cache.
        // If the keyType is a token, this will return the authorization ID (a String).
        // If the ketType is ID, this will return the full serialized object (a String).
        String authorizationId = (String) this.cacheService.read(key);

        // 3. Determine the final key that points to the full object.
        // If we searched by ID, the initial 'key' is the final key.
        // If we searched by a token, 'authorizationId' hold the ID we need to build the final key.
        String finalKey = keyType == KeyType.ID ? key : buildKey(KeyType.ID, authorizationId);

        // 4. Read the full object from the cache using the final key.
        // Note: If the initial lookup was by ID, this is a redundant read of the same key.
        String serializedAuth = (String) this.cacheService.read(finalKey);
        if (serializedAuth == null) {
            // The object does not exist in the cache.
            return null;
        }

        try {
            // 5. Deserialize the JSON string back into an OAuth2Authorization object.
            return this.objectMapper.readValue(serializedAuth, OAuth2Authorization.class);
        } catch (JsonProcessingException e) {
            // If deserialization fails, wrap the exception.
            throw new RuntimeException("Error deserializing OAuth2Authorization", e);
        }

    }

    /**
     * Constructs a standardized, formatted key for use in Redis. 🔑
     * <p>
     * This utility method ensures all keys follow a consistent namespacing convention,
     * which is crucial for organizing data and avoiding collisions in Redis. The format is
     * {@code "namespace:type:value"}.
     *
     * @param type the {@link KeyType} of the record (e.g., ID, ACCESS_TOKEN).
     * @param value the unique value for the key (e.g., the authorization ID or token string).
     * @return a formatted string to be used as a Redis key (e.g., "auth:authorization:access_token:xyz123").
     */
    private String buildKey(KeyType type, String value) {
        // Joins the static namespace, the lowercase name of the key type, and the dynamic value.
        return String.format("%s:%s:%s", AUTHORIZATION_NAMESPACE, type.name().toLowerCase(), value);
    }

    /**
     * Maps a public {@link OAuth2TokenType} from the Spring Security framework to the
     * internal {@code KeyType} enum used for building Redis keys.
     * <p>
     * This is a simple translation utility to decouple the internal storage logic
     * from the public framework APIs.
     *
     * @param tokenType the {@link OAuth2TokenType} to be mapped. Can be {@code null}.
     * @return the corresponding internal {@link KeyType}.
     */
    private KeyType mapTokenTypeToKeyType(OAuth2TokenType tokenType) {
        if (tokenType == null) {
            // Handle null input gracefully.
            return KeyType.UNKNOWN;
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            // Map access token type.
            return KeyType.ACCESS_TOKEN;
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            // Map refresh token type.
            return KeyType.REFRESH_TOKEN;
        } else {
            // Default any other token type (like AUTHORIZATION_CODE) to AUTH_CODE.
            return KeyType.AUTH_CODE;
        }
    }

    private enum KeyType {
        ID, AUTH_CODE, ACCESS_TOKEN, REFRESH_TOKEN, REVOKED, UNKNOWN
    }

}
