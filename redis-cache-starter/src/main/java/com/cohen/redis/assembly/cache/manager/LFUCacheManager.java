package com.cohen.redis.assembly.cache.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 缓存失效策略，最近最少使用
 *
 * @author 林金成
 * @date 2018/5/189:02
 */
public class LFUCacheManager implements CacheManager {
    /**
     * 日志工具
     */
    private static final Logger LOG = LoggerFactory.getLogger(LFUCacheManager.class);
    private Map<String, Map<String, Object>> cacheMap = null;// 缓存key和使用次数
    private int initCacheSize = 1024;

    private LFUCacheManager() {
    }

    public LFUCacheManager(int initCacheSize) {
        this.cacheMap = new HashMap<>();
        this.initCacheSize = initCacheSize;
    }

    @Override
    public boolean containsNamespaceInCacheMap(String namespace) {
        boolean b = this.cacheMap.containsKey(namespace);
        LOG.debug("[Redis Cache] [LFUCacheManager] : 判断缓存中是否存在 namespace: {}, - result: {}", namespace, b);
        return b;
    }

    @Override
    public boolean containsCacheKeyInCacheMap(String namespace, String key) {
        boolean b = (this.cacheMap.get(namespace) != null && this.cacheMap.get(namespace).containsKey(key));
        LOG.debug("[Redis Cache] [LFUCacheManager] : 判断缓存中是否存在 key: {}, - namespace: {} - key: {} - result: {}", key, namespace, key, b);
        return b;
    }

    @Override
    public String cacheKey(String namespace, String key) {
        this.validNamespace(namespace);
        String result = (String)this.cacheMap.get(namespace).put(key, 1);
        LOG.debug("[Redis Cache] [LFUCacheManager] : 保存缓存key: {} - namespace: {} - key: {} - 是否需要删除过期key: {} - 过期key: {}", key, namespace, key, result != null, result);
        return result;
    }

    public void increase(String namespace, String key) {
        this.cacheMap.get(namespace).get(key);// 调重写的get方法, 递增使用次数
    }

    private void validNamespace(String namespace) {
        if (this.cacheMap.get(namespace) == null) {
            this.cacheMap.put(namespace, new HashMap<String, Object>(initCacheSize, 0.75f) {
                /**
                 * 如果size达到initCacheSize, 先对节点按次数排序, 删除次数最少的节点并返回key
                 */
                @Override
                public Object put(String key, Object value) {
                    // 如果put前size达到initCacheSize, 需要删除使用次数最少的key, 先对map中的节点按次数降序排序, 将次数最少的节点key返回
                    if (this.size() == initCacheSize) {
                        List<Map.Entry<String, Object>> list = new ArrayList(this.entrySet());
                        Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
                            @Override
                            public int compare(Entry<String, Object> o1, Entry<String, Object> o2) {
                                return ((Integer) o2.getValue()).compareTo((Integer) o1.getValue());// 降序排序
                            }
                        });
                        String removeKey = list.get(list.size() - 1).getKey();
                        this.remove(removeKey);
                        return removeKey;
                    }
                    super.put(key, value);
                    return null;
                }

                @Override
                public Object get(Object key) {
                    Integer count = (Integer)super.get(key) + 1;
                    this.remove(key);
                    super.put((String)key, count);// 递增次数
                    return super.get(key);
                }
            });
            LOG.debug("[Redis Cache] [LFUCacheManager] : Namespace \"{}\" 为空, 创建一个HashMap, 大小为 {}", namespace, this.initCacheSize);
        }
    }
}
