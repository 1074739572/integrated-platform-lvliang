package com.iflytek.integrated.platform.common;

import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;
import static com.iflytek.integrated.platform.entity.QTVendorDriveLink.qTVendorDriveLink;

/**
* redis缓存操作
* @author weihe9
* @date 2021/1/22 10:01
*/
@Service
@ApiIgnore
public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    protected SQLQueryFactory sqlQueryFactory;

    /**
     * 开启新线程执行清除缓存
     */
    private static final ThreadPoolExecutor THREAD_POOL_CLEAR_REDIS = new ThreadPoolExecutor(
            4, 8, 10, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(8),new ThreadPoolExecutor.AbortPolicy());

    /**
     * 删除key
     * @param code
     * @param ids
     * @param keyName
     */
    public void delRedisKey(Integer code, String ids, String keyName) {
        //调用新线程处理redis缓存
        if(Constant.ResultCode.SUCCESS_CODE == code){
            THREAD_POOL_CLEAR_REDIS.execute(
                    ()->threadDelRedisKey(ids, keyName)
            );
        }
    }

    /**
     * 删除key操作
     * @param ids
     * @param keyName
     */
    private void threadDelRedisKey(String ids, String keyName){
        if(StringUtils.isBlank(ids) || StringUtils.isBlank(keyName)){
            logger.error("删除redis-key操作,没有取到ids或keyName");
            return;
        }
        List<String> conditionList = Arrays.asList(ids.split(","));
        ArrayList<Predicate> arr = new ArrayList<>();

        //调取枚举，处理返回结果
        StringPath sqlId = Constant.RedisMap.idByKey(keyName);
        if(sqlId == null){
            logger.error("删除redis-key操作,没有取到有效的key");
            return;
        }
        arr.add(sqlId.in(conditionList));
        arr.add(qTProject.projectCode.isNotNull().and(qTProduct.productCode.isNotNull().and(qTInterface.interfaceUrl.isNotNull())));
        arr.add(qTProject.projectStatus.eq(Constant.Status.START).and(qTPlatform.platformStatus.eq(Constant.Status.START)));

        List<RedisKeyDto> list =
                sqlQueryFactory.select(Projections.bean(RedisKeyDto.class, qTProject.projectCode.as("projectCode"),
                        qTHospital.hospitalCode.as("orgId"), qTProduct.productCode.as("productCode"), qTInterface.interfaceUrl.as("funCode")))
                        .from(qTProject)
                        .leftJoin(qTPlatform).on(qTPlatform.projectId.eq(qTProject.id))
                        .leftJoin(qTVendorConfig).on(qTVendorConfig.platformId.eq(qTPlatform.id))
                        .leftJoin(qTHospitalVendorLink).on(qTHospitalVendorLink.vendorConfigId.eq(qTVendorConfig.id))
                        .leftJoin(qTHospital).on(qTHospital.id.eq(qTHospitalVendorLink.hospitalId))
                        .leftJoin(qTProjectProductLink).on(qTProjectProductLink.projectId.eq(qTProject.id))
                        .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(qTProjectProductLink.productFunctionLinkId))
                        .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                        .leftJoin(qTProductInterfaceLink).on(qTProductInterfaceLink.productId.eq(qTProduct.id))
                        .leftJoin(qTInterface).on(qTInterface.id.eq(qTProductInterfaceLink.interfaceId))
                        .leftJoin(qTBusinessInterface).on(qTBusinessInterface.vendorConfigId.eq(qTVendorConfig.id))
                        .leftJoin(qTVendorDriveLink).on(qTVendorDriveLink.vendorId.eq(qTVendorConfig.vendorId))
                        .where(arr.toArray(new Predicate[arr.size()]))
                        .groupBy(qTProject.projectCode, qTHospital.hospitalCode, qTProduct.productCode, qTInterface.interfaceUrl)
                        .fetch();
        if (CollectionUtils.isNotEmpty(list)) {
            for (RedisKeyDto obj : list) {
                String key = "IntegratedPlatform:Configs:_"+obj.getProjectCode()+"_"+obj.getOrgId()+"_"+obj.getProductCode()+"_"+obj.getFunCode();
                redisUtil.del(key);
            }
            logger.info("删除缓存{}条",list.size());
        }
    }
}
