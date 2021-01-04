package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TProductInterfaceLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;
/**
* 产品与标准接口关联
* @author weihe9
* @date 2020/12/14 11:00
*/
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

    /**
     * 根据接口id获取产品与标准接口关联
     * @param interfaceId
     * @return
     */
    public List<TProductInterfaceLink> getObjByInterface(String interfaceId) {
        return sqlQueryFactory.select(qTProductInterfaceLink).from(qTProductInterfaceLink)
                .where(qTProductInterfaceLink.interfaceId.eq(interfaceId)).fetch();
    }

    /**
     * 根据产品id获取产品与标准接口关联
     * @param productId
     * @return
     */
    public List<TProductInterfaceLink> getObjByProduct(String productId) {
        return sqlQueryFactory.select(qTProductInterfaceLink).from(qTProductInterfaceLink)
                .where(qTProductInterfaceLink.productId.eq(productId)).fetch();
    }

}
