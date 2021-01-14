package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.*;
import com.iflytek.integrated.common.bean.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.integrated.platform.utils.Utils;
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

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;

/**
 * 产品管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "产品管理")
@RestController
@RequestMapping("/{version}/pt/productManage")
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
    private ProductInterfaceLinkService productInterfaceLinkService;
    @Autowired
    private ProjectProductLinkService projectProductLinkService;
    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private BatchUidService batchUidService;
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
    public ResultDto delProductById(@ApiParam(value = "产品功能关联表id") @RequestParam(value = "id", required = true) String id){
        //查看产品功能关联是否存在
        TProductFunctionLink functionLink = productFunctionLinkService.getOne(id);
        if(functionLink == null){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有找到该产品功能,删除失败!", "没有找到该产品功能,删除失败!");
        }
        //删除产品功能关联关系前先查询该关联数据是否有接口配置相关联
        List<TBusinessInterface> tbiList = businessInterfaceService.getListByProductFunctionLinkId(id);
        if (CollectionUtils.isNotEmpty(tbiList)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "该产品功能已与接口配置关联,无法删除!", "该产品功能已与接口配置关联,无法删除!");
        }
        //删除产品功能关联关系前先查询该关联数据是否有项目相关联
        List<TProjectProductLink> tpplList = projectProductLinkService.findProjectProductLinkByPflId(id);
        if (CollectionUtils.isNotEmpty(tpplList)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "该产品功能已与项目关联,无法删除!", "该产品功能已与项目关联,无法删除!");
        }

        //删除产品和功能的关联关系
        long lon = productFunctionLinkService.delete(id);
        if(lon <= 0){
            throw new RuntimeException("产品功能删除失败!");
        }
        //如果该产品下没有其它功能关联,则删除该产品
        String productId = functionLink.getProductId();
        List<TProductFunctionLink> fetch = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(qTProductFunctionLink.productId.eq(productId)).fetch();
        if (CollectionUtils.isEmpty(fetch)) {
            //删除产品前判断该产品是否有标准接口相关联
            List<TProductInterfaceLink> tpilList = productInterfaceLinkService.getObjByProduct(productId);
            if (CollectionUtils.isEmpty(tpilList)) {
                this.delete(productId);
            }
        }

        //如果该功能没有产品相关联,则删除该功能
        String functionId = functionLink.getFunctionId();
        fetch = sqlQueryFactory.select(qTProductFunctionLink).from(qTProductFunctionLink)
                .where(qTProductFunctionLink.functionId.eq(functionId)).fetch();
        if (CollectionUtils.isEmpty(fetch)) {
            functionService.delete(functionId);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "产品功能删除成功!", "产品功能删除成功!");
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理新增/编辑")
    @PostMapping("/saveAndUpdateProduct")
    public ResultDto saveAndUpdateProduct(@RequestBody JSONObject jsonObj) {
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.getLoginUserName();
//        if(StringUtils.isBlank(loginUserName)){
//            throw new RuntimeException("没有获取到登录用户");
//        }
        String id = jsonObj.getString("id");
//        TProductFunctionLink tpfl = addOrGetLink(jsonObj.getString("productName"), jsonObj.getString("functionName"));
//        boolean existence = isExistence(id, link.getProductId(), link.getFunctionId());

        TProduct tp = this.getObjByProductName(jsonObj.getString("productName"));
        TFunction tf = functionService.getObjByName(jsonObj.getString("functionName"));
        String productId = tp!=null?tp.getId():null;
        String functionId = tf!=null?tf.getId():null;

        boolean existence = isExistence(id, productId, functionId);
        if (existence) {
//            throw new RuntimeException("产品和功能关系已存在");
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "产品和功能关系已存在!", null);
        }
        //新增编辑标识 1新增 2编辑
        String addOrUpdate = jsonObj.getString("addOrUpdate");
        if ("1".equals(addOrUpdate)) {
            return saveProduct(jsonObj,loginUserName);
        }
        if ("2".equals(addOrUpdate)) {
            return updateProduct(jsonObj,loginUserName);
        }
        return new ResultDto(Constant.ResultCode.ERROR_CODE, "addOrUpdate参数有误!", jsonObj);
    }

    /** 新增产品 */
    private ResultDto saveProduct(JSONObject jsonObj, String loginUserName) {
        String productName = jsonObj.getString("productName");
        if (StringUtils.isBlank(productName)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "产品名称未填!", jsonObj);
        }
        String functionId = jsonObj.getString("functionId");
        String functionName = jsonObj.getString("functionName");
        //判断输入产品是否是新产品
        TProduct tp = getObjByProductName(productName.trim());
        //判断输入功能是否是新功能
        TFunction tf = functionService.getObjByName(functionName.trim());
        if (tp != null && tf != null) {
            TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(tp.getId(), tf.getId());
            if (tpfl != null) {
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "该产品功能已有关联!", "该产品功能已有关联!");
            }
        }

        String productId;
        if (tp != null) {
            productId = tp.getId();
            tp.setUpdatedTime(new Date());
            tp.setUpdatedBy(loginUserName);
            this.put(productId, tp);
        }else {
            //新增产品
            productId = batchUidService.getUid(qTProduct.getTableName()) + "";
            tp = new TProduct();
            tp.setId(productId);
            tp.setProductCode(utils.generateCode(qTProduct, qTProduct.productCode, productName));
            tp.setProductName(productName);
            tp.setIsValid(Constant.IsValid.ON);
            tp.setCreatedTime(new Date());
            tp.setCreatedBy(loginUserName);
            this.post(tp);
        }

        //新增功能
        if (StringUtils.isBlank(functionId)) {
            functionId = batchUidService.getUid(qTFunction.getTableName()) + "";
            tf = new TFunction();
            tf.setId(functionId);
            tf.setFunctionCode(utils.generateCode(qTFunction, qTFunction.functionCode, functionName));
            tf.setFunctionName(functionName);
            tf.setCreatedBy(loginUserName);
            tf.setCreatedTime(new Date());
            functionService.post(tf);
        }
        //新增产品与功能关联
        TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(productId, functionId);
        if (tpfl == null || StringUtils.isBlank(tpfl.getId())) {
            tpfl = new TProductFunctionLink();
            tpfl.setId(batchUidService.getUid(qTProductFunctionLink.getTableName()) + "");
            tpfl.setProductId(productId);
            tpfl.setFunctionId(functionId);
            tpfl.setCreatedTime(new Date());
            tpfl.setCreatedBy(loginUserName);
            productFunctionLinkService.post(tpfl);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"新增产品功能关联成功", tpfl);
    }

    /** 编辑产品 */
    private ResultDto updateProduct(JSONObject jsonObj, String loginUserName) {
        String id = jsonObj.getString("id");
        String productId = jsonObj.getString("productId");
        String productName = jsonObj.getString("productName");
        String functionId = jsonObj.getString("functionId");

        //判断输入产品是否是新产品
        TProduct tp = getObjByProductName(productName.trim());
        if (tp == null) {
            //新增产品
            productId = batchUidService.getUid(qTProduct.getTableName()) + "";
            tp = new TProduct();
            tp.setId(productId);
            tp.setProductCode(utils.generateCode(qTProduct, qTProduct.productCode, productName));
            tp.setProductName(productName);
            tp.setIsValid(Constant.IsValid.ON);
            tp.setCreatedTime(new Date());
            tp.setCreatedBy(loginUserName);
            this.post(tp);
            //校验之前的产品功能是否有关联,没有则删除该产品功能
            String oldProductId = jsonObj.getString("oldProductId");
            TProductFunctionLink tpflObj = productFunctionLinkService.getObjByProductAndFunctionByNoId(oldProductId, null, id);
            if (tpflObj == null) {
                this.delete(oldProductId);
            }
            String oldFunctionId = jsonObj.getString("oldFunctionId");
            tpflObj = productFunctionLinkService.getObjByProductAndFunctionByNoId(null, oldFunctionId, id);
            if (tpflObj == null) {
                functionService.delete(oldFunctionId);
            }
        }
//        productId = tp.getId();

        //新增产品
//        if (StringUtils.isBlank(productId)) {
//            productId = batchUidService.getUid(qTProduct.getTableName()) + "";
//            TProduct tp = new TProduct();
//            tp.setId(productId);
//            tp.setProductCode(utils.generateCode(qTProduct, qTProduct.productCode, productName));
//            tp.setProductName(productName);
//            tp.setIsValid(Constant.IsValid.ON);
//            tp.setCreatedTime(new Date());
//            tp.setCreatedBy(loginUserName);
//            this.post(tp);
//        }
        //功能
        String functionName = jsonObj.getString("functionName");
        TFunction tf = functionService.getObjByName(functionName);
        if (tf == null) {
            functionId = batchUidService.getUid(qTFunction.getTableName()) + "";
            tf = new TFunction();
            tf.setId(functionId);
            tf.setFunctionCode(utils.generateCode(qTFunction, qTFunction.functionCode, functionName));
            tf.setFunctionName(functionName);
            tf.setCreatedTime(new Date());
            tf.setCreatedBy(loginUserName);
            functionService.post(tf);
        }else {
//            TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(productId, functionId);
            TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunctionByNoId(productId, functionId, id);
            if (tpfl != null) {
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "该产品与功能已有关联!",tpfl);
            }
        }
        //更新产品与功能关联
        productFunctionLinkService.updateObjById(id, productId, functionId, loginUserName);
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
            ).from(qTProduct).orderBy(qTProduct.createdTime.desc()).fetch();
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
            .orderBy(qTFunction.createdTime.desc()).fetch();
            product.setFunctions(functions);
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"选择产品下拉及其功能获取成功!", products);
    }


