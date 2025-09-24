package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private static final String AUTHORIZATION_NAMESPACE = "auth:authorization:";
    // A metadata attribute name for the old refresh token, used during rotation ???
    // private static final String OLD_REFRESH_TOKEN_METADATA_NAME = "org.springframework.security.oauth2.server.authorization.OAuth2Authorization.metadata.previous-refresh-token";

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
     * @throws RuntimeException if an error occurs during JSON serialization.
     */
    @Override
    public void save(OAuth2Authorization authorization) {
        // Ensure the authorization object is not null before processing.
        Assert.notNull(authorization, "authorization cannot be null");

        /* // Check if this is a refresh token rotation
        OAuth2RefreshToken oldRefreshToken = authorization.getAttribute(OLD_REFRESH_TOKEN_METADATA_NAME);
        if (oldRefreshToken != null) {
            // If so, mark the old token as revoked for a brief period to detect reuse
            String revokedKey = buildKey(KeyType.REVOKED, oldRefreshToken.getTokenValue());
            this.cacheService.createWithTTL(revokedKey, "true", 2, TimeUnit.DAYS);
        } */

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
            throw new RuntimeException("Error serializing OAuth2Authorization",e);
        }

        /*
        # -----------------------------------------------------
        # PRIMARY INDEX: The ID key points to the full object
        # -----------------------------------------------------

        KEY:   auth:authorization:id:a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8
        VALUE: "{\"id\":\"a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8\", \"principalName\":\"user\", \"accessToken\":{...}, ...}"

        # -----------------------------------------------------
        # SECONDARY INDEXES: Token keys point only to the ID
        # -----------------------------------------------------

        # Index for the Authorization Code
        KEY:   auth:authorization:auth_code:def456
        VALUE: "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"

        # Index for the Access Token
        KEY:   auth:authorization:access_token:xyz789
        VALUE: "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"

        # Index for the Refresh Token
        KEY:   auth:authorization:refresh_token:abc123
        VALUE: "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"
        */
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        System.out.println();
    }

    @Override
    public OAuth2Authorization findById(String id) {
        System.out.println();
        return null;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        System.out.println();
        return null;
    }

    private String buildKey(KeyType type, String value) {
        return String.format("%s:%s:%s", AUTHORIZATION_NAMESPACE, type.name().toLowerCase(), value);
    }

    private enum KeyType {
        ID, AUTH_CODE, ACCESS_TOKEN, REFRESH_TOKEN, REVOKED, UNKNOWN;
    }

}
