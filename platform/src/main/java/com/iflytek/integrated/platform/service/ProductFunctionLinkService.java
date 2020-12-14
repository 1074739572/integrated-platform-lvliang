package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TProductFunctionLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;

/**
* 产品与功能关联
* @author weihe9
* @date 2020/12/12 14:16
*/
@Service
public class ProductFunctionLinkService extends QuerydslService<TProductFunctionLink, String, TProductFunctionLink, StringPath, PageRequest<TProductFunctionLink>> {

    private static final Logger logger = LoggerFactory.getLogger(ProductFunctionLinkService.class);

    public ProductFunctionLinkService(){
        super(qTProductFunctionLink, qTProductFunctionLink.id);
    }

    /**
     * 根据id删除产品与功能关联信息
     * @param id
     */
    public void deleteProductFunctionLinkById(String id) {
        sqlQueryFactory.delete(qTProductFunctionLink).where(qTProductFunctionLink.id.eq(id)).execute();
    }

    /**
     * 获取产品-功能关联对象
     * @param productId
     * @param functionId
     * @return
     */
    public TProductFunctionLink getObjByProductAndFunction(String productId, String functionId) {
        return sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink).
                where(qTProductFunctionLink.productId.eq(productId).and(qTProductFunctionLink.functionId.eq(functionId))).fetchOne();
    }

}
