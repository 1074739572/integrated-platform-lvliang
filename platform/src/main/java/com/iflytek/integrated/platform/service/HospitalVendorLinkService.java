package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
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
public class HospitalVendorLinkService extends QuerydslService<THospitalVendorLink, String, THospitalVendorLink, StringPath, PageRequest<THospitalVendorLink>> {

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
    public void deleteByVendorConfigId(String vendorConfigId) {
        sqlQueryFactory.delete(qTHospitalVendorLink)
                .where(qTHospitalVendorLink.vendorConfigId.eq(vendorConfigId)).execute();
    }

}