//    @ApiOperation(value = "新增接口-选择产品下拉及其功能")
//    @GetMapping("/getDisProAndFun")
//    public ResultDto getDisProAndFun(@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = false) String projectId) {
//        //获取该项目下所有产品id
//        List<String> productIdList = productFunctionLinkService.getProductIdByProjectId(projectId);
//        List<TProduct> productList = new ArrayList<>();
//        for (String productId : productIdList) {
//            productList.add(this.getOne(productId));
//        }
//        //拼接方法列表
//        for (TProduct product : productList){
//            List<TFunction> functions = sqlQueryFactory.select(
//                    Projections.bean(
//                            TFunction.class,
//                            qTFunction.id,
//                            qTFunction.functionCode,
//                            qTFunction.functionName
//                    )
//            ).from(qTFunction)
//                    .leftJoin(qTProductFunctionLink).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
//                    .where(qTProductFunctionLink.productId.eq(product.getId()))
//                    .orderBy(qTFunction.createdTime.desc()).fetch();
//            product.setFunctions(functions);
//        }
//        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"选择产品下拉及其功能获取成功!", productList);
//    }


    @ApiOperation(value = "根据产品获取功能")
    @GetMapping("/getFuncByPro")
    public ResultDto getFuncByPro(@ApiParam(value = "产品id") @RequestParam(value = "productId", required = true) String productId) {
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
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"根据产品获取功能成功", functions);
    }


    @ApiOperation(value = "新增接口时选择产品及其功能下拉")
    @GetMapping("/getDisProductAndFunByProject")
    public ResultDto getDisProductAndFunByProject(@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = true) String projectId) {
        //获取指定项目下所有产品
        List<String> productList = sqlQueryFactory.selectDistinct(qTProduct.id)
        .from(qTProduct)
        .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.productId.eq(qTProduct.id))
        .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTProductFunctionLink.id))
        .where(qTProjectProductLink.projectId.eq(projectId))
        .fetch();

        List<TProduct> rtnList = new ArrayList<>();
        for (String productId : productList) {
            TProduct tp = this.getOne(productId);
            //拼接方法列表
            List<TFunction> functions = sqlQueryFactory.select(
                    Projections.bean(
                            TFunction.class,
                            qTFunction.id,
                            qTFunction.functionCode,
                            qTFunction.functionName
                    )
            ).from(qTFunction)
                    .leftJoin(qTProductFunctionLink).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                    .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTProductFunctionLink.id))
                    .where(qTProductFunctionLink.productId.eq(productId).and(qTProjectProductLink.projectId.eq(projectId)))
                    .groupBy(qTFunction.id)
                    .orderBy(qTFunction.createdTime.desc()).fetch();
            tp.setFunctions(functions);
            rtnList.add(tp);
        }

