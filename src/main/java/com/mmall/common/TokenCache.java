package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Sunsongoing
 */

public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    /**
     * 使用guava本地缓存
     */
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder()
            //缓存的初始化容量，默认值是16
            .initialCapacity(1000)
            //LRU 缓存淘汰算法
            // 缓存的最大容量，当超过的时候将使用LRU算法（最近最少使用原则）来移除缓存
            .maximumSize(10000)
            //有效期 ,第二个参数是单位
            .expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，调用get取值的时候，如果没有对应的值，就调用这个方法进行加载
                @Override
                public String load(String s) throws Exception {
                    //返回字符串null 是为了防止比较的时候抛出空指针异常
                    return "null";
                }
            });


    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public static String getKey(String key) {
        String value = null;
        String nu = "null";
        try {
            value = localCache.get(key);
            if (nu.equals(value)) {
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localCache get error", e);
        }
        return null;
    }

    /**
     * 存储缓存
     * @param key
     * @param value
     */
    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }
}
