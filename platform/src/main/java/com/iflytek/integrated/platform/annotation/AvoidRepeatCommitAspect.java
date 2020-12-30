package com.iflytek.integrated.platform.annotation;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.utils.IpUtils;
import com.iflytek.medicalboot.core.id.BatchUidService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @description: 防止表单重复提交切面
 * @author:
 */
@Aspect
@Component
public class AvoidRepeatCommitAspect {
    private Logger logger = LoggerFactory.getLogger(AvoidRepeatCommitAspect.class);
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private BatchUidService batchUidService;

    @Pointcut("@annotation(com.iflytek.integrated.platform.annotation.AvoidRepeatCommit)")
    public void pointcutAvoidRepeatCommit() {

    }

    @Around("pointcutAvoidRepeatCommit()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = IpUtils.getUserIp(request);
        String userId = "";
        //获取注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        //目标类、方法
        String className = method.getDeclaringClass().getName();
        String name = method.getName();
        String ipKey = String.format("%s#%s", className, name);
        long hashCode = Math.abs((long) ipKey.hashCode());
        String key = String.format("%s_%d", ip + "_" + userId, hashCode);
        logger.info("ipKey={},hashCode={},key={}", ipKey, hashCode, key);
        AvoidRepeatCommit avoidRepeatableCommit = method.getAnnotation(AvoidRepeatCommit.class);
        long timeout = avoidRepeatableCommit.timeout();
        if (timeout < 0) {
            timeout = 5000;
        }
        Long value = (Long) redisTemplate.opsForValue().get(key);
        if (value != null) {
            logger.info("接口正在处理，请稍后！");
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "操作较频繁,请休息一会再点", "休息一会再来");
        }
        redisTemplate.opsForValue().set(key, batchUidService.getUid("avoidRepeat"), timeout, TimeUnit.MILLISECONDS);
        //执行方法
        return point.proceed();
    }
}
