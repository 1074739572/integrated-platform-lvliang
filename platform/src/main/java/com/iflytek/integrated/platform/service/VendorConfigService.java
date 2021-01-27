package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.entity.TVendorConfig;
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
public class VendorConfigService extends BaseService<TVendorConfig, String, StringPath> {

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
     * 根据厂商获取所有厂商配置信息
     * @param vendorId
     * @return
     */
    public List<TVendorConfig> getObjByVendorId(String vendorId) {
        List<TVendorConfig> list = sqlQueryFactory.select(qTVendorConfig).from(qTVendorConfig)
                .where(qTVendorConfig.vendorId.eq(vendorId)).fetch();
        return list;
    }

    /**
     * 删除平台下所有厂商配置信息
     * @param platformId
     */
    public long delVendorConfigAll(String platformId) {
        return sqlQueryFactory.delete(qTVendorConfig).where(qTVendorConfig.platformId.eq(platformId)).execute();
    }


}
