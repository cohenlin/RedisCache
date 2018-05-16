package com.cohen.redis.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 林金成
 * @date 2018/5/1210:45
 */
@ConfigurationProperties(prefix = "redis.cache")
public class RedisCacheProperty {
    private int initCacheSize = 1024;// 缓存大小
    private int second = 24 * 60 * 60;// 缓存时间
    private boolean enable;
    private String packageName;

    public int getInitCacheSize() {
        return initCacheSize;
    }

    public void setInitCacheSize(int initCacheSize) {
        this.initCacheSize = initCacheSize;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
