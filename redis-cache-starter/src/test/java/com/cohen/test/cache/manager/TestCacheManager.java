package com.cohen.test.cache.manager;

import com.cohen.redis.RedisCacheAutoConfiguration;
import com.cohen.redis.assembly.cache.RedisCache;
import com.cohen.redis.assembly.cache.RedisDao;
import com.cohen.redis.property.JedisPoolProperty;
import com.cohen.redis.property.RedisCacheProperty;
import com.cohen.test.cache.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RunWith(JUnit4.class)
public class TestCacheManager {

    @Test
    public void testFIFO() {
        Configuration configuration = this.getConfiguration(2, "FIFO");
        RedisCache redisCache = configuration.getBean(RedisCache.class);
        redisCache.clearCache("a");// 先清除缓存
        redisCache.updateToCache("a", "k1", "v1");
        redisCache.updateToCache("a", "k2", "v2");
        redisCache.updateToCache("a", "k3", "v3");
    }

    @Test
    public void testLRU() {
        Configuration configuration = this.getConfiguration(2, "LRU");
        RedisCache redisCache = configuration.getBean(RedisCache.class);
        redisCache.clearCache("a");// 先清除缓存
        redisCache.updateToCache("a", "k1", "v1");
        redisCache.updateToCache("a", "k2", "v2");
        redisCache.queryFromCache("a", "k1");
        redisCache.updateToCache("a", "k3", "v3");
    }

    @Test
    public void testLFU() {
        Configuration configuration = this.getConfiguration(2, "LFU");
        RedisCache redisCache = configuration.getBean(RedisCache.class);
        redisCache.clearCache("a");// 先清除缓存
        redisCache.updateToCache("a", "k1", "v1");
        redisCache.updateToCache("a", "k2", "v2");
        redisCache.queryFromCache("a", "k2");
        redisCache.updateToCache("a", "k3", "v3");
    }

    private Configuration getConfiguration(int size, String rule) {
        Configuration configuration = null;
        try {
            configuration = new Configuration(RedisCacheAutoConfiguration.class, this.getRedisCacheProperty(size, rule), this.getJedisPoolProperty());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configuration;
    }

    /**
     * 清除redis
     */
    @Test
    public void flushDB() {
        Configuration configuration = this.getConfiguration(1, "FIFO");
        JedisPool pool = configuration.getBean(JedisPool.class);
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        jedis.close();
    }

    private RedisCacheProperty getRedisCacheProperty(int size, String rule) {
        RedisCacheProperty property = new RedisCacheProperty();
        property.setInitCacheSize(size);
        property.setFailureRule(rule);
        property.setEnable(true);
        return property;
    }

    private JedisPoolProperty getJedisPoolProperty() {
        JedisPoolProperty property = new JedisPoolProperty();
        property.setHost("47.94.148.40");
        property.setPort(6379);
        property.setPassword("");
        property.setMaxidle(5);
        property.setMaxwait(100000);
        property.setTimeout(100000);
        return property;
    }
}
