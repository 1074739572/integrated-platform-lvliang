package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    protected SQLQueryFactory sqlQueryFactory;


    /**
     * 删除key操作
     * @param ids
     * @param keyName
     */
    public void delRedisKey(String ids, String keyName) {
        logger.info("执行清除关联缓存");

        List<String> conditionList = Arrays.asList(ids.split(","));
        ArrayList<Predicate> arr = new ArrayList<>();

        //调取枚举，处理返回结果
        StringPath sqlId = Constant.RedisMap.idByKey(keyName);
        if(sqlId != null){
            arr.add(sqlId.in(conditionList));
        }
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
                String key = obj.getProjectCode()+"_"+obj.getOrgId()+"_"+obj.getProductCode()+"_"+obj.getFunCode();
                redisUtil.hmDel("IntegratedPlatform:Configs:", key);
            }
        }
        logger.info("缓存删除结束，删除数量{}",list.size());
    }


}
