package com.cohen.redis.assembly;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 林金成
 * @date 2018/5/1123:51
 */
public class RedisCache {

    // 用于最近最少使用策略，统计key的查询次数
//    private Map<String, Integer> useCountOfCacheKeys = null;
    // 用于先进先出策略，存放缓存的key
//    private ArrayBlockingQueue queueOfCacheKeys = new ArrayBlockingQueue(1024);
    private int second = 24 * 60 * 60;// 默认为一天，单位是秒

    private int initCacheSize = 1024;// 缓存大小，默认为

    private RedisDao redisDao;

    private RedisCache() {
    }

    public RedisCache(RedisDao redisDao, int initCacheSize, int second) {
//        useCountOfCacheKeys = new HashMap<>(initCacheSize, 0.8f);
        this.second = second;
        this.redisDao = redisDao;
    }

    /**
     * 从缓存中取
     */
    public Object queryFromCache(String key) {
        return redisDao.get(key);
    }

    /**
     * 从缓存中取
     */
    public Object queryFromCache(String namespace, String key) {
        return redisDao.hget(namespace, key);
    }

    /**
     * 更新到缓存
     */
    public void updateToCache(String namespace, String key, Object value) {
        redisDao.hset(namespace, key, value);
    }

    public void removeCacheKey(String namespace, String key) {
        redisDao.hdel(namespace, key);
    }

    /**
     * 清除缓存
     */
    public void clearCache(String namespace) {
        redisDao.del(namespace);
    }

    /**
     * 判断缓存中是否存在key
     */
    public boolean contains(String namespace, String key) {
        return redisDao.containsKeyInHset(namespace, key);
    }
}
