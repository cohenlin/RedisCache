package com.cohen.redis.assembly.cache.manager;

import java.util.*;

/**
 * 缓存失效策略，最近最少使用
 *
 * @author 林金成
 * @date 2018/5/189:02
 */
public class LFUCacheManager implements CacheManager {
    private Map<String, List<LFUCacheManager.Entry<String, Integer>>> cacheMap = null;// 缓存key和使用次数
    private Map<String, Set<String>> keyMap = null;// 保存key，用于校验是否存在缓存key

    @Override
    public boolean containsNamespaceInCacheMap(String namespace) {
        return cacheMap.containsKey(namespace);
    }

    @Override
    public boolean containsCacheKeyInCacheMap(String namespace, String key) {
        return ((this.cacheMap.get(namespace) != null) && (this.keyMap.get(namespace).contains(key)));
    }

    @Override
    public String cacheKey(String namespace, String key) {
        this.validNamespace(namespace);
        return null;
    }

    private void validNamespace(String namespace) {
        if (this.cacheMap.get(namespace) == null) {
            this.cacheMap.put(namespace, new ArrayList<>());
            this.keyMap.put(namespace, new HashSet<>());
        }
    }

    /**
     * 保存key和使用次数
     *
     * @param <K>
     * @param <V>
     */
    private static class Entry<K, V> implements Map

            .Entry<K, V> {
        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V value) {
            return null;
        }
    }
}
