package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Saves a key-value pair to Redis. This will overwrite any existing key.
     *
     * @param key   The key to store.
     * @param value The value to store.
     */
    @Override
    public void create(String key, Object value) {
        this.redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Saves a key-value pair with an expiration time.
     *
     * @param key      The key to store.
     * @param value    The value to store.
     * @param timeout  The duration before the key expires.
     * @param timeUnit The unit of time for the timeout.
     */
    @Override
    public void createWithTTL(String key, Object value, long timeout, TimeUnit timeUnit) {
        this.redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * Retrieves a value by its key.
     *
     * @param key The key to retrieve.
     * @return The value associated with the key, or null if not found.
     */
    @Override
    public Object read(String key) {
        return this.redisTemplate.opsForValue().get(key);
    }

    /**
     * Updates an existing key with a new value.
     *
     * @param key   The key of the entry to update.
     * @param value The new value.
     * @return true if the key existed and was updated, false otherwise.
     */
    @Override
    public boolean update(String key, Object value) {
        Boolean result = this.redisTemplate.opsForValue().setIfPresent(key, value);
        return result != null && result;
    }

    /**
     * Deletes a key-value pair.
     *
     * @param key The key to delete.
     * @return true if the key was deleted, false if the key did not exist.
     */
    @Override
    public boolean delete(String key) {
        Boolean result = this.redisTemplate.delete(key);
        return result != null && result;
    }

}
