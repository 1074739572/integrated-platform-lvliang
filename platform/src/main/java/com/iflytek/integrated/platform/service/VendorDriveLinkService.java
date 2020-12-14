package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TVendorDriveLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTVendorDriveLink.qTVendorDriveLink;
/**
* 厂商与驱动关联
* @author weihe9
* @date 2020/12/12 14:16
*/
@Service
public class VendorDriveLinkService extends QuerydslService<TVendorDriveLink, String, TVendorDriveLink, StringPath, PageRequest<TVendorDriveLink>> {

    private static final Logger logger = LoggerFactory.getLogger(VendorDriveLinkService.class);

    public VendorDriveLinkService(){
        super(qTVendorDriveLink, qTVendorDriveLink.id);
    }

    /**
     * 根据厂商id删除厂商与驱动关联信息
     * @param id
     */
    public void deleteVendorDriveLinkById(String id) {
        sqlQueryFactory.delete(qTVendorDriveLink).where(qTVendorDriveLink.vendorId.eq(id)).execute();
    }

    /**
     * 根据厂商id获取厂商驱动关联
     * @param vendorId
     * @return
     */
    public List<TVendorDriveLink> getVendorDriveLinkByVendorId(String vendorId) {
        return sqlQueryFactory.select(Projections.bean(qTVendorDriveLink,qTDrive.driveName.as("driveName")))
                .from(qTVendorDriveLink)
                .leftJoin(qTDrive).on(qTDrive.id.eq(qTVendorDriveLink.driveId))
                .where(qTVendorDriveLink.vendorId.eq(vendorId)).fetch();
    }

}
