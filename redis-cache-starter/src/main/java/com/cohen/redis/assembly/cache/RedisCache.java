package com.cohen.redis.assembly.cache;

import com.cohen.redis.assembly.cache.manager.CacheManager;
import com.cohen.redis.assembly.cache.manager.LFUCacheManager;
import com.cohen.redis.assembly.cache.manager.LRUCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 林金成
 * @date 2018/5/1123:51
 */
public class RedisCache implements Cache {

    /**
     * 日志工具
     */
    private static final Logger LOG = LoggerFactory.getLogger(RedisCache.class);
    private RedisDao redisDao;
    private CacheManager cacheManager;// 管理缓存清除机制

    private RedisCache() {
    }

    public RedisCache(RedisDao redisDao, CacheManager cacheManager) {
        this.redisDao = redisDao;
        this.cacheManager = cacheManager;
    }

    /**
     * 从缓存中取
     */
    public Object queryFromCache(String namespace, String key) {
        Object result = redisDao.hget(namespace, key);
        if (this.cacheManager instanceof LFUCacheManager) {// 如果是LFUCacheManager的实例, 每次从缓存中取需要递增使用次数
            ((LFUCacheManager) this.cacheManager).increase(namespace, key);
        }
        if (this.cacheManager instanceof LRUCacheManager) {// 如果是LRUCacheManager的实例, 每次从缓存中取需要调用map的get方法, 将key从链表中移动至链表尾端
            ((LRUCacheManager) this.cacheManager).moveKey(namespace, key);
        }
        LOG.info("[Redis Cache] : 缓存命中 - namespace: {} - key: {} - result: {}", namespace, key, result.toString());
        return result;
    }

    /**
     * 更新到缓存
     */
    public void updateToCache(String namespace, String key, Object value) {
        String poll = null;
        if ((poll = cacheManager.cacheKey(namespace, key)) != null) {// 保存key的时候, 清掉了一个另key，从redis中删除
            this.removeCacheKey(namespace, poll);// 删除被清除的key对应的redis缓存数据
        }
        redisDao.hset(namespace, key, value);// 将数据缓存入redis
        LOG.info("[Redis Cache] : 缓存未命中, 数据已缓存入redis - namespace: {} - key: {} - result: {}", namespace, key, value.toString());
    }

    /**
     * 删除redis中缓存key
     */
    private void removeCacheKey(String namespace, String key) {
        LOG.info("[Redis Cache] : 已清除 \"{}\" 中缓存 \"{}\" 数据", namespace, key);
        redisDao.hdel(namespace, key);
    }

    /**
     * 清除缓存
     */
    public void clearCache(String namespace) {
        LOG.info("[Redis Cache] : 已清除 \"{}\" 中所有缓存数据", namespace);
        redisDao.del(namespace);
    }

    /**
     * 判断缓存中是否存在key
     */
    public boolean contains(String namespace, String key) {
        return cacheManager.containsCacheKeyInCacheMap(namespace, key);
    }

    /**
     * 判断缓存中是否存在namespace
     */
    public boolean containsNamespace(String namespace) {
        return cacheManager.containsNamespaceInCacheMap(namespace);
    }
}