package com.nhb.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作工具类
 * <p>
 * 提供对 Redis String 类型的通用存取操作，支持泛型、过期时间、类型安全。
 * </p>
 */
@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置指定 key 的值，并设置过期时间
     *
     * @param key     键（不允许为 null）
     * @param value   值（可为任意可序列化对象）
     * @param timeout 过期时间数值（必须 > 0）
     * @param unit    时间单位（如 TimeUnit.HOURS）
     * @throws IllegalArgumentException if key is null or timeout <= 0
     */
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        if (key == null || timeout <= 0) {
            throw new IllegalArgumentException("Key 不能为 null，且过期时间必须大于 0");
        }
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置指定 key 的值，永不过期
     *
     * @param key   键
     * @param value 值
     */
    public void setValue(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key 不能为 null");
        }
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取指定 key 对应的值
     *
     * @param key 键
     * @param <T> 期望返回的类型（调用方需确保类型匹配）
     * @return 对应的值，若不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定的 key
     *
     * @param key 键
     * @return 是否删除成功（true 表示 key 存在并被删除）
     */
    public boolean deleteKey(String key) {
        if (key == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return 存在返回 true，否则 false
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 为已存在的 key 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间数值
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
}