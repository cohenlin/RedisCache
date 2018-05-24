package com.cohen.redis.assembly.cache.manager;

import com.cohen.redis.assembly.cache.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 缓存失效策略，先进先出
 *
 * @author 林金成
 * @date 2018/5/1710:44
 */
public class FIFOCacheManager implements CacheManager {
    /**
     * 日志工具
     */
    private static final Logger LOG = LoggerFactory.getLogger(FIFOCacheManager.class);
    private Map<String, ArrayBlockingQueue> cacheMap = null;
    private int initCacheSize;// 缓存大小，默认为1024

    private FIFOCacheManager() {
    }

    public FIFOCacheManager(int initCacheSize) {
        this.cacheMap = new HashMap<>();
        this.initCacheSize = initCacheSize;
    }

    /**
     * 判断cacheMap中是否存在namespace
     */
    public boolean containsNamespaceInCacheMap(String namespace) {
        LOG.debug("[Redis Cache] [FIFOCacheManager] : 判断缓存中是否存在 namespace: {}, - result: {}", namespace, this.cacheMap.containsKey(namespace));
        return this.cacheMap.containsKey(namespace);
    }

    /**
     * 判断cacheMap中是否存在key
     */
    public boolean containsCacheKeyInCacheMap(String namespace, String key) {
        LOG.debug("[Redis Cache] [FIFOCacheManager] : 判断缓存中是否存在 key: {}, - namespace: {} - key: {} - result: {}", key, namespace, key, this.cacheMap.containsKey(namespace));
        return ((cacheMap.get(namespace) != null) && (cacheMap.get(namespace).contains(key)));
    }

    /**
     * 保存缓存key
     */
    public String cacheKey(String namespace, String key) {
        this.validNamespace(namespace);
        String result = null;
        // 将缓存key存入cacheMap中对应的namespace
        if (!cacheMap.get(namespace).offer(key)) {// 如果没有插入成功，key为非空，说明队列已经满了，则取出最早插入的一条删除
            result = (String) cacheMap.get(namespace).poll();
            cacheMap.get(namespace).offer(key);// 再次插入缓存key
        }
        LOG.debug("[Redis Cache] [FIFOCacheManager] : 保存缓存key: {} - namespace: {} - key: {} - 是否需要删除过期key: {} - 过期key: {}", key, namespace, key, result != null, result);
        return result;
    }

    private void validNamespace(String namespace) {
        if (this.cacheMap.get(namespace) == null) {// 如果当前namespace为空
            this.cacheMap.put(namespace, new ArrayBlockingQueue(this.initCacheSize));// 根据初始大小创建一个
            LOG.debug("[Redis Cache] [FIFOCacheManager] : Namespace \"{}\" 为空, 创建一个ArrayBlockingQueue, 大小为 {}", namespace, this.initCacheSize);
        }
    }
}
