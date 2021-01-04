package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;
import static com.querydsl.sql.SQLExpressions.groupConcat;

/**
* 对接接口配置
* @author weihe9
* @date 2020/12/13 20:40
*/
@Service
public class BusinessInterfaceService extends QuerydslService<TBusinessInterface, String, TBusinessInterface, StringPath, PageRequest<TBusinessInterface>> {

    private static final Logger logger = LoggerFactory.getLogger(BusinessInterfaceService.class);

    public BusinessInterfaceService(){
        super(qTBusinessInterface, qTBusinessInterface.id);
    }


    /**
     *  更改mock状态
     * @param id
     * @param mockStatus
     */
    public void updateMockStatus(String id, String mockStatus, String loginUserName) {
        sqlQueryFactory.update(qTBusinessInterface)
                .set(qTBusinessInterface.mockStatus, mockStatus)
                .set(qTBusinessInterface.updatedTime, new Date())
                .set(qTBusinessInterface.updatedBy, loginUserName)
            .where(qTBusinessInterface.id.eq(id)).execute();
    }

    /**
     *  更改启停用状态
     * @param id
     * @param status
     */
    public void updateStatus(String id, String status, String loginUserName) {
        sqlQueryFactory.update(qTBusinessInterface)
                .set(qTBusinessInterface.status, status)
                .set(qTBusinessInterface.updatedTime, new Date())
                .set(qTBusinessInterface.updatedBy, loginUserName)
            .where(qTBusinessInterface.id.eq(id)).execute();
    }

    /**
     *  保存mock模板
     * @param id
     * @param mockTemplate
     */
    public void saveMockTemplate(String id, String mockTemplate, String loginUserName) {
        sqlQueryFactory.update(qTBusinessInterface)
                .set(qTBusinessInterface.mockTemplate, mockTemplate)
                .set(qTBusinessInterface.updatedTime, new Date())
                .set(qTBusinessInterface.updatedBy, loginUserName)
            .where(qTBusinessInterface.id.eq(id)).execute();
    }

    /**
     * 根据标准接口id获取产品id(获取标准接口详情使用)
     * @param interfaceId
     */
    public TBusinessInterface getProductIdByInterfaceId(String interfaceId) {
        return sqlQueryFactory.select(Projections.bean(qTBusinessInterface, qTProductFunctionLink.productId.as("productId")))
                .from(qTBusinessInterface)
                .leftJoin(qTProductFunctionLink).on(qTBusinessInterface.productFunctionLinkId.eq(qTProductFunctionLink.id))
                .where(qTBusinessInterface.interfaceId.eq(interfaceId)).fetchFirst();
    }

    /**
     * 获取接口配置信息列表
     * @param platformId
     * @param status
     * @param mockStatus
     * @param pageNo
     * @param pageSize
     * @return
     */
//    public QueryResults<TBusinessInterface> getInterfaceConfigureList(String platformId, String status, String mockStatus, Integer pageNo, Integer pageSize) {
//        ArrayList<Predicate> list = new ArrayList<>();
//        list.add(qTVendorConfig.platformId.eq(platformId));
//        if(StringUtils.isNotEmpty(status)) {
//            list.add(qTBusinessInterface.status.eq(status));
//        }
//        if(StringUtils.isNotEmpty(mockStatus)) {
//            list.add(qTBusinessInterface.mockStatus.eq(mockStatus));
//        }
//        QueryResults<TBusinessInterface> queryResults = sqlQueryFactory.select(
//                Projections.bean(TBusinessInterface.class, qTBusinessInterface.id, qTBusinessInterface.productFunctionLinkId,
//                        qTBusinessInterface.interfaceId, qTBusinessInterface.vendorConfigId, qTBusinessInterface.businessInterfaceName,
//                        qTBusinessInterface.requestType, qTBusinessInterface.requestConstant, qTBusinessInterface.interfaceType,
//                        qTBusinessInterface.pluginId, qTBusinessInterface.frontInterface, qTBusinessInterface.afterInterface,
//                        qTBusinessInterface.inParamFormat, qTBusinessInterface.inParamSchema, qTBusinessInterface.inParamTemplate,
//                        qTBusinessInterface.inParamFormatType, qTBusinessInterface.outParamFormat, qTBusinessInterface.outParamSchema,
//                        qTBusinessInterface.outParamTemplate, qTBusinessInterface.outParamFormatType, qTBusinessInterface.mockStatus,
//                        qTBusinessInterface.status, qTBusinessInterface.createdBy, qTBusinessInterface.createdTime,
//                        qTBusinessInterface.updatedBy, qTBusinessInterface.updatedTime, qTVendorConfig.versionId.as("versionId")))
//                .from(qTBusinessInterface)
//                .leftJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
//                .where(list.toArray(new Predicate[list.size()]))
//                .orderBy(qTBusinessInterface.updatedTime.desc())
//                .limit(pageSize)
//                .offset((pageNo - 1) * pageSize)
//                .fetchResults();
//        return queryResults;
//    }

