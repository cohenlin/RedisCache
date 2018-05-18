package com.cohen.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在方法上，表示此方法执行后，清空相关namespace的redis缓存
 *
 * @author 林金成
 * @date 2018/5/1613:14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisCleared {
}
