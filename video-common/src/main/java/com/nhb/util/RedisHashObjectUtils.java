package com.nhb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用于将 Java 对象以 Hash 形式存入 Redis 的工具类
 * - 对象的每个属性名作为 Hash 的 field
 * - 每个属性值（包括 List、嵌套对象等）自动序列化为 JSON 字符串作为 value
 */
@Component
public class RedisHashObjectUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 将对象以 Hash 形式存入 Redis（带过期时间）
     *
     * @param key   Redis key
     * @param obj   要存储的对象（不能为 null）
     * @param timeout 过期时间数值
     * @param unit  时间单位
     */
    public void setObject(String key, Object obj, long timeout, TimeUnit unit) {
        if (key == null || obj == null) {
            throw new IllegalArgumentException("Key and object must not be null");
        }
        Map<String, String> hash = objectToHash(obj);
        redisTemplate.opsForHash().putAll(key, hash);
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 将对象以 Hash 形式存入 Redis（永不过期）
     */
    public void setObject(String key, Object obj) {
        if (key == null || obj == null) {
            throw new IllegalArgumentException("Key and object must not be null");
        }
        Map<String, String> hash = objectToHash(obj);
        redisTemplate.opsForHash().putAll(key, hash);
    }

    /**
     * 从 Redis Hash 读取并还原为指定类型的对象
     */
    public <T> T getObject(String key, Class<T> clazz) {
        if (key == null || clazz == null) {
            return null;
        }
        Map<Object, Object> hashEntries = redisTemplate.opsForHash().entries(key);
        if (hashEntries == null || hashEntries.isEmpty()) {
            return null;
        }

        // 将 Map<Object, Object> 转为 Map<String, String>
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : hashEntries.entrySet()) {
            String field = (String) entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : null;
            stringMap.put(field, value);
        }

        // 反序列化为对象
        try {
            return mapToObject(stringMap, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize hash to object: " + clazz.getName(), e);
        }
    }

    /**
     * 删除对象对应的 Hash
     */
    public boolean deleteObject(String key) {
        if (key == null) return false;
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }


    /**
     * 检查指定的 key 是否存在于 Redis 中（不关心其类型）
     *
     * @param key Redis key
     * @return true 如果 key 存在；否则 false
     */
    public boolean exists(String key) {
        if (key == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 更新 Redis Hash 中指定 field 的值（自动将值序列化为 JSON 字符串）
     *
     * @param key   Redis key
     * @param field Hash 的字段名
     * @param value 要存储的值（支持任意类型，包括 null、List、Map、对象等）
     * @throws RuntimeException 如果序列化失败
     */
    public void putField(String key, String field, Object value) {
        if (key == null || field == null) {
            throw new IllegalArgumentException("Key and field must not be null");
        }

        try {
            String jsonValue = objectMapper.writeValueAsString(value); // null 会被序列化为 "null"
            redisTemplate.opsForHash().put(key, field, jsonValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize value for field '" + field + "' in key '" + key + "'", e);
        }
    }
    // ------------------ 内部转换方法 ------------------

    private Map<String, String> objectToHash(Object obj) {
        try {
            Map<String, Object> objectMap = objectMapper.convertValue(obj, Map.class);
            Map<String, String> hash = new HashMap<>();
            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String jsonValue = (value == null) ? "" : objectMapper.writeValueAsString(value);
                hash.put(key, jsonValue);
            }
            return hash;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing object to hash", e);
        }
    }

    private <T> T mapToObject(Map<String, String> stringMap, Class<T> clazz) throws JsonProcessingException {
        // 先将每个 value 从 JSON 字符串还原为 Object
        Map<String, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            String field = entry.getKey();
            String jsonValue = entry.getValue();
            if (jsonValue == null || jsonValue.isEmpty()) {
                objectMap.put(field, null);
            } else {
                // 使用 readTree + convertValue 更安全地处理类型
                Object value = objectMapper.readValue(jsonValue, Object.class);
                objectMap.put(field, value);
            }
        }
        return objectMapper.convertValue(objectMap, clazz);
    }
}