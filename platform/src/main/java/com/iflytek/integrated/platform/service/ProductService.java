package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.*;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.annotation.AvoidRepeatCommit;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.integrated.platform.entity.TFunction;
import com.iflytek.integrated.platform.entity.TProduct;
import com.iflytek.integrated.platform.entity.TProductFunctionLink;
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
import io.swagger.annotations.ApiParam;
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
@CrossOrigin
@RestController
@RequestMapping("/v1/pb/productManage")
public class ProductService extends QuerydslService<TProduct, String, TProduct, StringPath, PageRequest<TProduct>> {

    public ProductService(){
        super(qTProduct,qTProduct.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private FunctionService functionService;
    @Autowired
    private ProductFunctionLinkService productFunctionLinkService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private Utils utils;


    @ApiOperation(value = "产品功能列表")
    @GetMapping("/getProductList")
    public ResultDto getProductList(@ApiParam(value = "产品编码") @RequestParam(value = "productCode", required = false) String productCode,
                                    @ApiParam(value = "产品名称") @RequestParam(value = "productName", required = false) String productName,
                                    @ApiParam(value = "页码",example = "1") @RequestParam(defaultValue = "1", required = false)Integer pageNo,
                                    @ApiParam(value = "每页大小",example = "10") @RequestParam(defaultValue = "10", required = false)Integer pageSize){
        try {
            QueryResults<TProductFunctionLink> queryResults = productFunctionLinkService.getTProductFunctionLinkList(
                    productCode, productName, pageNo, pageSize);
            //分页
            TableData<TProductFunctionLink> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "产品管理列表获取成功", tableData);
        }
        catch (Exception e) {
            logger.error("获取产品管理列表失败!", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", ExceptionUtil.dealException(e));
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理删除")
    @PostMapping("/delProductById")
    public ResultDto delProductById(@ApiParam(value = "产品id") @RequestParam(value = "id", required = true) String id){
        //查看产品是否存在
        TProductFunctionLink functionLink = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(qTProductFunctionLink.id.eq(id)).fetchOne();
        if(functionLink == null || StringUtils.isEmpty(functionLink.getId())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有找到该产品功能，删除失败", "没有找到该产品功能，删除失败");
        }
        //删除产品：删除产品和功能的关联关系
        Long lon = sqlQueryFactory.delete(qTProductFunctionLink)
                .where(qTProductFunctionLink.id.eq(functionLink.getId())).execute();
        if(lon <= 0){
            throw new RuntimeException("产品功能删除失败");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "产品功能删除成功", "产品功能删除成功");
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理新增/编辑")
    @PostMapping("/saveAndUpdateProduct")
    @AvoidRepeatCommit
    public ResultDto saveAndUpdateProduct(@RequestBody JSONObject jsonObj) {
        String id = jsonObj.getString("id");
//        TProductFunctionLink tpfl = addOrGetLink(jsonObj.getString("productName"), jsonObj.getString("functionName"));
//        boolean existence = isExistence(id, link.getProductId(), link.getFunctionId());

        TProduct tp = this.getObjByProductName(jsonObj.getString("productName"));
        TFunction tf = functionService.getObjByName(jsonObj.getString("functionName"));
        String productId = tp!=null?tp.getId():null;
        String functionId = tf!=null?tf.getId():null;

        boolean existence = isExistence(id, productId, functionId);
        if (existence) {
            throw new RuntimeException("产品和功能关系已存在");
        }else {
            if (StringUtils.isBlank(id)) {
                return saveProduct(jsonObj);
            }else {
                return updateProduct(jsonObj);
            }
        }
    }

    /** 新增产品 */
    private ResultDto saveProduct(JSONObject jsonObj) {
        String productName = jsonObj.getString("productName");
        if (StringUtils.isBlank(productName)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "产品名称未填!", jsonObj);
        }
        //判断输入产品是否是新产品
        TProduct tp = getObjByProductName(productName.trim());
        String productId;
        if (tp != null) {
            productId = tp.getId();
            tp.setUpdatedTime(new Date());
            this.put(productId, tp);
        }else {
            productId = batchUidService.getUid(qTProduct.getTableName()) + "";
            //新增产品
            tp = new TProduct();
            tp.setId(productId);
            tp.setProductCode(utils.generateCode(qTProduct, qTProduct.productCode, productName));
            tp.setProductName(productName);
            tp.setIsValid(Constant.IsValid.ON);
            tp.setCreatedTime(new Date());
            this.post(tp);
        }

        String functionId = jsonObj.getString("functionId");
        //新增功能
        if (StringUtils.isBlank(functionId)) {
            functionId = batchUidService.getUid(qTFunction.getTableName()) + "";
            String functionName = jsonObj.getString("functionName");
            TFunction tf = new TFunction();
            tf.setId(functionId);
            tf.setFunctionCode(utils.generateCode(qTFunction, qTFunction.functionCode, functionName));
            tf.setFunctionName(functionName);
            tf.setCreatedTime(new Date());
            functionService.post(tf);
        }
        //新增产品与功能关联
        TProductFunctionLink tpfl = new TProductFunctionLink();
        tpfl.setId(batchUidService.getUid(qTProductFunctionLink.getTableName()) + "");
        tpfl.setProductId(productId);
        tpfl.setFunctionId(functionId);
        tpfl.setCreatedTime(new Date());
        productFunctionLinkService.post(tpfl);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"新增产品功能关联成功", tpfl);
    }

    /** 编辑产品 */
    private ResultDto updateProduct(JSONObject jsonObj) {
        String id = jsonObj.getString("id");
        String productId = jsonObj.getString("productId");
        String productName = jsonObj.getString("productName");
        String functionId = jsonObj.getString("functionId");
        //新增产品
        if (StringUtils.isBlank(productId)) {
            productId = batchUidService.getUid(qTProduct.getTableName()) + "";
            TProduct tp = new TProduct();
            tp.setId(productId);
            tp.setProductCode(utils.generateCode(qTProduct, qTProduct.productCode, productName));
            tp.setProductName(productName);
            tp.setIsValid(Constant.IsValid.ON);
            tp.setCreatedTime(new Date());
            this.post(tp);
        }
        //新增功能
        if (StringUtils.isBlank(functionId)) {
            functionId = batchUidService.getUid(qTFunction.getTableName()) + "";
            String functionName = jsonObj.getString("functionName");
            TFunction tf = new TFunction();
            tf.setId(functionId);
            tf.setFunctionCode(utils.generateCode(qTFunction, qTFunction.functionCode, functionName));
            tf.setFunctionName(functionName);
            tf.setCreatedTime(new Date());
            functionService.post(tf);
        }else {
            TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(productId, functionId);
            if (tpfl != null) {
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "该产品与功能已有关联!",tpfl);
            }
        }
        //更新产品与功能关联
        productFunctionLinkService.updateObjById(id, productId, functionId);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"编辑产品功能关联成功", id);
    }


    @ApiOperation(value = "选择产品下拉及其功能")
    @GetMapping("/getDisProduct")
    public ResultDto getDisProduct() {
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
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"选择产品下拉及其功能获取成功!", products);
    }


