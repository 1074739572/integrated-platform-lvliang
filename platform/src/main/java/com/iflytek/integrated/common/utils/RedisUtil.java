package com.iflytek.integrated.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
* redid 工具类
 * @author
*/
@Component
public class RedisUtil<T> {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * HashSet
     *
     * @param key 键
     * @param id 对应的hash键kev
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
     *
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
     *
     * @param key  键 不能为null
     * @return 值
     */
    public Map<Object, Object> hmGet(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

}
