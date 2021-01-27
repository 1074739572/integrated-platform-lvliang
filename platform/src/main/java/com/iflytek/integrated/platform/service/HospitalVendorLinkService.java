package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;

/**
* 厂商医院配置
* @author weihe9
* @date 2020/12/12 14:16
*/
@Service
public class HospitalVendorLinkService extends BaseService<THospitalVendorLink, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(HospitalVendorLinkService.class);

    public HospitalVendorLinkService(){
        super(qTHospitalVendorLink, qTHospitalVendorLink.id);
    }

    /**
     * 根据厂商配置表id获取厂商医院配置
     * @param vendorConfigId
     * @return
     */
    public List<THospitalVendorLink> getTHospitalVendorLinkByVendorConfigId(String vendorConfigId) {
        return sqlQueryFactory.select(qTHospitalVendorLink).from(qTHospitalVendorLink)
                .where(qTHospitalVendorLink.vendorConfigId.eq(vendorConfigId)).fetch();
    }

    /**
     * 删除厂商配置的关联医院信息
     * @param vendorConfigId
     */
    public long deleteByVendorConfigId(String vendorConfigId) {
        return sqlQueryFactory.delete(qTHospitalVendorLink)
                    .where(qTHospitalVendorLink.vendorConfigId.eq(vendorConfigId)).execute();
    }

    /**
     * 根据医院id获取厂商医院配置
     * @param hospitalId
     * @return
     */
    public List<THospitalVendorLink> getThvlListByHospitalId(String hospitalId) {
        List<THospitalVendorLink> list = sqlQueryFactory.select(qTHospitalVendorLink).from(qTHospitalVendorLink)
                .where(qTHospitalVendorLink.hospitalId.eq(hospitalId))
                .fetch();
        return list;
    }

}
