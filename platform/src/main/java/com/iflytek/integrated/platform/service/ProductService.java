package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.*;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.dto.ProductFunctionDto;
import com.iflytek.integrated.platform.entity.TFunction;
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
import java.util.Date;
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
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", tableData);
        }
        catch (Exception e) {
            logger.error("获取产品管理列表失败!", e);
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理删除")
    @DeleteMapping("/{version}/pb/productManage/delProductById")
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
        sqlQueryFactory.delete(qTProductFunctionLink)
                .where(qTProductFunctionLink.id.eq(functionLink.getId())).execute();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "产品功能删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "产品管理新增/编辑")
    @PostMapping("/{version}/pb/productManage/saveAndUpdateProduct")
    public ResultDto saveAndUpdateProduct(@RequestBody ProductFunctionDto dto){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(dto);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        //获取产品id，功能id关系
        TProductFunctionLink link = addOrGetLink(dto.getProductName(),dto.getFunctionName());
        if(StringUtils.isEmpty(dto.getId())){
            //新增产品关系
            Long lon = sqlQueryFactory.insert(qTProductFunctionLink)
                    .set(qTProductFunctionLink.id,uidService.getUID()+"")
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
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"","新增/编辑产品功能关系成功");
    }

    @ApiOperation(value = "选择产品下拉及其功能")
    @GetMapping("/{version}/pb/productManage/getDisProduct")
    public ResultDto getDisProduct(){
        List<TProduct> products = sqlQueryFactory.select(
                Projections.bean(
                        TProduct.class,
                        qTProduct.productName,
                        qTProduct.productCode
                )
            ).from(qTProduct).orderBy(qTProduct.updatedTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"",products);
    }

    @ApiOperation(value = "根据产品获取功能")
    @GetMapping("/{version}/pb/productManage/getFuncByPro")
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
            functionLink.setProductId(uidService.getUID() + "");
            Long lon = sqlQueryFactory.insert(qTProduct)
                    .set(qTProduct.id,functionLink.getProductId())
                    .set(qTProduct.productName,productName)
                    .set(qTProduct.productCode,"")
                    .set(qTProduct.isValid,Constant.IS_VALID_ON)
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
            functionLink.setFunctionId(uidService.getUID() + "");
            Long lon = sqlQueryFactory.insert(qTFunction)
                    .set(qTFunction.functionName,functionName)
                    .set(qTFunction.functionCode,functionLink.getFunctionId())
                    .set(qTFunction.createdBy,"")
                    .set(qTFunction.createdTime,new Date()).execute();
            if(lon <= 0){
                throw new RuntimeException("创建功能失败");
            }
        }
        return functionLink;
    }
}
