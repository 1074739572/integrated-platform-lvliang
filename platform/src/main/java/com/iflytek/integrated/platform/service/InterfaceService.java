package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.dto.*;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTInterfaceType.qTInterfaceType;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;
import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;
import static com.iflytek.integrated.platform.entity.QTType.qTType;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;

/**
* 接口管理
* @author weihe9
* @date 2020/12/12 17:16
*/
@Slf4j
@Api(tags = "接口管理")
@RestController
@RequestMapping("/{version}/pt/interfaceManage")
public class InterfaceService extends BaseService<TInterface, String, StringPath> {

    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private ProductInterfaceLinkService productInterfaceLinkService;
    @Autowired
    private ProductFunctionLinkService productFunctionLinkService;
    @Autowired
    private InterfaceParamService interfaceParamService;
    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private NiFiRequestUtil niFiRequestUtil;
    @Autowired
    private ValidatorHelper validatorHelper;

    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public InterfaceService(){
        super(qTInterface, qTInterface.id);
    }

    @ApiOperation(value = "更改mock状态", notes = "更改mock状态")
    @PostMapping("/updateMockStatus")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> updateMockStatus(@ApiParam(value = "接口配置") @RequestParam(value = "id", required = true) String id,
                                      @ApiParam(value = "更改后的状态") @RequestParam(value = "mockStatus", required = true) String mockStatus) {
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        return businessInterfaceService.updateMockStatus(id, mockStatus, loginUserName);
    }

    @ApiOperation(value = "更改接口配置状态", notes = "更改接口配置状态")
    @PostMapping("/updateStatus")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> updateStatus(@ApiParam(value = "接口配置") @RequestParam(value = "id", required = true) String id,
                                  @ApiParam(value = "更改后的状态") @RequestParam(value = "status", required = true) String status) {
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        return businessInterfaceService.updateStatus(id, status, loginUserName);
    }

