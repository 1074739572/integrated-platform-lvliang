package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.*;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.dto.ProductFunctionDto;
import com.iflytek.integrated.platform.entity.TFunction;
import com.iflytek.integrated.platform.entity.TProduct;
import com.iflytek.integrated.platform.entity.TProductFunctionLink;
import com.iflytek.integrated.platform.validator.ValidationResult;
import com.iflytek.integrated.platform.validator.ValidatorHelper;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;

/**
 * 产品管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "产品管理")
@RestController
@RequestMapping("/{version}/pb/productManage")
public class ProductService extends QuerydslService<TProduct, String, TProduct, StringPath, PageRequest<TProduct>> {
    public ProductService(){
        super(qTProduct,qTProduct.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private Utils utils;

    @ApiOperation(value = "产品管理列表")
    @GetMapping("/getProductList")
    public ResultDto getProductList(String productCode, String productName,
                            @RequestParam(defaultValue = "1")Integer pageNo,
                            @RequestParam(defaultValue = "10")Integer pageSize){
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTProduct.isValid.eq(Constant.IsValid.ON));
            //判断条件是否为空
            if(StringUtils.isNotEmpty(productCode)){
                list.add(qTProduct.productCode.eq(productCode));
            }
            if(StringUtils.isNotEmpty(productName)){
                list.add(qTProduct.productName.like(Utils.createFuzzyText(productName))
                        .or(qTFunction.functionName.like(Utils.createFuzzyText(productName))));
            }
            //根据查询条件获取产品列表
            QueryResults<TProduct> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                            TProduct.class,
                            qTProduct.id,
                            qTProduct.productName,
                            qTFunction.functionName,
                            qTProduct.updatedTime,
                            qTProductFunctionLink.id.as("funLinkId")
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
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", tableData);
        }
        catch (Exception e) {
            logger.error("获取产品管理列表失败!", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", ExceptionUtil.dealException(e));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理删除")
    @DeleteMapping("/delProductById")
    public ResultDto delProductById(String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "id不能为空");
        }
        //查看产品是否存在
        TProductFunctionLink functionLink = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(qTProductFunctionLink.id.eq(id)).fetchOne();
        if(functionLink == null || StringUtils.isEmpty(functionLink.getId())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "没有找到该产品功能，删除失败");
        }
        //删除产品：删除产品和功能的关联关系
        Long lon = sqlQueryFactory.delete(qTProductFunctionLink)
                .where(qTProductFunctionLink.id.eq(functionLink.getId())).execute();
        if(lon <= 0){
            throw new RuntimeException("产品功能删除失败");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "产品功能删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理新增/编辑")
    @PostMapping("/saveAndUpdateProduct")
    public ResultDto saveAndUpdateProduct(@RequestBody ProductFunctionDto dto){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(dto);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        //获取产品id，功能id关系
        TProductFunctionLink link = addOrGetLink(dto.getProductName(),dto.getFunctionName());
        //校验是否存在产品和功能关系
        isExistence(dto.getId(),link.getProductId(),link.getFunctionId());
        if(StringUtils.isEmpty(dto.getId())){
            //新增产品关系
            Long lon = sqlQueryFactory.insert(qTProductFunctionLink)
                    .set(qTProductFunctionLink.id,batchUidService.getUid(qTProductFunctionLink.getTableName())+"")
                    .set(qTProductFunctionLink.productId,link.getProductId())
                    .set(qTProductFunctionLink.functionId,link.getFunctionId())
                    .set(qTProductFunctionLink.createdBy,"")
                    .set(qTProductFunctionLink.createdTime,new Date()).execute();
            if(lon <= 0){
                throw new RuntimeException("产品管理新增失败");
            }
        }
        else {
            //编辑产品关系
            Long lon = sqlQueryFactory.update(qTProductFunctionLink)
                    .set(qTProductFunctionLink.productId,link.getProductId())
                    .set(qTProductFunctionLink.functionId,link.getFunctionId())
                    .set(qTProductFunctionLink.updatedBy,"")
                    .set(qTProductFunctionLink.updatedTime,new Date())
                    .where(qTProductFunctionLink.id.eq(dto.getId())).execute();
            if(lon <= 0){
                throw new RuntimeException("产品管理编辑失败");
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"","新增或编辑产品功能关系成功");
    }

    @ApiOperation(value = "选择产品下拉及其功能")
    @GetMapping("/getDisProduct")
    public ResultDto getDisProduct(){
        List<TProduct> products = sqlQueryFactory.select(
                Projections.bean(
                        TProduct.class,
                        qTProduct.id,
                        qTProduct.productName,
                        qTProduct.productCode
                )
            ).from(qTProduct).orderBy(qTProduct.updatedTime.desc()).fetch();
        //拼接方法列表
        for (TProduct product : products){
            List<TFunction> functions = sqlQueryFactory.select(
                    Projections.bean(
                        TFunction.class,
                        qTFunction.id,
                        qTFunction.functionCode,
                        qTFunction.functionName
                    )
            ).from(qTFunction)
            .leftJoin(qTProductFunctionLink).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
            .where(qTProductFunctionLink.productId.eq(product.getId()))
            .orderBy(qTFunction.updatedTime.desc()).fetch();
            product.setFunctions(functions);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"",products);
    }

    @ApiOperation(value = "根据产品获取功能")
    @GetMapping("/getFuncByPro")
    public ResultDto getFuncByPro(String productId){
        if(StringUtils.isEmpty(productId)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "","产品id不能为空");
        }
        List<TFunction> functions = sqlQueryFactory.select(
                Projections.bean(
                        TFunction.class,
                        qTFunction.id,
                        qTFunction.functionName,
                        qTFunction.functionCode
                )
            ).from(qTFunction)
                .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.functionId.eq(qTFunction.id))
                .where(qTProductFunctionLink.productId.eq(productId)).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"",functions);
    }

    /**
     * 获取或新增产品，功能，并保存关系
     * @param productName
     * @param functionName
     * @return
     */
    private TProductFunctionLink addOrGetLink(String productName, String functionName){
        TProductFunctionLink functionLink = new TProductFunctionLink();
        //查询是否已经存在产品Id
        functionLink.setProductId(sqlQueryFactory.select(qTProduct.id)
                .from(qTProduct).where(qTProduct.productName.eq(productName)).fetchOne());
        if(StringUtils.isEmpty(functionLink.getProductId())){
            //如果是新的产品名称，新建一个产品
            functionLink.setProductId(batchUidService.getUid(qTProduct.getTableName()) + "");
            Long lon = sqlQueryFactory.insert(qTProduct)
                    .set(qTProduct.id,functionLink.getProductId())
                    .set(qTProduct.productName,productName)
                    .set(qTProduct.productCode,utils.generateCode(qTProduct,qTProduct.productCode,productName))
                    .set(qTProduct.isValid,Constant.IsValid.ON)
                    .set(qTProduct.createdBy,"")
                    .set(qTProduct.createdTime,new Date()).execute();
            if(lon <= 0){
                throw new RuntimeException("创建产品失败");
            }
        }
        //查询是否存在功能
        functionLink.setFunctionId(sqlQueryFactory.select(qTFunction.id)
                .from(qTFunction).where(qTFunction.functionName.eq(functionName)).fetchOne());
        if(StringUtils.isEmpty(functionLink.getFunctionId())){
            //如果是新的产品名称，新建一个功能
            functionLink.setFunctionId(batchUidService.getUid(qTFunction.getTableName()) + "");
            Long lon = sqlQueryFactory.insert(qTFunction)
                    .set(qTFunction.id,functionLink.getFunctionId())
                    .set(qTFunction.functionName,functionName)
                    .set(qTFunction.functionCode,utils.generateCode(qTFunction,qTFunction.functionCode,functionName))
                    .set(qTFunction.createdBy,"")
                    .set(qTFunction.createdTime,new Date()).execute();
            if(lon <= 0){
                throw new RuntimeException("创建功能失败");
            }
        }
        return functionLink;
    }

    /**
     * 校验是否已经存在
     * @param linkId
     * @param productId
     * @param functionId
     */
    private void isExistence(String linkId, String productId, String functionId){
        //校验是否存在重复产品和功能关系
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTProductFunctionLink.functionId.eq(functionId));
        list.add(qTProductFunctionLink.productId.eq(productId));
        if(StringUtils.isNotEmpty(linkId)){
            list.add(qTProductFunctionLink.id.notEqualsIgnoreCase(linkId));
        }
        List<String> links = sqlQueryFactory.select(qTProductFunctionLink.id).from(qTProductFunctionLink)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(links)){
            throw new RuntimeException("产品和功能关系已存在");
        }
    }
}