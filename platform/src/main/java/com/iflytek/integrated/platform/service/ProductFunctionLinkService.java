package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.platform.entity.TProductFunctionLink;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
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
        return sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(qTProductFunctionLink.productId.eq(productId).and(qTProductFunctionLink.functionId.eq(functionId))).fetchOne();
    }

    /**
     * 产品管理列表查询
     * @param productCode
     * @param productName
     * @param pageNo
     * @param pageSize
     * @return
     */
    public QueryResults<TProductFunctionLink> getTProductFunctionLinkList(String productCode, String productName, Integer pageNo, Integer pageSize) {
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTProduct.isValid.eq(Constant.IsValid.ON));
        if(StringUtils.isNotEmpty(productCode)){
            list.add(qTProduct.productCode.eq(productCode));
        }
        if(StringUtils.isNotEmpty(productName)){
            list.add(qTProduct.productName.like(Utils.createFuzzyText(productName)));
        }
        QueryResults<TProductFunctionLink> queryResults = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                        .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                        .leftJoin(qTFunction).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                        .where(list.toArray(new Predicate[list.size()]))
                        .orderBy(qTProduct.updatedTime.desc())
                        .limit(pageSize)
                        .offset((pageNo - 1) * pageSize)
                        .fetchResults();
        return queryResults;
    }

}
