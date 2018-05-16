package com.cohen.redis;

import com.cohen.redis.annotation.RedisCached;
import com.cohen.redis.assembly.RedisCache;
import com.cohen.redis.assembly.RedisCacheHandler;
import com.cohen.redis.assembly.RedisDao;
import com.cohen.redis.property.JedisPoolProperty;
import com.cohen.redis.property.RedisCacheProperty;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

/**
 * redis缓存自动配置类
 *
 * @author 林金成
 * @date 2018/5/1210:51
 */
@Configuration
@EnableConfigurationProperties({RedisCacheProperty.class, JedisPoolProperty.class})// 引入配置信息类
public class RedisCacheAutoConfiguration {

    /**
     * jedisPool
     */
    @Bean
    @ConditionalOnMissingBean
    public JedisPool jedisPool(JedisPoolProperty property) {
        JedisPool jedisPool = null;
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(property.getMaxidle());
        config.setMaxWaitMillis(property.getMaxwait());
        if (!property.getPassword().isEmpty()) {
            jedisPool = new JedisPool(config, property.getHost(), property.getPort(), property.getTimeout(), property.getPassword());
        } else {
            jedisPool = new JedisPool(config, property.getHost(), property.getPort(), property.getTimeout());
        }
        return jedisPool;
    }

    /**
     * redisDao
     */
    @Bean
    @ConditionalOnMissingBean({RedisDao.class})
    @ConditionalOnBean({JedisPool.class})
    public RedisDao redisDao(JedisPool jedisPool) {
        RedisDao redisDao = new RedisDao();
        redisDao.setJedisPool(jedisPool);
        return redisDao;
    }

    @Bean
    @ConditionalOnMissingBean({RedisCache.class})
    @ConditionalOnBean({RedisDao.class})
    @ConditionalOnProperty(name = "redis.cache.enable", havingValue = "true", matchIfMissing = true)// 判断是否配置了开启缓存
    public RedisCache redisCache(RedisDao redisDao, RedisCacheProperty property) {
        return new RedisCache(redisDao, property.getInitCacheSize(), property.getSecond());
    }

    @Bean
    @ConditionalOnMissingBean({RedisCacheHandler.class})
    @ConditionalOnBean({RedisCache.class})
    @ConditionalOnClass({RedisCached.class,Aspect.class})// 判断类路径下是否有Aspect的依赖
    public RedisCacheHandler redisCacheHandler(RedisCache redisCache) {
        return new RedisCacheHandler(redisCache);
    }
}
