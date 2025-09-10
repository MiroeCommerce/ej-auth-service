package com.ecommerce.auth.service;

import java.util.concurrent.TimeUnit;

public interface CacheService {

    void create(String key, Object value);

    void createWithTTL(String key, Object value, long timeout, TimeUnit timeUnit);

    Object read(String key);

    boolean update(String key, Object value);

    boolean delete(String key);

}
