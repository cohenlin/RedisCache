package com.cohen.test.cache;

import com.cohen.redis.RedisCacheAutoConfiguration;
import com.cohen.redis.assembly.cache.RedisCache;
import com.cohen.redis.property.JedisPoolProperty;
import com.cohen.redis.property.RedisCacheProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 先入先出缓存策略测试
 */
public class Configuration {

    private Map<String, Object> context = new HashMap<>();// 容器(beanName为key)
    private Map<String, Class> beanNameClassMap = new HashMap<>();// beanName : 类型
    private Map<Class, String> beanClassNameMap = new HashMap<>();// 类型 : beanName
    private Map<String, List<Class<?>>> beanNameParamMap = new HashMap<>();// beanName : 参数
    private Map<String, Method> beanNameMethodMap = new HashMap<>();// beanName : 方法
    private Map<String, List<String>> onMissingBeansMap = new HashMap<>();// 当前bean被创建时需要容器不存在的类型
    private Map<String, List<String>> dependencesMap = new HashMap<>();// 当前bean被创建时依赖的类型
    private Map<String, List<String>> beDependentMap = new HashMap<>();// 依赖于当前bean的类型
    private Object obj = null;

    public Configuration(Class<?> clazz) throws Exception {
        this.init(clazz);
    }

    public Configuration(Class<?> clazz, RedisCacheProperty redisCacheProperty) throws Exception {
        this.context.put("redisCacheProperty", redisCacheProperty);
        this.init(clazz);
    }

    public Configuration(Class<?> clazz, JedisPoolProperty jedisPoolProperty) throws Exception {
        this.context.put("jedisPoolProperty", jedisPoolProperty);
        this.init(clazz);
    }

    public Configuration(Class<?> clazz, RedisCacheProperty redisCacheProperty, JedisPoolProperty jedisPoolProperty) throws Exception {
        this.context.put("redisCacheProperty", redisCacheProperty);
        this.context.put("jedisPoolProperty", jedisPoolProperty);
        this.init(clazz);
    }

    public <T> T getBean(Class<T> clazz) {
        return clazz.cast(this.context.get(this.beanClassNameMap.get(clazz)));
    }

    public Object getBean(String name) {
        return this.context.get(name);
    }

    private void init(Class<?> clazz) throws Exception {
        this.loadConfiguration(clazz);// 加载配置关系
        this.createBeans();// 根据配置关系创建bean
    }

    /**
     * 加载JavaConfig方式配置的bean
     */
    private void loadConfiguration(Class<?> clazz) throws Exception {
        // 判断是否为配置类, 不是直接返回, 是配置类则处理
        if (clazz.getAnnotation(org.springframework.context.annotation.Configuration.class) == null) {
            return;
        }

        this.obj = clazz.newInstance();

        // 读取配置类中引入的配置属性类
        EnableConfigurationProperties cps;
        if ((cps = clazz.getAnnotation(EnableConfigurationProperties.class)) != null) {
            if (cps.value() != null) {
                for (Class<?> prop : cps.value()) {
                    String name = this.getInstanceName(prop);
                    if (!beanNameClassMap.containsValue(prop)) {
                        beanNameClassMap.put(name, prop);
                        beanClassNameMap.put(prop, name);
                    }
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
                    beanClassNameMap.put(method.getReturnType(), beanName);
                }
                // 统计beanName和对应方法
                if (!beanNameMethodMap.containsValue(method)) {
                    beanNameMethodMap.put(beanName, method);
                }
                // 统计当前bean需要的参数
                if (this.beanNameParamMap.get(beanName) == null) {
                    this.beanNameParamMap.put(beanName, new ArrayList<>());
                }
                for (Parameter parameter : method.getParameters()) {
                    this.beanNameParamMap.get(beanName).add(parameter.getType());
                }
                // 统计missingBean条件
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
                // 统计onBean条件
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
    private void createBeans() throws Exception {
        for (Map.Entry<String, Class> entry : this.beanNameClassMap.entrySet()) {
            String beanName = entry.getKey();// bean名称
            Class<?> beanType = entry.getValue();// bean类型
            if (!this.create(beanName, beanType)) {// 如果创建失败, 抛出异常
                throw new RuntimeException("Error create bean with name: " + beanName + " and type: " + beanType + " !");
            }
        }
    }

    /**
     * 递归创建bean
     */
    private boolean create(String beanName, Class<?> beanType) throws Exception {

        if (this.context.containsKey(beanName)) {// 如果容器中存在, 返回true
            return true;
        }

        // 判断onMissingBean条件, 不满足则返回
        if (this.onMissingBeansMap.get(beanName) != null) {
            for (String missingBean : this.onMissingBeansMap.get(beanName)) {
                if (this.context.containsKey(missingBean)) {
                    return false;
                }
            }
        }
        // 判断onBean条件, 不满足则先去创建
        if (this.dependencesMap.get(beanName) != null) {
            for (String dependence : this.dependencesMap.get(beanName)) {
                if (!this.context.containsKey(dependence)) {// 当前容器没有这个bean, 先去创建
                    if (!this.create(dependence, this.beanNameClassMap.get(dependence))) {
                        return false;
                    }
                }
            }
        }
        // 判断所需参数, 不存在则创建
        if (this.beanNameParamMap.get(beanName) != null) {
            for (Class<?> paramType : this.beanNameParamMap.get(beanName)) {
                String paramName = this.beanClassNameMap.get(paramType);// 获得参数名
                if (!this.context.containsKey(paramName)) {// 容器中不存在这个参数对象, 创建
                    if (!this.create(paramName, paramType)) {
                        return false;
                    }
                }
            }
        }
        // 创建bean
        if ((!this.context.containsKey(beanName))) {// 容器中没有才创建
            if (this.beanNameMethodMap.containsKey(beanName)) {
                Method method = this.beanNameMethodMap.get(beanName);
                List<Object> params = new ArrayList<>();
                for (Class<?> aClass : this.beanNameParamMap.get(beanName)) {
                    params.add(this.context.get(this.beanClassNameMap.get(aClass)));
                }
                this.context.put(beanName, beanType.cast(method.invoke(this.obj, params.toArray())));
            } else {
                this.context.put(beanName, beanType.newInstance());
            }
        }
        return true;
    }

    /**
     * 获取bean的名称, 默认为类名首字母小写
     */
    private String getInstanceName(Class<?> clazz) {
        String[] strings = clazz.getName().split("\\.");
        String className = strings[strings.length - 1];
        return className.substring(0, 1).toLowerCase() + className.substring(1, className.toCharArray().length);
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration(RedisCacheAutoConfiguration.class);
        RedisCache redisCache = configuration.getBean(RedisCache.class);
    }
}