    @ApiOperation(value = "根据产品获取功能")
    @GetMapping("/getFuncByPro")
    public ResultDto getFuncByPro(String productId) {
        if(StringUtils.isEmpty(productId)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "产品id不能为空","产品id不能为空");
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
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"根据产品获取功能成功",functions);
    }


    /**
     * 获取或新增产品，功能，并保存关系
     * @param productName
     * @param functionName
     * @return
     */
//    private TProductFunctionLink addOrGetLink(String productName, String functionName){
////        return productFunctionLinkService.getObjByPNameAndFName(productName, functionName);
//
//        TProductFunctionLink functionLink = new TProductFunctionLink();
//        //查询是否已经存在产品Id
//        functionLink.setProductId(sqlQueryFactory.select(qTProduct.id)
//                .from(qTProduct).where(qTProduct.productName.eq(productName)).fetchOne());
//
//        functionLink.setFunctionId(sqlQueryFactory.select(qTFunction.id)
//                .from(qTFunction).where(qTFunction.functionName.eq(functionName)).fetchOne());
//
//        if(StringUtils.isEmpty(functionLink.getProductId())){
//            //如果是新的产品名称，新建一个产品
//            functionLink.setProductId(batchUidService.getUid(qTProduct.getTableName()) + "");
//            Long lon = sqlQueryFactory.insert(qTProduct)
//                    .set(qTProduct.id,functionLink.getProductId())
//                    .set(qTProduct.productName,productName)
//                    .set(qTProduct.productCode,utils.generateCode(qTProduct,qTProduct.productCode,productName))
//                    .set(qTProduct.isValid,Constant.IsValid.ON)
//                    .set(qTProduct.createdBy,"")
//                    .set(qTProduct.createdTime,new Date()).execute();
//            if(lon <= 0){
//                throw new RuntimeException("创建产品失败");
//            }
//        }
////        查询是否存在功能
//
//        if(StringUtils.isEmpty(functionLink.getFunctionId())){
//            //如果是新的产品名称，新建一个功能
//            functionLink.setFunctionId(batchUidService.getUid(qTFunction.getTableName()) + "");
//            Long lon = sqlQueryFactory.insert(qTFunction)
//                    .set(qTFunction.id,functionLink.getFunctionId())
//                    .set(qTFunction.functionName,functionName)
//                    .set(qTFunction.functionCode,utils.generateCode(qTFunction,qTFunction.functionCode,functionName))
//                    .set(qTFunction.createdBy,"")
//                    .set(qTFunction.createdTime,new Date()).execute();
//            if(lon <= 0){
//                throw new RuntimeException("创建功能失败");
//            }
//        }
//        return functionLink;
//    }

    /**
     * 校验是否已经存在
     * @param linkId
     * @param productId
     * @param functionId
     */
    private boolean isExistence(String linkId, String productId, String functionId) {
        if (StringUtils.isBlank(productId)) {
            return false;
        }
        if (StringUtils.isBlank(functionId)) {
            return false;
        }

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
            return true;
        }
        return false;
    }


    /**
     * 根据产品名称获取产品信息
     * @param productName
     * @return
     */
    public TProduct getObjByProductName(String productName) {
        return sqlQueryFactory.select(qTProduct).from(qTProduct).where(qTProduct.productName.eq(productName)).fetchOne();
    }

}
