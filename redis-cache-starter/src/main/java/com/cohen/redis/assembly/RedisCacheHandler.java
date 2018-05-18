package com.cohen.redis.assembly;

import com.cohen.redis.assembly.cache.Cache;
import com.cohen.redis.assembly.cache.RedisCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 林金成
 * @date 2018/5/129:52
 */
@Aspect
public class RedisCacheHandler {

    /**
     * 日志工具
     */
    private static final Logger LOG = LoggerFactory.getLogger(RedisCacheHandler.class);
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();// 读写锁
    private Cache redisCache;

    private RedisCacheHandler() {
    }

    public RedisCacheHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * AOP增强标记 @RedisCached 注解的DAO层方法，先查redis缓存，缓存中没有再去执行目标方法查数据库，查询后将数据缓存入redis
     */
    @Around("@annotation(com.cohen.redis.annotation.RedisCached)")
    public Object cached(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();// 类名，缓存的namespace
        StringBuffer sb = new StringBuffer();// 拼缓存的key: 方法名:参数值:...
        sb.append(signature.getName());
        Object[] args = joinPoint.getArgs();
        for (Object arg :
                args) {
            sb.append(":").append(arg);
        }
        String key = sb.toString();// 缓存key
        this.lock.readLock().lock();// 准备操作缓存，上读锁
        if (redisCache.contains(className, key)) {// 如果缓存中存在，取出返回
            try {
                result = redisCache.queryFromCache(className, key);
            } finally {
                this.lock.readLock().unlock();// 从缓存中取完数据，释放读锁
            }
        } else {
            // 缓存中没有，准备写入缓存，释放读锁，上写锁
            this.lock.readLock().unlock();
            this.lock.writeLock().lock();
            // 考虑到当一条线程写的时候，其他线程有可能已经阻塞在这，所以再次判断缓存中有没有
            if (!redisCache.contains(className, key)) {// 依然没有，查询到放到缓存中，释放写锁
                try {
                    result = joinPoint.proceed(args);
                    redisCache.updateToCache(className, key, result);
                } finally {
                    this.lock.writeLock().unlock();// 释放写锁
                }
            } else {// 缓存中已经存在，释放写锁上读锁，从缓存中取到
                this.lock.writeLock().unlock();
                this.lock.readLock().lock();
                try {
                    result = redisCache.queryFromCache(className, key);
                } finally {
                    this.lock.readLock().unlock();// 释放读锁
                }
            }
        }
        return result;
    }

    /**
     * AOP增强标记了 @RedisCleared 注解的DAO层方法，当执行更改数据方法的时候清除相关namespace缓存
     */
    @After("@annotation(com.cohen.redis.annotation.RedisCleared)")
    public void clear(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();// 获得目标信息（方法）
        String className = signature.getDeclaringTypeName();// 类名，缓存的namespace
        if(redisCache.containsNamespace(className)){
            redisCache.clearCache(className);
        }
    }
}
