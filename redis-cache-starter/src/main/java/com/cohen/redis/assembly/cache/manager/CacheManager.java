package com.cohen.redis.assembly.cache.manager;

/**
 * 缓存失效策略接口
 *
 * @author 林金成
 * @date 2018/5/1710:44
 */
public interface CacheManager {
    /**
     * 判断当前cacheMap中是否存在namespace
     *
     * @param namespace
     * @return
     */
    boolean containsNamespaceInCacheMap(String namespace);

    /**
     * 判断当前cacheMap中是否存在缓存key
     *
     * @param namespace
     * @param key
     * @return
     */
    boolean containsCacheKeyInCacheMap(String namespace, String key);

    /**
     * 将缓存的key保存至map
     *
     * @param namespace
     * @param key
     * @return : 当map大小达到设置值时，会删除最老元素并返回其key，根据返回的key删除redis中的缓存数据
     */
    String cacheKey(String namespace, String key);
}
