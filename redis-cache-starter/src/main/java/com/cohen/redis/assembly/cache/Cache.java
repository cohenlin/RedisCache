package com.cohen.redis.assembly.cache;

/**
 * @author 林金成
 * @date 2018/5/1717:53
 */
public interface Cache {
    Object queryFromCache(String namespace, String key);

    void updateToCache(String namespace, String key, Object value);

    void clearCache(String namespace);

    boolean contains(String namespace, String key);

    boolean containsNamespace(String namespace);
}
