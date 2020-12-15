package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Date;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;

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
    public void updateMockStatus(String id, String mockStatus) {
        sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.status, mockStatus).set(qTBusinessInterface.updatedTime, new Date())
                .where(qTBusinessInterface.id.eq(id)).execute();
    }

    /**
     *  保存mock模板
     * @param id
     * @param mockTemplate
     */
    public void saveMockTemplate(String id, String mockTemplate) {
        sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockTemplate, mockTemplate).set(qTBusinessInterface.updatedTime, new Date())
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
                .where(qTBusinessInterface.interfaceId.eq(interfaceId)).fetchOne();
    }


}
