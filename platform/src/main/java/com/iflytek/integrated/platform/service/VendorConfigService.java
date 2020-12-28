package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TVendorConfig;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;

/**
* 厂商配置
* @author weihe9
* @date 2020/12/14 10:58
*/
@Service
public class VendorConfigService extends QuerydslService<TVendorConfig, String, TVendorConfig, StringPath, PageRequest<TVendorConfig>> {

    private static final Logger logger = LoggerFactory.getLogger(VendorConfigService.class);

    public VendorConfigService(){
        super(qTVendorConfig, qTVendorConfig.id);
    }

    /**
     * 获取平台-厂商配置
     * @param platformId
     * @param vendorId
     * @return
     */
    public TVendorConfig getObjByPlatformAndVendor(String platformId, String vendorId) {
        return sqlQueryFactory.select(qTVendorConfig).from(qTVendorConfig).
                where(qTVendorConfig.platformId.eq(platformId).and(qTVendorConfig.vendorId.eq(vendorId))).fetchFirst();
    }

    /**
     * 获取平台-所有厂商配置
     * @param platformId
     * @return
     */
    public List<TVendorConfig> getObjByPlatformId(String platformId) {
        return sqlQueryFactory.select(qTVendorConfig).from(qTVendorConfig).
                where(qTVendorConfig.platformId.eq(platformId)).fetch();
    }

    /**
     * 删除平台下的某厂商配置信息
     * @param platformId
     * @param vendorId
     */
    public void delVendorConfig(String platformId, String vendorId) {
        sqlQueryFactory.delete(qTVendorConfig)
                .where(qTVendorConfig.platformId.eq(platformId).and(qTVendorConfig.vendorId.eq(vendorId)))
                .execute();
    }

    /**
     * 删除平台下所有厂商配置信息
     * @param platformId
     */
    public void delVendorConfigAll(String platformId) {
        sqlQueryFactory.delete(qTVendorConfig).where(qTVendorConfig.platformId.eq(platformId)).execute();
    }

}
