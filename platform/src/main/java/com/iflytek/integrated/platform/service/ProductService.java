package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.*;
import com.iflytek.integrated.platform.dto.ProductFunctionDto;
import com.iflytek.integrated.platform.entity.TProduct;
import com.iflytek.integrated.platform.entity.TProductFunctionLink;
import com.iflytek.integrated.platform.validator.ValidationResult;
import com.iflytek.integrated.platform.validator.ValidatorHelper;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.UidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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

    @Autowired
    private UidService uidService;
    @Autowired
    private ValidatorHelper validatorHelper;

    @ApiOperation(value = "产品管理列表")
    @GetMapping("/{version}/pb/productManage/getProductList")
    public ResultDto getProductList(String productCode, String productName, Integer pageNo, Integer pageSize) {
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTProduct.isValid.eq(Constant.IS_VALID_ON));
            //判断条件是否为空
            if(!StringUtils.isEmpty(productCode)){
                list.add(qTProduct.productCode.eq(productCode));
            }
            if(!StringUtils.isEmpty(productName)){
                list.add(qTProduct.productName.like(Utils.createFuzzyText(productName))
                        .or(qTFunction.functionName.like(Utils.createFuzzyText(productName))));
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
            return new ResultDto(Boolean.FALSE, "", ExceptionUtil.dealException(e));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理删除")
    @DeleteMapping("/{version}/pb/productManage/delProductById")
    public ResultDto delProductById(String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto(Boolean.FALSE, "id不能为空", null);
        }
        //查看产品是否存在
        TProductFunctionLink functionLink = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(qTProductFunctionLink.id.eq(id)).fetchOne();
        if(functionLink == null || StringUtils.isEmpty(functionLink.getId())){
            return new ResultDto(Boolean.FALSE, "", "没有找到该产品功能，删除失败");
        }
        //删除产品：删除产品和功能的关联关系
        sqlQueryFactory.delete(qTProductFunctionLink)
                .where(qTProductFunctionLink.id.eq(functionLink.getId())).execute();
        return new ResultDto(Boolean.TRUE, "", "产品功能删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理新增/编辑")
    @PostMapping("/{version}/pb/productManage/saveAndUpdateProduct")
    public ResultDto saveAndUpdateProduct(@RequestBody ProductFunctionDto dto){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(dto);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Boolean.FALSE, "", validationResult.getErrorMsg());
        }

        if(StringUtils.isEmpty(dto.getId())){
            //新增产品关系

            return new ResultDto(true,"","新增产品功能关系成功");
        }
        else {
            //编辑产品关系
            return new ResultDto(true,"","编辑产品功能关系成功");
        }
    }

    private TProductFunctionLink addOrGetLink(){
        TProductFunctionLink functionLink = new TProductFunctionLink();
        return functionLink;
    }
}
