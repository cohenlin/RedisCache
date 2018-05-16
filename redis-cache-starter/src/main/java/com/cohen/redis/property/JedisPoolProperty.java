package com.cohen.redis.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 林金成
 * @date 2018/5/129:03
 */
@ConfigurationProperties(prefix = "redis.pool")
public class JedisPoolProperty {
    private String host = "localhost";
    private int port = 6379;
    private String password = "";
    private int timeout = 100000;
    private int maxidle = 5;
    private int maxwait = 100000;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxidle() {
        return maxidle;
    }

    public void setMaxidle(int maxidle) {
        this.maxidle = maxidle;
    }

    public int getMaxwait() {
        return maxwait;
    }

    public void setMaxwait(int maxwait) {
        this.maxwait = maxwait;
    }
}
