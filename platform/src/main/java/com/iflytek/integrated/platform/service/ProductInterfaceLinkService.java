package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TProductInterfaceLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;
@Service
public class ProductInterfaceLinkService  extends QuerydslService<TProductInterfaceLink, String, TProductInterfaceLink, StringPath, PageRequest<TProductInterfaceLink>> {

    private static final Logger logger = LoggerFactory.getLogger(ProductInterfaceLinkService.class);

    public ProductInterfaceLinkService(){
        super(qTProductInterfaceLink, qTProductInterfaceLink.id);
    }


    /**
     * 根据标准接口id删除产品与标准接口关联
     * @param id
     */
    public void deleteProductInterfaceLinkById(String id) {
        sqlQueryFactory.delete(qTProductInterfaceLink).where(qTProductInterfaceLink.interfaceId.eq(id)).execute();
    }


}
