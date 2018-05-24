package com.cohen.test.configuration;

import com.cohen.redis.RedisCacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 先入先出缓存策略测试
 */
public class MyConfiguration {

    private Map<String, Object> context = new HashMap<>();// 容器
    private Map<String, Class> beanNameClassMap = new HashMap<>();// 实例
    private Map<String, Method> beanNameMethodMap = new HashMap<>();// bean的类型以及对应的方法
    private Map<String, List<String>> onMissingBeansMap = new HashMap<>();// 当前bean被创建时需要容器不存在的类型
    private Map<String, List<String>> dependencesMap = new HashMap<>();// 当前bean被创建时依赖的类型
    private Map<String, List<String>> beDependentMap = new HashMap<>();// 依赖于当前bean的类型

    public MyConfiguration(Class<?> clazz) {
        try {
            this.init(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(Class<?> clazz) throws Exception {
        this.loadConfiguration(clazz);// 加载配置关系
        this.createBeans();// 根据配置关系创建bean
    }

    /**
     * 加载JavaConfig方式配置的bean
     */
    public void loadConfiguration(Class<?> clazz) {
        if (clazz.getAnnotation(org.springframework.context.annotation.Configuration.class) == null) {// 判断是否为配置类, 不是直接返回, 是配置类则处理
            return;
        }
        // 读取配置类中引入的配置属性类
        EnableConfigurationProperties cps;
        if ((cps = clazz.getAnnotation(EnableConfigurationProperties.class)) != null) {
            for (Class<?> prop : cps.value()) {
                String name = this.getInstanceName(prop);
                if (!beanNameClassMap.containsValue(prop)) {
                    beanNameClassMap.put(name, prop);
                }
            }
        }
        // 加载JavaConfig方式配置的bean属性
        for (Method method : clazz.getDeclaredMethods()) {
            Bean bean = null;
            if ((bean = method.getAnnotation(Bean.class)) != null) {// 获取所有被@Bean标记的方法
                String beanName = this.getInstanceName(method.getReturnType());
                if (!beanNameClassMap.containsValue(method.getReturnType())) {
                    beanNameClassMap.put(beanName, method.getReturnType());
                }
                if (!beanNameMethodMap.containsValue(method)) {
                    beanNameMethodMap.put(beanName, method);
                }
                ConditionalOnMissingBean missingBean;
                if ((missingBean = method.getAnnotation(ConditionalOnMissingBean.class)) != null) {// 标记了@ConditionalOnMissingBean 注解
                    if (missingBean.value() != null) {
                        if (onMissingBeansMap.get(beanName) == null) {
                            onMissingBeansMap.put(beanName, new ArrayList<>());
                        }
                        for (Class<?> aClass : missingBean.value()) {// 遍历@ConditionalOnMissingBean 标注的value
                            onMissingBeansMap.get(beanName).add(this.getInstanceName(aClass));
                        }
                    }
                }
                ConditionalOnBean onBean;
                if ((onBean = method.getAnnotation(ConditionalOnBean.class)) != null) {
                    if (dependencesMap.get(beanName) == null) {
                        dependencesMap.put(beanName, new ArrayList<>());
                    }
                    for (Class<?> aClass : onBean.value()) {// 遍历@ConditionalOnBean 标注的value
                        if (beDependentMap.get(this.getInstanceName(aClass)) == null) {
                            beDependentMap.put(this.getInstanceName(aClass), new ArrayList<>());
                        }
                        dependencesMap.get(beanName).add(this.getInstanceName(aClass));
                        beDependentMap.get(this.getInstanceName(aClass)).add(beanName);
                    }
                }
            }
        }
    }

    /**
     * 根据加载之后的配置关系，创建bean实例
     */
    public void createBeans() throws Exception {
        for (Map.Entry<String, Class> entry : this.beanNameClassMap.entrySet()) {
            String beanName = entry.getKey();// bean名称
            Class<?> type = entry.getValue();// bean类型
            if (this.onMissingBeansMap.get(beanName) != null) {// 如果onMissingBeansMap不为空, 先判断是否容器中已经存在了指定不能存在的bean
                for (String missingBean : this.onMissingBeansMap.get(beanName)) {
                    if (this.context.containsKey(missingBean)) {
                        continue;
                    }
                }
                // 创建bean
            }
        }
    }

    /**
     * 递归创建bean
     */
    private void create(String beanName, Class<?> beanType) {

    }

    private String getInstanceName(Class<?> clazz) {
        String[] strings = clazz.getDeclaringClass().getName().split("\\.");
        String className = strings[strings.length - 1];
        return className.substring(0, 1).toLowerCase() + className.substring(1, className.toCharArray().length);
    }

    public static void main(String[] args) throws Exception {

    }
}
