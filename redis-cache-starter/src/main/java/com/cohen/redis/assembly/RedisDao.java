package com.cohen.redis.assembly;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.*;

/**
 * redis工具类
 *
 * @author 林金成
 * @date 2018/5/1122:57
 */
public class RedisDao {

    private JedisPool jedisPool;

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 保存key,value至redis
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        Jedis jedis = this.jedisPool.getResource();
        jedis.set(serialize(key), serialize(value));
        jedis.close();
    }

    /**
     * 保存key,value至redis，并设置有效时间
     *
     * @param key
     * @param seconds
     * @param value
     */
    public void setex(String key, int seconds, Object value) {
        Jedis jedis = this.jedisPool.getResource();
        jedis.setex(serialize(key), seconds, serialize(value));
        jedis.close();
    }

    /**
     * 获取key对应的value
     *
     * @param key
     */
    public Object get(String key) {
        Jedis jedis = this.jedisPool.getResource();
        Object result = null;
        try {
            result = deserialize(jedis.get(serialize(key)));
        } catch (Throwable throwable) {
            result = null;
        }finally {
            jedis.close();
        }
        return result;
    }

    public Object hget(String hset, String key) {
        Jedis jedis = this.jedisPool.getResource();
        Object result = null;
        try {
            result = deserialize(jedis.hget(serialize(hset), serialize(key)));
        } catch (Throwable throwable) {
            result = null;
        }finally {
            jedis.close();
        }
        return result;
    }

    /**
     * 判断是否存在key
     *
     * @param key
     * @return
     */
    public boolean contains(String key) {
        Jedis jedis = this.jedisPool.getResource();
        boolean exists = jedis.exists(serialize(key));
        jedis.close();
        return exists;
    }

    /**
     * 判断hset中是否存在key
     *
     * @param hset
     * @param key
     * @return
     */
    public boolean containsKeyInHset(String hset, String key) {
        Jedis jedis = this.jedisPool.getResource();
        Boolean hexists = jedis.hexists(serialize(hset), serialize(key));
        jedis.close();
        return hexists;
    }

    /**
     * 存入哈希中
     *
     * @param hset
     * @param key
     * @param value
     */
    public void hset(String hset, String key, Object value) {
        Jedis jedis = this.jedisPool.getResource();
        jedis.hset(serialize(hset), serialize(key), serialize(value));
        jedis.close();
    }

    public void hdel(String hset, String key) {
        Jedis jedis = this.jedisPool.getResource();
        jedis.hdel(serialize(hset), serialize(key));
        jedis.close();
    }

    /**
     * 清除key
     */
    public void del(String key) {
        Jedis jedis = this.jedisPool.getResource();
        jedis.del(serialize(key));
        jedis.close();
    }

    //序列化
    private byte[] serialize(Object o) {
        ObjectOutputStream obi = null;
        ByteArrayOutputStream bai = null;
        try {
            bai = new ByteArrayOutputStream();
            obi = new ObjectOutputStream(bai);
            obi.writeObject(o);
            byte[] byt = bai.toByteArray();
            return byt;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    //反序列化
    private Object deserialize(byte[] bytes) throws Throwable {
        ObjectInputStream oii = null;
        ByteArrayInputStream bis = null;
        bis = new ByteArrayInputStream(bytes);
        try {
            oii = new ObjectInputStream(bis);
            Object obj = oii.readObject();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