    /**
     * 获取接口配置信息列表
     * @param platformId
     * @param status
     * @param mockStatus
     * @param pageNo
     * @param pageSize
     * @return
     */
    public QueryResults<TBusinessInterface> getInterfaceConfigureList(String platformId, String status, String mockStatus, Integer pageNo, Integer pageSize) {
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTVendorConfig.platformId.eq(platformId));
        if(StringUtils.isNotEmpty(status)) {
            list.add(qTBusinessInterface.status.eq(status));
        }
        if(StringUtils.isNotEmpty(mockStatus)) {
            list.add(qTBusinessInterface.mockStatus.eq(mockStatus));
        }
        QueryResults<TBusinessInterface> queryResults = sqlQueryFactory.select(
                Projections.bean(TBusinessInterface.class, qTBusinessInterface.id, qTBusinessInterface.productFunctionLinkId,
                        qTBusinessInterface.interfaceId, qTBusinessInterface.vendorConfigId,
                        groupConcat(qTBusinessInterface.businessInterfaceName).as("businessInterfaceName"),
                        qTBusinessInterface.requestType, qTBusinessInterface.requestConstant, qTBusinessInterface.interfaceType,
                        qTBusinessInterface.pluginId, qTBusinessInterface.inParamFormat, qTBusinessInterface.inParamSchema,
                        qTBusinessInterface.inParamTemplate, qTBusinessInterface.inParamFormatType, qTBusinessInterface.outParamFormat, qTBusinessInterface.outParamSchema,
                        qTBusinessInterface.outParamTemplate, qTBusinessInterface.outParamFormatType, qTBusinessInterface.mockStatus,
                        qTBusinessInterface.status, qTBusinessInterface.createdBy, qTBusinessInterface.createdTime,
                        qTBusinessInterface.updatedBy, qTBusinessInterface.updatedTime, qTVendorConfig.versionId.as("versionId")))
                .from(qTBusinessInterface)
                .leftJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
                .where(list.toArray(new Predicate[list.size()]))
                .groupBy(qTBusinessInterface.productFunctionLinkId,qTBusinessInterface.interfaceId,qTBusinessInterface.vendorConfigId)
                .orderBy(qTBusinessInterface.updatedTime.desc())
                .limit(pageSize)
                .offset((pageNo - 1) * pageSize)
                .fetchResults();
        return queryResults;
    }

    /**
     * 根据三条件获取
     * @param productFunctionLinkId
     * @param interfaceId
     * @param vendorConfigId
     * @return
     */
    public List<TBusinessInterface> getTBusinessInterfaceList(String productFunctionLinkId, String interfaceId, String vendorConfigId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                                        .where(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId)
                                                .and(qTBusinessInterface.interfaceId.eq(interfaceId)
                                                .and(qTBusinessInterface.vendorConfigId.eq(vendorConfigId))))
                                        .fetch();
        return list;
    }

    /**
     * 新增接口配置时根据条件判断是否存在该数据
     * @param thvlList
     * @param projectId
     * @param productId
     * @param interfaceId
     * @return
     */
    public List<TBusinessInterface> getBusinessInterfaceIsExist(List<THospitalVendorLink> thvlList, String projectId, String productId, String interfaceId) {
        ArrayList<Predicate> list = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(thvlList)) {
            List<String> hospitalIds = new ArrayList<>();
            for (THospitalVendorLink obj : thvlList) {
                hospitalIds.add(obj.getHospitalId());
            }
            list.add(qTHospitalVendorLink.hospitalId.in(hospitalIds));
        }
        if(StringUtils.isNotEmpty(interfaceId)){
            list.add(qTBusinessInterface.interfaceId.eq(interfaceId));
        }
        if(StringUtils.isNotEmpty(productId)){
            list.add(qTProductFunctionLink.productId.eq(productId));
        }
        if(StringUtils.isNotEmpty(projectId)){
//            list.add(qTInterfaceMonitor.projectId.in(projectId));
            list.add(qTProjectProductLink.projectId.in(projectId));
        }

        List<TBusinessInterface> rtnList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .join(qTHospitalVendorLink).on(qTHospitalVendorLink.vendorConfigId.eq(qTBusinessInterface.vendorConfigId))
                .join(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(qTBusinessInterface.productFunctionLinkId))
//                .join(qTInterfaceMonitor).on(qTInterfaceMonitor.productFunctionLinkId.eq(qTProductFunctionLink.id))
                .join(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTBusinessInterface.productFunctionLinkId))
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return rtnList;
    }


    /**
     * 根据产品功能关联表id获取对接接口配置数据
     * @param productFunctionLinkId
     * @return
     */
    public List<TBusinessInterface> getListByProductFunctionLinkId(String productFunctionLinkId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId))
                .fetch();
        return list;
    }


    /**
     * 根据插件表id获取对接接口配置数据
     * @param pluginId
     * @return
     */
    public List<TBusinessInterface> getListByPluginId(String pluginId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.pluginId.eq(pluginId))
                .fetch();
        return list;
    }


    /**
     * 根据厂商配置id获取对接接口配置数据
     * @param vendorConfigId
     * @return
     */
    public List<TBusinessInterface> getListByVendorConfigId(String vendorConfigId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.vendorConfigId.eq(vendorConfigId))
                .fetch();
        return list;
    }

}
