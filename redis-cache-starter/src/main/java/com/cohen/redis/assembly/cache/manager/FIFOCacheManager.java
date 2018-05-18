package com.cohen.redis.assembly.cache.manager;

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
        return cacheMap.containsKey(namespace);
    }

    /**
     * 判断cacheMap中是否存在key
     */
    public boolean containsCacheKeyInCacheMap(String namespace, String key) {
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
        return result;
    }

    private void validNamespace(String namespace) {
        if (this.cacheMap.get(namespace) == null) {// 如果当前namespace为空
            this.cacheMap.put(namespace, new ArrayBlockingQueue(this.initCacheSize));// 根据初始大小创建一个
        }
    }
}
