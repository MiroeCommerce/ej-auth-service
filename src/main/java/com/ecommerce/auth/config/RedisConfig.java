package com.ecommerce.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * Configures and creates a {@link RedisTemplate} bean to be used for Redis data access.
     * <p>
     * This method sets up a {@code RedisTemplate} with specific serializers to ensure that
     * keys are stored as human-readable strings and values are stored in JSON format.
     * This approach allows for storing complex Java objects in Redis while maintaining
     * interoperability and readability.
     * </p>
     * <ul>
     * <li><b>Key Serializers:</b> Both standard keys and hash keys are serialized using {@link StringRedisSerializer}.</li>
     * <li><b>Value Serializers:</b> Both standard values and hash values are serialized using {@link GenericJackson2JsonRedisSerializer}, which converts Java objects to and from JSON.</li>
     * </ul>
     *
     * @param redisConnectionFactory The connection factory for connecting to the Redis server,
     *                               which is automatically provided by the Spring container.
     * @return A fully configured {@link RedisTemplate} instance ready for injection.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Use StringRedisSerializer for keys and hash keys.
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Use GenericJackson2JsonRedisSerializer for values and hash values.
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Initialize the template after setting the properties.
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