    @ApiOperation(value = "获取mock模板", notes = "获取mock模板")
    @GetMapping("/getMockTemplate")
    public ResultDto<List<MockTemplateDto>> getMockTemplate(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        List<TBusinessInterface> interfaces = businessInterfaceService.busInterfaces(id);
        if(CollectionUtils.isEmpty(interfaces)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id没有找到接口配置");
        }
        List<MockTemplateDto> dtoList = new ArrayList<>();
        for(TBusinessInterface businessInterface : interfaces){
            MockTemplateDto dto = new  MockTemplateDto(
                    businessInterface.getId(),
                    //如果mock模板为空，取出参的格式，作为初始的mock模板
                    StringUtils.isNotBlank(businessInterface.getMockTemplate())?
                            businessInterface.getMockTemplate():businessInterface.getOutParamFormat(),
                    businessInterface.getMockIsUse(),
                    businessInterface.getExcErrOrder(),
                    businessInterface.getBusinessInterfaceName()
            );
            dtoList.add(dto);
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取mock模板成功!", dtoList);
    }

    @ApiOperation(value = "保存mock模板", notes = "保存mock模板")
    @PostMapping("/saveMockTemplate")
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<String> saveMockTemplate(@RequestBody List<MockTemplateDto> dtoList) {
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        if(CollectionUtils.isEmpty(dtoList)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到mock模板!");
        }
        //返回缓存操作数据
        String rtnStr = "";
        for (MockTemplateDto dto : dtoList){
            //校验参数是否完整
            ValidationResult validationResult = validatorHelper.validate(dto);
            if (validationResult.isHasErrors()) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg(), validationResult.getErrorMsg());
            }
            //校验mock模板格式是否正确
            PlatformUtil.strIsJsonOrXml(dto.getMockTemplate());
            long lon = businessInterfaceService.saveMockTemplate(dto.getId(),
                    dto.getMockTemplate(), dto.getMockIsUse(), loginUserName);
            if(lon <= 0){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "保存mock模板失败!");
            }
            rtnStr += dto.getId() + ",";
        }
        rtnStr = StringUtils.isBlank(rtnStr)?null:rtnStr.substring(0,rtnStr.length()-1);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存mock模板成功!", rtnStr);
    }

    @ApiOperation(value = "获取接口调试显示数据", notes = "获取接口调试显示数据")
    @GetMapping("/getInterfaceDebug")
    public ResultDto<InDebugResDto> getInterfaceDebug(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        try {
            TBusinessInterface businessInterface = sqlQueryFactory.select(
                Projections.bean(
                        TBusinessInterface.class,
                        qTBusinessInterface.id,
                        qTBusinessInterface.interfaceId,
                        qTBusinessInterface.vendorConfigId,
                        qTProject.projectCode.as("projectCode"),
                        qTInterface.interfaceUrl.as("interfaceUrl"),
                        qTProduct.productCode.as("productCode")
                    )
                ).from(qTBusinessInterface)
                    .leftJoin(qTInterface).on(qTBusinessInterface.interfaceId.eq(qTInterface.id))
                    .leftJoin(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
                    .leftJoin(qTPlatform).on(qTPlatform.id.eq(qTVendorConfig.platformId))
                    .leftJoin(qTProject).on(qTProject.id.eq(qTPlatform.projectId))
                    .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(qTBusinessInterface.productFunctionLinkId))
                    .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                    .where(qTBusinessInterface.id.eq(id)).fetchFirst();
            if(businessInterface == null || StringUtils.isEmpty(businessInterface.getId())){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有查询到接口配置信");
            }
            //获取入参列表
            String interfaceId = StringUtils.isNotEmpty(businessInterface.getInterfaceId())?businessInterface.getInterfaceId():"";
            List<String> paramNames = sqlQueryFactory.select(qTInterfaceParam.paramName).from(qTInterfaceParam)
                    .where(qTInterfaceParam.interfaceId.eq(interfaceId).and(qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN))).fetch();

            //获取医院名称列表
            String vendorConfigId = StringUtils.isNotEmpty(businessInterface.getVendorConfigId())?
                    businessInterface.getVendorConfigId():"";
            List<String> hospitals = sqlQueryFactory.select(qTHospital.hospitalCode).from(qTHospitalVendorLink)
                    .leftJoin(qTHospital).on(qTHospital.id.eq(qTHospitalVendorLink.hospitalId))
                    .where(qTHospitalVendorLink.vendorConfigId.eq(vendorConfigId).and(qTHospital.hospitalCode.isNotEmpty())).fetch();

            //拼接实体
            InDebugResDto resDto = new InDebugResDto();
            resDto.setFuncode(businessInterface.getInterfaceUrl());
            resDto.setProductcode(businessInterface.getProductCode());
            resDto.setProjectcode(businessInterface.getProjectCode());
            resDto.setInParams(paramNames);
            resDto.setOrgids(hospitals);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取接口调试显示数据成功!", resDto);
        } catch (Exception e) {
            logger.error("获取接口调试显示数据失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取接口调试显示数据失败!");
        }
    }

    @PostMapping("/interfaceDebug")
    @ApiOperation(value = "接口调试", notes = "接口调试")
    public ResultDto<String> interfaceDebug(String format){
        String result = niFiRequestUtil.interfaceDebug(format);
        if(StringUtils.isBlank(result)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE,"接口调试失败");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", result);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "标准接口删除", notes = "标准接口删除")
    @PostMapping("/delInterfaceById")
    public ResultDto<String> delInterfaceById(@ApiParam(value = "标准接口id") @RequestParam(value = "id", required = true) String id) {
        try {
            //校验该接口是否有产品关联
            TBusinessInterface tbi = businessInterfaceService.getProductIdByInterfaceId(id);
            if (tbi != null && StringUtils.isNotBlank(tbi.getId())) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该标准接口已有产品关联,无法删除!");
            }
            List<TBusinessInterface> list = businessInterfaceService.getListByInterfaceId(id);
            if (CollectionUtils.isNotEmpty(list)) {
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该标准接口已有接口配置关联,无法删除!");
            }
            //删除接口
            this.delete(id);
            //产品与标准接口关联
            productInterfaceLinkService.deleteProductInterfaceLinkById(id);
        } catch (Exception e) {
            logger.error("厂商删除失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "标准接口删除失败!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口删除成功!", id);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "标准接口新增/编辑", notes = "标准接口新增/编辑")
    @PostMapping("/saveAndUpdateInterface")
    public ResultDto<String> saveAndUpdateInterface(@RequestBody InterfaceDto dto) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String interfaceName = dto.getInterfaceName();
        if (StringUtils.isBlank(interfaceName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "接口名为空!", "接口名为空!");
        }
        String id = dto.getId();
        if (StringUtils.isBlank(id)) {
            return this.saveInterface(dto, loginUserName);
        }
        return this.updateInterface(dto, loginUserName);
    }

    /** 新增标准接口 */
    private ResultDto saveInterface(InterfaceDto dto, String loginUserName) {
        String interfaceName = dto.getInterfaceName();
        if (null != this.getInterfaceByName(interfaceName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口名已存在!", "该接口名已存在!");
        }
        //出参
        List<TInterfaceParam> outParamList = dto.getOutParamList();
        if (CollectionUtils.isEmpty(outParamList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "出参不能为空!", "出参不能为空!");
        }

        //校验出入参格式字符串是否为json或者xml
        //入参格式非必填
        String inParamFormat = dto.getInParamFormat();
        //出参格式必填
        String outParamFormat = dto.getOutParamFormat();
        if (StringUtils.isNotBlank(inParamFormat)) {
            PlatformUtil.strIsJsonOrXml(inParamFormat);
        }
        PlatformUtil.strIsJsonOrXml(outParamFormat);
        //新增标准接口
        String interfaceId = batchUidService.getUid(qTInterface.getTableName()) + "";
        TInterface ti = new TInterface();
        ti.setId(interfaceId);
        ti.setInterfaceName(dto.getInterfaceName());
        ti.setTypeId(dto.getTypeId());
        ti.setInterfaceUrl(dto.getInterfaceUrl());
        ti.setInParamFormat(inParamFormat);
        ti.setOutParamFormat(outParamFormat);
        ti.setCreatedTime(new Date());
        ti.setCreatedBy(loginUserName);

        //新增产品与接口关联
        List<String> productIdArr = dto.getProductIds();
        for (int i = 0; i < productIdArr.size(); i++) {
            TProductInterfaceLink tpil = new TProductInterfaceLink();
            tpil.setId(batchUidService.getUid(qTProductInterfaceLink.getTableName()) + "");
            tpil.setProductId(productIdArr.get(i));
            tpil.setInterfaceId(interfaceId);
            tpil.setCreatedTime(new Date());
            tpil.setCreatedBy(loginUserName);
            productInterfaceLinkService.post(tpil);
        }
        //新增接口参数
        //入参
        List<TInterfaceParam> inParamList = dto.getInParamList();
        if (CollectionUtils.isNotEmpty(inParamList)) {
            //可以为空 校验
            for (int i = 0; i < inParamList.size(); i++) {
                TInterfaceParam tip = new TInterfaceParam();
                tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
                tip.setInterfaceId(interfaceId);
                TInterfaceParam obj = inParamList.get(i);
                tip.setParamName(obj.getParamName());
                tip.setParamType(obj.getParamType());
                tip.setParamInstruction(obj.getParamInstruction());
                tip.setParamInOut(Constant.ParmInOut.IN);
                tip.setCreatedTime(new Date());
                tip.setCreatedBy(loginUserName);
                interfaceParamService.post(tip);
            }
        }
        //出参
        for (int i = 0; i < outParamList.size(); i++) {
            TInterfaceParam tip = new TInterfaceParam();
            tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
            tip.setInterfaceId(interfaceId);
            TInterfaceParam obj = outParamList.get(i);
            tip.setParamName(obj.getParamName());
            tip.setParamType(obj.getParamType());
            tip.setParamInstruction(obj.getParamInstruction());
            tip.setParamInOut(Constant.ParmInOut.OUT);
            tip.setCreatedTime(new Date());
            tip.setCreatedBy(loginUserName);
            interfaceParamService.post(tip);
            //是否开启 1开
            if ("1".equals(obj.getIsStart())) {
                //存储参数到标准接口表
                ti.setParamOutStatus(obj.getParamName());
                ti.setParamOutStatusSuccess(obj.getParamOutStatusSuccess());
            }
        }
        //新增标准接口
        this.post(ti);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口新增成功!", null);
    }

    /** 修改标准接口 */
    private ResultDto updateInterface(InterfaceDto dto, String loginUserName) {
        String id = dto.getId();
        TInterface tf = this.getOne(id);
        if (tf == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据传入id未查出对应标准接口,检查是否传入错误!", "根据传入id未查出对应标准接口,检查是否传入错误!");
        }
        //传入标准接口名
        String interfaceName = dto.getInterfaceName();
        //查询新接口名是否已存在
        tf = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .where(qTInterface.interfaceName.eq(interfaceName).and(qTInterface.id.notEqualsIgnoreCase(id)))
                .fetchOne();
        if (tf != null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口名已存在!", "该接口名已存在!");
        }
        //出参
        List<TInterfaceParam> outParamList = dto.getOutParamList();
        if (CollectionUtils.isEmpty(outParamList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "出参不能为空!", "出参不能为空!");
        }

        String interfaceTypeId = dto.getTypeId();
        String interfaceUrl = dto.getInterfaceUrl();
        //校验出入参格式字符串是否为json或者xml
        String inParamFormat = dto.getInParamFormat();
        String outParamFormat = dto.getOutParamFormat();
        if (StringUtils.isNotBlank(inParamFormat)) {
            PlatformUtil.strIsJsonOrXml(inParamFormat);
        }
        PlatformUtil.strIsJsonOrXml(outParamFormat);

        //修改标准接口信息
        long execute = sqlQueryFactory.update(qTInterface)
                    .set(qTInterface.interfaceName, interfaceName)
                    .set(qTInterface.typeId, interfaceTypeId)
                    .set(qTInterface.interfaceUrl, interfaceUrl)
                    .set(qTInterface.inParamFormat, inParamFormat)
                    .set(qTInterface.outParamFormat, outParamFormat)
                    .set(qTInterface.updatedTime, new Date())
                    .set(qTInterface.paramOutStatus, "")
                    .set(qTInterface.paramOutStatusSuccess, "")
                    .set(qTInterface.updatedBy, loginUserName)
                    .where(qTInterface.id.eq(id)).execute();
        if (execute < 1) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "修改标准接口信息失败!", "修改标准接口信息失败!");
        }
        //替换产品与接口关联
        productInterfaceLinkService.deleteProductInterfaceLinkById(id);
        List<String> productIdArr = dto.getProductIds();
        for (int i = 0; i < productIdArr.size(); i++) {
            TProductInterfaceLink tpil = new TProductInterfaceLink();
            tpil.setId(batchUidService.getUid(qTProductInterfaceLink.getTableName()) + "");
            tpil.setProductId(productIdArr.get(i));
            tpil.setInterfaceId(id);
            tpil.setCreatedTime(new Date());
            tpil.setCreatedBy(loginUserName);
            productInterfaceLinkService.post(tpil);
        }
        //替换接口参数
        interfaceParamService.deleteProductInterfaceLinkById(id);
        //入参
        List<TInterfaceParam> inParamList = dto.getInParamList();
        if (CollectionUtils.isNotEmpty(inParamList)) {
            for (int i = 0; i < inParamList.size(); i++) {
                TInterfaceParam tip = new TInterfaceParam();
                tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
                tip.setInterfaceId(id);
                TInterfaceParam obj = inParamList.get(i);
                tip.setParamName(obj.getParamName());
                tip.setParamType(obj.getParamType());
                tip.setParamInstruction(obj.getParamInstruction());
                tip.setParamInOut(Constant.ParmInOut.IN);
                tip.setCreatedTime(new Date());
                tip.setCreatedBy(loginUserName);
                interfaceParamService.post(tip);
            }
        }
        //出参
        for (int i = 0; i < outParamList.size(); i++) {
            TInterfaceParam tip = new TInterfaceParam();
            tip.setId(batchUidService.getUid(qTInterfaceParam.getTableName()) + "");
            tip.setInterfaceId(id);
            TInterfaceParam obj = outParamList.get(i);
            tip.setParamName(obj.getParamName());
            tip.setParamType(obj.getParamType());
            tip.setParamInstruction(obj.getParamInstruction());
            tip.setParamInOut(Constant.ParmInOut.OUT);
            tip.setCreatedTime(new Date());
            tip.setCreatedBy(loginUserName);
            interfaceParamService.post(tip);
            //入参该字段表示是否开启 1开
            if ("1".equals(obj.getIsStart())) {
                //标准接口信息出参赋值
                String paramName = obj.getParamName();
                String paramOutStatusSuccess = obj.getParamOutStatusSuccess();
                sqlQueryFactory.update(qTInterface)
                        .set(qTInterface.paramOutStatus, StringUtils.isBlank(paramName)?"":paramName)
                        .set(qTInterface.paramOutStatusSuccess, StringUtils.isBlank(paramOutStatusSuccess)?"":paramOutStatusSuccess)
                        .where(qTInterface.id.eq(id)).execute();
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "标准接口修改成功!", id);
    }


    @ApiOperation(value = "获取接口分类")
    @GetMapping("/getInterfaceType")
    public ResultDto<List<TInterfaceType>> getInterfaceType() {
        List<TInterfaceType> vendors = sqlQueryFactory.select(
                Projections.bean(
                        TInterfaceType.class,
                        qTInterfaceType.id,
                        qTInterfaceType.interfaceTypeCode,
                        qTInterfaceType.interfaceTypeName
                )
        ).from(qTInterfaceType).orderBy(qTInterfaceType.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
    }


    @ApiOperation(value = "标准接口列表")
    @GetMapping("/getInterfaceList")
    public ResultDto<TableData<TInterface>> getInterfaceList(
              @ApiParam(value = "接口分类id") @RequestParam(value = "typeId", required = false) String typeId,
              @ApiParam(value = "接口名称") @RequestParam(value = "name", required = false) String name,
              @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
              @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        //查询条件
        ArrayList<Predicate> list = new ArrayList<>();
        if(StringUtils.isNotEmpty(typeId)){
            list.add(qTInterface.typeId.eq(typeId));
        }
        if(StringUtils.isNotEmpty(name)){
            list.add(qTInterface.interfaceName.like(PlatformUtil.createFuzzyText(name)));
        }
        QueryResults<TInterface> queryResults = sqlQueryFactory.select(
            Projections.bean(
                    TInterface.class,
                    qTInterface.id,
                    qTInterface.interfaceName,
                    qTInterface.interfaceUrl,
                    qTInterface.inParamFormat,
                    qTInterface.outParamFormat,
                    qTInterface.createdTime,
                    qTInterface.typeId,
                    qTType.typeName.as("interfaceTypeName"),
                    sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                            .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.IN))
                            .and(qTInterfaceParam.interfaceId.eq(qTInterface.id))).as("inParamCount"),
                    sqlQueryFactory.select(qTInterfaceParam.id.count()).from(qTInterfaceParam)
                            .where((qTInterfaceParam.paramInOut.eq(Constant.ParmInOut.OUT))
                            .and(qTInterfaceParam.interfaceId.eq(qTInterface.id))).as("outParamCount")
                )
            ).from(qTInterface)
                    .leftJoin(qTType).on(qTType.id.eq(qTInterface.typeId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .groupBy(qTInterface.id)
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTInterface.createdTime.desc())
                    .fetchResults();
        //分页
        TableData<TInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"标准接口列表获取成功!", tableData);
    }


    @ApiOperation(value = "获取接口配置列表")
    @GetMapping("/getInterfaceConfigureList")
    public ResultDto<TableData<TBusinessInterface>> getInterfaceConfigureList(
            @ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
            @ApiParam(value = "启停用状态") @RequestParam(value = "status", required = false) String status,
            @ApiParam(value = "mock状态") @RequestParam(value = "mockStatus", required = false) String mockStatus,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        //获取接口配置列表信息
        QueryResults<TBusinessInterface> queryResults = businessInterfaceService.getInterfaceConfigureList(platformId, status, mockStatus, pageNo, pageSize);
        //匹配列表展示信息
        List<TBusinessInterface> list = queryResults.getResults();
        for (TBusinessInterface tbi : list) {
            //获取产品+功能
            if (StringUtils.isNotBlank(tbi.getProductFunctionLinkId())) {
                TProductFunctionLink tpfl = sqlQueryFactory.select(Projections.bean(
                        qTProductFunctionLink, qTProduct.productName.as("productName"), qTFunction.functionName.as("functionName")))
                        .from(qTProductFunctionLink)
                        .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                        .leftJoin(qTFunction).on(qTFunction.id.eq(qTProductFunctionLink.functionId))
                        .where(qTProductFunctionLink.id.eq(tbi.getProductFunctionLinkId())).fetchFirst();
                if (tpfl != null) {
                    tbi.setProductName(tpfl.getProductName());
                    tbi.setFunctionName(tpfl.getFunctionName());
                }
            }
            //获取标准接口
            if (StringUtils.isNotBlank(tbi.getInterfaceId())) {
                TInterface ti = this.getOne(tbi.getInterfaceId());
                if (ti != null) {
                    tbi.setInterfaceName(ti.getInterfaceName());
                }
            }
        }
        //分页
        TableData<TBusinessInterface> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"获取接口配置列表获取成功", tableData);
    }


    @ApiOperation(value = "获取接口配置详情")
    @GetMapping("/getInterfaceConfigInfoById")
    public ResultDto<BusinessInterfaceDto> getInterfaceConfigInfoById(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        //返回对象
        BusinessInterfaceDto dto = new BusinessInterfaceDto();
        dto.setId(id);

        //多个厂商接口信息
        List<TBusinessInterface> tbiList = new ArrayList<>();

        //获取接口配置对象
        TBusinessInterface tbi = businessInterfaceService.getOne(id);
        if (tbi != null) {
            //标准接口
            dto.setInterfaceId(tbi.getInterfaceId());
            //获取厂商及配置信息
            String vendorConfigId = tbi.getVendorConfigId();
            dto.setVendorConfigId(vendorConfigId);
            TVendorConfig tvc = vendorConfigService.getOne(vendorConfigId);
            if (tvc != null) {
                dto.setVendorId(tvc.getVendorId());
            }
            //获取产品与功能及关联信息
            String productFunctionLinkId = tbi.getProductFunctionLinkId();
            dto.setProductFunctionLinkId(productFunctionLinkId);
            TProductFunctionLink tpfl = productFunctionLinkService.getOne(productFunctionLinkId);
            if (tpfl != null) {
                dto.setProductId(tpfl.getProductId());
                dto.setFunctionId(tpfl.getFunctionId());
            }
            //多个厂商配置信息
            String pflId = tbi.getProductFunctionLinkId();
            String iId = tbi.getInterfaceId();
            String vcId = tbi.getVendorConfigId();
            tbiList = businessInterfaceService.getTBusinessInterfaceList(pflId, iId, vcId);
        }

        dto.setBusinessInterfaceList(tbiList);

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"获取接口配置详情成功", dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/编辑接口配置", notes = "新增/编辑接口配置")
    @PostMapping("/saveAndUpdateInterfaceConfig")
    public ResultDto<String> saveAndUpdateInterfaceConfig(@RequestBody BusinessInterfaceDto dto) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求参数不能为空!");
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        List<TBusinessInterface> tbiList = dto.getBusinessInterfaceList();
        if (Constant.Operation.ADD.equals(dto.getAddOrUpdate())) {
            return this.saveInterfaceConfig(dto, loginUserName);
        }
        if (Constant.Operation.UPDATE.equals(dto.getAddOrUpdate())) {
            return this.updateInterfaceConfig(dto, loginUserName);
        }
        return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "addOrUpdate 新增编辑标识不正确!", null);
    }

    /**
     * 新增接口配置
     * @param dto
     * @param loginUserName
     * @return
     */
    private ResultDto saveInterfaceConfig(BusinessInterfaceDto dto, String loginUserName) {
        //获取厂商配置
        String vendorConfigId = "";
        if (StringUtils.isBlank(dto.getVendorConfigId())) {
            TVendorConfig tvc = vendorConfigService.getObjByPlatformAndVendor(dto.getPlatformId(), dto.getVendorId());
            vendorConfigId = tvc!=null?tvc.getId():null;
        }else {
            vendorConfigId = dto.getVendorConfigId();
        }
        //产品与功能关联
        TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(dto.getProductId(), dto.getFunctionId());
        String productFunctionLinkId = tpfl!=null?tpfl.getId():null;
//        String productFunctionLinkId = "";
//        if (StringUtils.isBlank(dto.getProductFunctionLinkId())) {
//            TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(dto.getProductId(), dto.getFunctionId());
//            productFunctionLinkId = tpfl!=null?tpfl.getId():null;
//        }else {
//            productFunctionLinkId = dto.getProductFunctionLinkId();
//        }

        //根据项目,厂商,标准接口判定是否存在相同配置数据
        List<THospitalVendorLink> thvlList = hospitalVendorLinkService.getTHospitalVendorLinkByVendorConfigId(vendorConfigId);
        //根据条件判断是否存在该数据
        List<TBusinessInterface> tbiList = businessInterfaceService.getBusinessInterfaceIsExist(thvlList, dto.getProjectId(), dto.getProductId(), dto.getInterfaceId());
        if (CollectionUtils.isNotEmpty(tbiList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据项目id,产品id,标准接口id匹配到该条件数据已存在!", null);
        }

        tbiList = dto.getBusinessInterfaceList();
        for (int i = 0; i < tbiList.size(); i++) {
            TBusinessInterface tbi = tbiList.get(i);
            //新增接口配置
            tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName())+"");
            tbi.setInterfaceId(dto.getInterfaceId());
            tbi.setStatus(Constant.Status.START);
            tbi.setMockStatus(Constant.Status.STOP);
            tbi.setCreatedTime(new Date());
            tbi.setCreatedBy(loginUserName);
            tbi.setVendorConfigId(vendorConfigId);
            tbi.setProductFunctionLinkId(productFunctionLinkId);
            tbi.setExcErrOrder(i);
            //获取schema
            niFiRequestUtil.generateSchemaToInterface(tbi);
            //新增接口配置
            businessInterfaceService.post(tbi);
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增接口配置成功", null);
    }

    /**
     * 编辑接口配置
     * @param dto
     * @param loginUserName
     * @return
     */
    private ResultDto updateInterfaceConfig(BusinessInterfaceDto dto, String loginUserName) {
        if (StringUtils.isBlank(dto.getPlatformId()) || StringUtils.isBlank(dto.getVendorId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "平台id或厂商id不能为空!", null);
        }
        //获取厂商配置
        TVendorConfig tvc = vendorConfigService.getObjByPlatformAndVendor(dto.getPlatformId(), dto.getVendorId());
        String vendorConfigId = tvc!=null?tvc.getId():dto.getVendorConfigId();
        //产品与功能关联
        TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(dto.getProductId(), dto.getFunctionId());
        String productFunctionLinkId = tpfl!=null?tpfl.getId():null;
//        String productFunctionLinkId = "";
//        if (StringUtils.isBlank(dto.getProductFunctionLinkId())) {
//            TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(dto.getProductId(), dto.getFunctionId());
//            productFunctionLinkId = tpfl!=null?tpfl.getId():null;
//        }else {
//            productFunctionLinkId = dto.getProductFunctionLinkId();
//        }

        //返回缓存接口配置id
        String rtnId = "";
        List<TBusinessInterface> tbiList = dto.getBusinessInterfaceList();
        for (int i = 0; i < tbiList.size(); i++) {
            TBusinessInterface tbi = tbiList.get(i);
            if (StringUtils.isBlank(tbi.getId())) {
                //新增的厂商配置
                tbi.setId(batchUidService.getUid(qTBusinessInterface.getTableName())+"");
                tbi.setInterfaceId(dto.getInterfaceId());
                tbi.setStatus(Constant.Status.START);
                tbi.setMockStatus(Constant.Status.STOP);
                tbi.setCreatedTime(new Date());
                tbi.setCreatedBy(loginUserName);
                tbi.setVendorConfigId(vendorConfigId);
                tbi.setProductFunctionLinkId(productFunctionLinkId);
                tbi.setExcErrOrder(i);
                //获取schema
                niFiRequestUtil.generateSchemaToInterface(tbi);
                //新增接口配置
                businessInterfaceService.post(tbi);
            }else {
                //接口配置重新赋值
                tbi.setInterfaceId(dto.getInterfaceId());
                tbi.setUpdatedTime(new Date());
                tbi.setUpdatedBy(loginUserName);
                tbi.setVendorConfigId(vendorConfigId);
                tbi.setProductFunctionLinkId(productFunctionLinkId);
                tbi.setExcErrOrder(i);
                //获取schema
                niFiRequestUtil.generateSchemaToInterface(tbi);
                //新增接口配置
                businessInterfaceService.put(tbi.getId(), tbi);
                rtnId += tbi.getId() + ",";
            }
        }
        rtnId = StringUtils.isBlank(rtnId)?null:rtnId.substring(0, rtnId.length()-1);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "编辑接口配置成功", rtnId);
    }


    @ApiOperation(value = "根据参数格式获取jolt", notes = "根据参数格式获取jolt")
    @PostMapping("/paramFormatJolt")
    public ResultDto<String> paramFormatJolt(String paramFormat, String content,
                             @RequestParam(value = "joltType", defaultValue = "request", required = false) String joltType){
        String contentType = Constant.ParamFormatType.getByType(content);
        if(StringUtils.isBlank(contentType) || Constant.ParamFormatType.NONE.getType().equals(contentType)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "参数类型无效!", "参数类型无效!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "jolt获取成功!", niFiRequestUtil.generateJolt(paramFormat, contentType, joltType));
    }


    @ApiOperation(value = "接口配置删除", notes = "接口配置删除")
    @PostMapping("/deleteInterfaceConfigure")
    public ResultDto<String> deleteInterfaceConfigure(
            @ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface tbi = businessInterfaceService.getOne(id);
        if (tbi == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该接口id查询不到接口配置信息!", "该接口id查询不到接口配置信息!");
        }
        List<TBusinessInterface> list = businessInterfaceService.getListByCondition(tbi.getProductFunctionLinkId(), tbi.getInterfaceId(), tbi.getVendorConfigId());
        //删除相同条件接口配置
        long count = businessInterfaceService.delObjByCondition(
                tbi.getProductFunctionLinkId(), tbi.getInterfaceId(), tbi.getVendorConfigId());
        if(count <= 0){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "接口配置删除失败!", "接口配置删除失败!");
        }
        //获取返回缓存id
        String rtnStr = "";
        if (CollectionUtils.isNotEmpty(list)) {
            for (TBusinessInterface obj : list) {
                rtnStr += obj.getId() + ",";
            }
        }
        rtnStr = StringUtils.isBlank(rtnStr)?null:rtnStr.substring(0,rtnStr.length()-1);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "接口配置删除成功!共删除"+count+"条数据", rtnStr);
    }


    @ApiOperation(value = "根据id删除单个接口配置信息", notes = "根据id删除单个接口配置信息")
    @PostMapping("/deleteBusinessInterfaceById")
    public ResultDto<String> deleteBusinessInterfaceById(@ApiParam(value = "接口配置id") @RequestParam(value = "id", required = true) String id) {
        TBusinessInterface tbi = businessInterfaceService.getOne(id);
        if (tbi == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该接口配置信息!", id);
        }
        long count = businessInterfaceService.delete(id);
        if (count < 1) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id删除该接口配置信息失败!", id);
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "单个接口配置信息删除成功!", id);
    }


    @ApiOperation(value = "获取标准接口详情", notes = "获取标准接口详情")
    @GetMapping("/getInterfaceInfoById")
    public ResultDto<InterfaceDto> getInterfaceInfoById(@ApiParam(value = "标准接口id") @RequestParam(value = "id", required = true) String id) {
        TInterface ti = this.getOne(id);
        if (ti == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该标准接口!");
        }
        try {
            InterfaceDto iDto = new InterfaceDto();
            BeanUtils.copyProperties(ti, iDto);
            //获取产品id
            List<TProductInterfaceLink> list = productInterfaceLinkService.getObjByInterface(id);
            List<String> productIds = new ArrayList<>();
            String productIdStr = "";
            for (int i = 0; i < list.size(); i++) {
                productIds.add(list.get(i).getProductId());
                productIdStr += list.get(i).getProductId();
                if (i < list.size() - 1) {
                    productIdStr += ",";
                }
            }
            iDto.setProductId(productIdStr);
            iDto.setProductIds(productIds);
            //获取接口参数
            List<TInterfaceParam> paramsList = interfaceParamService.getParamsByInterfaceId(id);
            //入参
            List<TInterfaceParam> inParamList = new ArrayList<>();
            //出参
            List<TInterfaceParam> outParamList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(paramsList)) {
                TInterfaceParam tip;
                for (TInterfaceParam obj : paramsList) {
                    tip = new TInterfaceParam();
                    tip.setParamName(obj.getParamName());
                    tip.setParamType(obj.getParamType());
                    tip.setParamInstruction(obj.getParamInstruction());
                    tip.setParamLength(obj.getParamLength());
                    tip.setParamInOut(obj.getParamInOut());
                    if (iDto.getParamOutStatus().equals(obj.getParamName())) {
                        //开启状态
                        tip.setIsStart("1");
                        tip.setParamOutStatus(obj.getParamOutStatus());
                        tip.setParamOutStatusSuccess(obj.getParamOutStatusSuccess());
                    }
                    if (Constant.ParmInOut.IN.equals(obj.getParamInOut())) {
                        inParamList.add(tip);
                    }
                    if (Constant.ParmInOut.OUT.equals(obj.getParamInOut())) {
                        outParamList.add(tip);
                    }
                }
            }
            iDto.setInParamList(inParamList);
            iDto.setOutParamList(outParamList);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取标准接口详情成功!", iDto);
        } catch (Exception e) {
            logger.error("获取标准接口详情失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取标准接口详情失败!");
        }
    }


    @ApiOperation(value = "选择接口下拉(可根据当前项目操作选择)")
    @GetMapping("/getDisInterface")
    public ResultDto<List<TInterface>> getDisInterface(@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = false) String projectId,
                    @ApiParam(value = "操作 1获取当前项目下的接口 2获取非当前项目下的接口") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
        List<TInterface> interfaces = null;
        if (StringUtils.isNotBlank(projectId) && Constant.Operation.CURRENT.equals(status)) {
            //返回当前项目下的接口
            interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .leftJoin(qTBusinessInterface).on(qTBusinessInterface.interfaceId.eq(qTInterface.id))
                    .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTBusinessInterface.productFunctionLinkId))
                    .where(qTProjectProductLink.projectId.eq(projectId))
                    .orderBy(qTInterface.createdTime.desc()).fetch();
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", interfaces);
        }
        //获取所有接口
        interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface).orderBy(qTInterface.createdTime.desc()).fetch();
        if (StringUtils.isBlank(projectId)) {
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", interfaces);
        }
        //返回当前项目下的接口
        List<TInterface> tiList = sqlQueryFactory.select(qTInterface).from(qTInterface)
                    .leftJoin(qTBusinessInterface).on(qTBusinessInterface.interfaceId.eq(qTInterface.id))
                    .leftJoin(qTProjectProductLink).on(qTProjectProductLink.productFunctionLinkId.eq(qTBusinessInterface.productFunctionLinkId))
                    .where(qTProjectProductLink.projectId.eq(projectId))
                    .orderBy(qTInterface.createdTime.desc()).fetch();
        //去除当前项目下的接口
        interfaces.removeAll(tiList);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", interfaces);
    }


    @ApiOperation(value = "根据参数模板（json）获取key-value", notes = "根据参数模板（json）获取key-value")
    @PostMapping("/jsonFormat")
    public ResultDto<List<ParamsDto>> jsonFormat(String paramJson){
        if(StringUtils.isBlank(paramJson)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "参数为空");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"", PlatformUtil.jsonFormat(paramJson));
    }


    @ApiOperation(value = "根据产品获取标准接口(新增接口)")
    @GetMapping("/getInterByPro")
    public ResultDto<List<TInterface>> getInterByPro(@ApiParam(value = "产品id") @RequestParam(value = "productId", required = false) String productId) {
        ArrayList<Predicate> list = new ArrayList<>();
        if(StringUtils.isNotEmpty(productId)){
            list.add(qTProductInterfaceLink.productId.eq(productId));
        }
        List<TInterface> interfaces = sqlQueryFactory.select(qTInterface).from(qTInterface)
                .leftJoin(qTProductInterfaceLink).on(qTProductInterfaceLink.interfaceId.eq(qTInterface.id))
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"根据产品获取功能成功", interfaces);
    }

    /**
     * 根据接口名获取标准接口
     * @param interfaceName
     * @return
     */
    private TInterface getInterfaceByName(String interfaceName) {
        if (StringUtils.isBlank(interfaceName)) {
            return null;
        }
        return sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.interfaceName.eq(interfaceName)).fetchFirst();
    }


}