//        //获取指定项目下所有产品
//        List<String> productList = sqlQueryFactory.selectDistinct(qTProduct.id)
//                .from(qTProduct)
//                .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.productId.eq(qTProduct.id))
//                .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTProductFunctionLink.id))
//                .where(list.toArray(new Predicate[list.size()]))
//                .fetch();
//
//        List<TProduct> rtnList = new ArrayList<>();
//        for (String productId : productList) {
//            TProduct tp = this.getOne(productId);
//            //拼接方法列表
//            List<TFunction> functions = sqlQueryFactory.select(
//                    Projections.bean(
//                            TFunction.class,
//                            qTFunction.id,
//                            qTFunction.functionCode,
//                            qTFunction.functionName
//                    )
//            ).from(qTFunction)
//                    .leftJoin(qTProductFunctionLink).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
//                    .where(qTProductFunctionLink.productId.eq(productId))
//                    .orderBy(qTFunction.createdTime.desc()).fetch();
//            tp.setFunctions(functions);
//            rtnList.add(tp);
//        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"选择产品下拉及其功能获取成功!", rtnList);
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
        if (StringUtils.isBlank(productId) || StringUtils.isBlank(functionId)) {
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
        return sqlQueryFactory.select(qTProduct).from(qTProduct).where(qTProduct.productName.eq(productName)).fetchFirst();
    }

}
