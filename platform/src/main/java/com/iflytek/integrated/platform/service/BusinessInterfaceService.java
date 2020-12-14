package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
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



}
