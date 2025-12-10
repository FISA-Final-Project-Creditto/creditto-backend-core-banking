package org.creditto.core_banking.domain.redis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@Disabled
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisConnection() {
        String key = "test-key";
        String expectedValue = "redis";
        redisTemplate.opsForValue().set(key, expectedValue);
        Object value = redisTemplate.opsForValue().get(key);
        System.out.println("Redis Value: " + value);
    }
}
