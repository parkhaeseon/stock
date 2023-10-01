package com.example.stock.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockRepository {

    private RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // key와 setnx 명령어를 통해 lock
    public Boolean lock(Long key) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));
    }

    public Boolean unlock(Long key) {
        return redisTemplate.delete(generateKey(key));
    }

    /**
     * redis를 활용하는 방식도 로직 실행 전, 후로
     * lock 획득 및 해제를 수행해야하기 때문에 facade class를 하나 생성해주도록 한다.
     */

    private String generateKey(Long key) {
        return key.toString();
    }
}
