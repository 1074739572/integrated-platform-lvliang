package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ExceptionUtil;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.platform.entity.TProduct;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;

/**
 * 医院管理
 * @author czzhan
 */
@Slf4j
@RestController
public class ProductService extends QuerydslService<TProduct, String, TProduct, StringPath, PageRequest<TProduct>> {
    public ProductService(){
        super(qTProduct,qTProduct.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @ApiOperation(value = "产品管理列表")
    @GetMapping("/{version}/pb/productManage/getProductList")
    public ResultDto getProductList(String productName, Integer pageNo, Integer pageSize) {
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTProduct.isValid.equalsIgnoreCase(Constant.YES));
            //判断条件是否为空
            if(!StringUtils.isEmpty(productName)){
                list.add(qTProduct.productName.like(productName));
            }
            //根据查询条件获取医院列表
            QueryResults<TProduct> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                            TProduct.class,
                            qTProduct.id,
                            qTProduct.productName,
                            qTFunction.functionName,
                            qTProduct.updatedTime
                    )).from(qTProduct)
                    .where(list.toArray(new Predicate[list.size()]))
                    .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.productId.eq(qTProduct.id))
                    .leftJoin(qTFunction).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTProduct.updatedTime.desc())
                    .fetchResults();
            //分页
            TableData<TProduct> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Boolean.TRUE, "", tableData);

        }
        catch (Exception e) {
            logger.error("获取产品管理列表失败!", e);
            return new ResultDto(Boolean.FALSE, ExceptionUtil.dealException(e), null);
        }
    }

}
