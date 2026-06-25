package com.github.liyibo1110.secondkill.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.liyibo1110.secondkill.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 两级缓存：Caffeine（L1本地缓存）+ Redis（L2分布式缓存）。
 *
 * 查询链路：Caffeine → Redis → sourceLoader回源（Dubbo调用）。
 * 刷新策略：Caffeine的refreshAfterWrite触发异步刷新，刷新时只查Redis不回源Dubbo。
 * 穿透防护：对null结果缓存NullValue占位符。
 * 击穿防护：LoadingCache保证同一key单线程加载。
 * @author liyibo
 * @date 2026-06-24 11:06
 */
@Slf4j
public class TwoLevelCache<V> {

    private static final String NULL_MARKER = "NULL";
    private static final Object NULL_VALUE = new Object();

    private final LoadingCache<String, Object> caffeineCache;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final String redisKeyPrefix;
    private final long redisTtlSeconds;
    private final long nullTtlSeconds;
    private final JavaType valueType;

    /**
     * @param maxSize        Caffeine最大条目数
     * @param expireSeconds  Caffeine硬过期时间（秒），兜底防止数据永不失效
     * @param refreshSeconds Caffeine刷新间隔（秒），到期后下次访问触发异步刷新
     * @param redisService   Redis操作
     * @param objectMapper   JSON序列化
     * @param redisKeyPrefix Redis key前缀
     * @param redisTtlSeconds Redis过期时间（秒）
     * @param nullTtlSeconds  null标记的Redis过期时间（秒）
     * @param valueType      反序列化目标类型
     * @param sourceLoader   回源加载函数（Dubbo调用），仅在Caffeine和Redis都未命中时执行
     */
    public TwoLevelCache(int maxSize, long expireSeconds, long refreshSeconds,
                         RedisService redisService, ObjectMapper objectMapper,
                         String redisKeyPrefix, long redisTtlSeconds,
                         long nullTtlSeconds, JavaType valueType,
                         Function<String, V> sourceLoader) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.redisKeyPrefix = redisKeyPrefix;
        this.redisTtlSeconds = redisTtlSeconds;
        this.nullTtlSeconds = nullTtlSeconds;
        this.valueType = valueType;

        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .refreshAfterWrite(refreshSeconds, TimeUnit.SECONDS)
                .recordStats()
                .build(new CacheLoader<>() {
                    @Override
                    public Object load(String key) {
                        // 首次加载：Redis → Dubbo回源
                        return loadFromRedisOrSource(key, sourceLoader);
                    }

                    @Override
                    public Object reload(String key, Object oldValue) {
                        // 后台异步刷新：只查Redis，不调Dubbo
                        // Redis由外部定时任务保持数据新鲜
                        try {
                            Object fromRedis = loadFromRedisOnly(key);
                            return fromRedis != null ? fromRedis : oldValue;
                        } catch (Exception e) {
                            // Redis异常时返回旧值，不影响调用方
                            return oldValue;
                        }
                    }
                });
    }

    /**
     * 查询缓存。
     * Caffeine命中直接返回。刷新间隔到期后，返回旧值的同时在后台异步刷新。
     * LoadingCache保证同一个key同一时刻只有一个线程执行加载，天然防止缓存击穿。
     */
    @SuppressWarnings("unchecked")
    public V get(String key) {
        Object value = caffeineCache.get(key);
        if (value == NULL_VALUE)
            return null;

        return (V) value;
    }

    /**
     * 主动写入两级缓存（供缓存预热使用）
     */
    public void put(String key, V value) {
        if (value == null)
            return;

        caffeineCache.put(key, value);
        String redisKey = redisKeyPrefix + key;
        try {
            String json = objectMapper.writeValueAsString(value);
            redisService.set(redisKey, json, redisTtlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("缓存序列化失败, key={}", redisKey, e);
        }
    }

    /**
     * 清除两级缓存
     */
    public void evict(String key) {
        caffeineCache.invalidate(key);
        redisService.delete(redisKeyPrefix + key);
    }

    /**
     * 获取Caffeine缓存统计数据
     * 包含命中率、命中次数、未命中次数、淘汰次数等指标
     */
    public CacheStats getStats() {
        return caffeineCache.stats();
    }

    /**
     * 首次加载：先查Redis，Redis未命中再调sourceLoader回源
     */
    private Object loadFromRedisOrSource(String key, Function<String, V> sourceLoader) {
        String redisKey = redisKeyPrefix + key;
        String json = redisService.get(redisKey);

        if (json != null) {
            if (NULL_MARKER.equals(json))
                return NULL_VALUE;

            try {
                return objectMapper.readValue(json, valueType);
            } catch (JsonProcessingException e) {
                log.warn("缓存反序列化失败, key={}", redisKey, e);
            }
        }

        // Redis未命中，调用sourceLoader回源
        V value = sourceLoader.apply(key);

        if (value == null) {
            // 穿透防护：缓存null标记，短TTL
            redisService.set(redisKey, NULL_MARKER, nullTtlSeconds, TimeUnit.SECONDS);
            return NULL_VALUE;
        }

        // 回填Redis
        try {
            String valueJson = objectMapper.writeValueAsString(value);
            redisService.set(redisKey, valueJson, redisTtlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("缓存序列化失败, key={}", redisKey, e);
        }

        return value;
    }

    /**
     * 后台刷新时只查Redis，不回源Dubbo
     */
    private Object loadFromRedisOnly(String key) {
        String redisKey = redisKeyPrefix + key;
        String json = redisService.get(redisKey);

        if (json == null)
            return null;

        if (NULL_MARKER.equals(json))
            return NULL_VALUE;

        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            log.warn("缓存反序列化失败, key={}", redisKey, e);
            return null;
        }
    }
}
