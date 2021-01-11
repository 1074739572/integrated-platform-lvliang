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
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
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
     * 根据相同条件更改接口配置mock状态
     * @param id
     * @param mockStatus
     * @param loginUserName
     */
    public void updateMockStatus(String id, String mockStatus, String loginUserName) {
        //获取多接口，多个接口的id集合
        List<String> idList = busInterfaceIds(id);
        long size = sqlQueryFactory.update(qTBusinessInterface)
                .set(qTBusinessInterface.mockStatus, mockStatus)
                .set(qTBusinessInterface.updatedTime, new Date())
                .set(qTBusinessInterface.updatedBy, loginUserName)
            .where(qTBusinessInterface.id.in(idList)).execute();
        //判断编辑是否成功
        if(idList.size() != size){
            throw new RuntimeException("mock状态编辑失败");
        }
    }

    /**
     * 根据相同条件更改接口配置启停用状态
     * @param id
     * @param status
     * @param loginUserName
     */
    public void updateStatus(String id, String status, String loginUserName) {
        //获取多接口，多个接口的id集合
        List<String> idList = busInterfaceIds(id);
        long size = sqlQueryFactory.update(qTBusinessInterface)
                .set(qTBusinessInterface.status, status)
                .set(qTBusinessInterface.updatedTime, new Date())
                .set(qTBusinessInterface.updatedBy, loginUserName)
            .where(qTBusinessInterface.id.in(idList)).execute();
        //判断编辑是否成功
        if(idList.size() != size){
            throw new RuntimeException("启停用状态编辑失败");
        }
    }


    /**
     * 根据相同条件删除接口配置数据
     * @param productFunctionLinkId
     * @param interfaceId
     * @param vendorConfigId
     */
    public long delObjByCondition(String productFunctionLinkId, String interfaceId, String vendorConfigId) {
        long count = sqlQueryFactory.delete(qTBusinessInterface)
                .where(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId)
                        .and(qTBusinessInterface.interfaceId.eq(interfaceId)
                        .and(qTBusinessInterface.vendorConfigId.eq(vendorConfigId))))
                .execute();
        return count;
    }

    /**
     * 根据相同条件查询所有接口配置数据
     * @param productFunctionLinkId
     * @param interfaceId
     * @param vendorConfigId
     */
    public List<TBusinessInterface> getListByCondition(String productFunctionLinkId, String interfaceId, String vendorConfigId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                                        .where(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId)
                                                .and(qTBusinessInterface.interfaceId.eq(interfaceId)
                                                        .and(qTBusinessInterface.vendorConfigId.eq(vendorConfigId))))
                                        .fetch();
        return list;
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
     * 根据标准接口id获取接口配置id(获取标准接口详情使用)
     * @param interfaceId
     */
    public List<TBusinessInterface> getListByInterfaceId(String interfaceId) {
        return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .where(qTBusinessInterface.interfaceId.eq(interfaceId)).fetch();
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
//                .orderBy(qTBusinessInterface.createdTime.desc())
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
                .orderBy(qTBusinessInterface.createdTime.desc())
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
            list.add(qTPlatform.projectId.in(projectId));
        }

        List<TBusinessInterface> rtnList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .join(qTHospitalVendorLink).on(qTHospitalVendorLink.vendorConfigId.eq(qTBusinessInterface.vendorConfigId))
                .join(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(qTBusinessInterface.productFunctionLinkId))
                .join(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
                .join(qTPlatform).on(qTPlatform.id.eq(qTVendorConfig.platformId))
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


    /**
     * 获取平台下的所有接口配置信息
     * @param platformId
     */
    public List<TBusinessInterface> getListByPlatform(String platformId) {
        List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
                .leftJoin(qTVendorConfig).on(qTBusinessInterface.vendorConfigId.eq(qTVendorConfig.id))
                .where(qTVendorConfig.platformId.eq(platformId))
                .fetch();
        return list;
    }


    /**
     * 根据id，获取id的list
     * @return
     */
    private List<String> busInterfaceIds(String id){
        TBusinessInterface businessInterface = getOne(id);
        if(businessInterface == null){
            throw new RuntimeException("没有找到接口配置");
        }
        ArrayList<Predicate> list = new ArrayList<>();
        if(StringUtils.isNotBlank(businessInterface.getInterfaceId())){
            list.add(qTBusinessInterface.interfaceId.eq(businessInterface.getInterfaceId()));
        }
        if(StringUtils.isNotBlank(businessInterface.getProductFunctionLinkId())){
            list.add(qTBusinessInterface.productFunctionLinkId.eq(businessInterface.getProductFunctionLinkId()));
        }
        if(StringUtils.isNotBlank(businessInterface.getVendorConfigId())){
            list.add(qTBusinessInterface.vendorConfigId.eq(businessInterface.getVendorConfigId()));
        }
        //获取id集合
        List<String> idList = sqlQueryFactory.select(qTBusinessInterface.id)
                .from(qTBusinessInterface).where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isEmpty(idList)){
            throw new RuntimeException("没有找到多接口配置id集合");
        }
        return idList;
    }

}
