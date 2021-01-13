package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.platform.entity.TProductFunctionLink;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * 获取产品-功能关联对象
     * @param productId
     * @param functionId
     * @return
     */
    public TProductFunctionLink getObjByProductAndFunction(String productId, String functionId) {
        TProductFunctionLink obj = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(qTProductFunctionLink.productId.eq(productId).and(qTProductFunctionLink.functionId.eq(functionId))).fetchFirst();
        return obj;
    }

    /**
     * 获取产品-功能关联对象
     * @param productId
     * @param functionId
     * @return
     */
    public TProductFunctionLink getObjByProductAndFunctionByNoId(String productId, String functionId, String productFunctionId) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(productId)) {
            list.add(qTProductFunctionLink.productId.eq(productId));
        }
        if (StringUtils.isNotEmpty(functionId)) {
            list.add(qTProductFunctionLink.functionId.eq(functionId));
        }
        if (StringUtils.isNotEmpty(productFunctionId)) {
            list.add(qTProductFunctionLink.id.notEqualsIgnoreCase(productFunctionId));
        }
        TProductFunctionLink obj = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(list.toArray(new Predicate[list.size()]))
                .fetchFirst();
        return obj;
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
        QueryResults<TProductFunctionLink> queryResults = sqlQueryFactory.select(Projections.bean(TProductFunctionLink.class,
                    qTProductFunctionLink.id, qTProductFunctionLink.productId, qTProductFunctionLink.functionId, qTProductFunctionLink.createdTime,
                    qTProductFunctionLink.updatedTime, qTProduct.productName.as("productName"), qTFunction.functionName.as("functionName"), qTProduct.productCode.as("productCode")))
                    .from(qTProductFunctionLink)
                    .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                    .leftJoin(qTFunction).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .orderBy(qTProduct.createdTime.desc())
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .fetchResults();
        return queryResults;
    }

    /**
     * 根据id修改关联产品/功能
     * @param id
     * @return
     */
    public long updateObjById(String id, String productId, String functionId, String loginUserName) {
        return sqlQueryFactory.update(qTProductFunctionLink)
                .set(qTProductFunctionLink.productId, productId)
                .set(qTProductFunctionLink.functionId, functionId)
                .set(qTProductFunctionLink.updatedTime, new Date())
                .set(qTProductFunctionLink.updatedBy, loginUserName!=null?loginUserName:"")
                .where(qTProductFunctionLink.id.eq(id)).execute();
    }

    /**
     * 查询某产品下所有关联(排除当前)
     * @param productId
     * @param id
     * @return
     */
//    public List<TProductFunctionLink> getListByProductIdAndNoId(String productId, String id) {
//        List<TProductFunctionLink> list = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
//                .where(qTProductFunctionLink.productId.eq(productId).and(qTProductFunctionLink.id.notEqualsIgnoreCase(id)))
//                .fetch();
//        return list;
//    }

    /**
     * 根据项目id获取所有产品id
     * @param projectId
     */
//    public List<String> getProductIdByProjectId(String projectId) {
//         return sqlQueryFactory.selectDistinct(qTProductFunctionLink.productId).from(qTProductFunctionLink)
//                .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTProductFunctionLink.id))
//                .where(qTProjectProductLink.projectId.eq(projectId))
//                .fetch();
//    }

}
