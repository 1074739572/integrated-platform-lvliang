package com.iflytek.integrated.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
* redis 工具类
 * @author
*/
@Component
public class RedisUtil<T> {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * HashSet
     * @param key 键
     * @param id 对应的hash键key
     * @param t 对应的键value
     * @return true 成功 false 失败
     */
    public boolean hmSet(String key, String id, T t) {
        try {
            redisTemplate.opsForHash().put(key, id, t);
            return true;
        } catch (Exception e) {
            logger.error("redis存储出现异常；异常信息：" + e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public Boolean hmDel(String key, Object... item) {
        try {
            Long lon = redisTemplate.opsForHash().delete(key, item);
            if(lon > 0){
                return true;
            }
        }
        catch (Exception e){
        }
        return false;
    }

    /**
     * hmGet
     * @param key  键 不能为null
     * @return 值
     */
    public Map<Object, Object> hmGet(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 普通缓存放入
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("redis存储出现异常；异常信息：" + e);
            return false;
        }
    }

    /**
     * 指定缓存失效时间
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            logger.error("redis存储出现异常；异常信息：" + e);
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

}
