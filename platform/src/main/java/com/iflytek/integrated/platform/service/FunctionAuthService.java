package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.TFunctionAuth;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTFunctionAuth.qtFunctionAuth;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;
import static com.iflytek.integrated.platform.entity.QTType.qTType;

@Slf4j
@Api(tags = "功能权限管理")
@RestController
@RequestMapping("/{version}/pt/functionAuth")
public class FunctionAuthService extends BaseService<TFunctionAuth, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(FunctionAuthService.class);

    @Autowired
    private BatchUidService batchUidService;

    public FunctionAuthService() {
        super(qtFunctionAuth, qtFunctionAuth.id);
    }

    @ApiOperation(value = "功能权限列表")
    @GetMapping("/getFunctionAuthList")
    public ResultDto<TableData<TFunctionAuth>> getFunctionAuthList(
            @ApiParam(value = "业务类型id") @RequestParam(value = "typeId", required = false) String typeId,
            @ApiParam(value = "服务id") @RequestParam(value = "interfaceId", required = false) String interfaceId,
            @ApiParam(value = "服务名称") @RequestParam(value = "interfaceName", required = false) String interfaceName,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            // 查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            if (StringUtils.isNotEmpty(typeId)) {
                list.add(qTInterface.typeId.eq(typeId));
            }
            if (StringUtils.isNotEmpty(interfaceId)) {
                list.add(qTInterface.id.eq(interfaceId));
            }
            if (StringUtils.isNotEmpty(interfaceName)) {
                list.add(qTInterface.interfaceName.like(PlatformUtil.createFuzzyText(interfaceName)));
            }

            StringTemplate template = Expressions.stringTemplate("concat(string_agg ( concat(concat({0},'/' ::TEXT),{1}), ',' :: TEXT ))",qTSys.sysName,qTBusinessInterface.businessInterfaceName);
            QueryResults<TFunctionAuth> queryResults = sqlQueryFactory
                    .select(Projections.bean(TFunctionAuth.class,qtFunctionAuth.id.min().as("id"),
                            qtFunctionAuth.interfaceId.min().as(qtFunctionAuth.interfaceId),
                            qtFunctionAuth.publishId.min().as(qtFunctionAuth.publishId),
                            qTType.typeName.min().as(qTType.typeName),
                            qTBusinessInterface.requestInterfaceId,
                            qTInterface.interfaceName.min().as("interfaceName"),
                            qTBusinessInterface.createdTime.max().as(qTBusinessInterface.createdTime),
                            template.as(qTBusinessInterface.businessInterfaceName)))
                    .from(qtFunctionAuth).leftJoin(qTBusinessInterface).on(qTBusinessInterface.requestInterfaceId.eq(qtFunctionAuth.interfaceId))
                    .leftJoin(qTSysRegistry).on(qTSysRegistry.id.eq(qTBusinessInterface.sysRegistryId))
                    .leftJoin(qTSys).on(qTSys.id.eq(qTSysRegistry.sysId))
                    .leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
                    .leftJoin(qTType).on(qTType.id.eq(qTInterface.typeId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .groupBy(qTBusinessInterface.requestInterfaceId)
                    .limit(pageSize).offset((pageNo - 1) * pageSize)
                    .orderBy(qtFunctionAuth.createdTime.as("createdTime").desc()).fetchResults();

            // 分页
            TableData<TFunctionAuth> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取功能权限列表成功!", tableData);
        } catch (BeansException e) {
            logger.error("获取功能权限列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取功能权限列表失败!");
        }
    }

    @ApiOperation(value = "获取功能权限信息", notes = "获取功能权限信息")
    @GetMapping("/getFunctionAuth/{id}")
    public ResultDto<TFunctionAuth> getFunctionAuth(
            @ApiParam(value = "功能权限id") @PathVariable(value = "id", required = false) String id) {
        try {
            TFunctionAuth tFunctionAuth = this.getOne(id);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取功能权限信息成功!", tFunctionAuth);
        } catch (BeansException e) {
            logger.error("获取功能权限信息失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取功能权限信息失败!");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/修改功能权限信息", notes = "新增/修改功能权限信息")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TFunctionAuth dto) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
        }
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String id = dto.getId();
        if (StringUtils.isBlank(id)) {
            // 新增服务注册信息
            id = batchUidService.getUid(qTSysRegistry.getTableName()) + "";
            dto.setId(id);
            dto.setCreatedTime(new Date());
            dto.setCreatedBy(loginUserName);
            this.post(dto);
        } else {
            //修改
            dto.setUpdatedTime(new Date());
            dto.setUpdatedBy(loginUserName);
            long l = this.put(id, dto);
            if (l < 1) {
                throw new RuntimeException("功能权限编辑失败!");
            }
        }
        Map<String , String> data = new HashMap<String , String>();
        data.put("id", id);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存功能权限信息成功!", JSON.toJSONString(data));
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "功能权限删除", notes = "功能权限删除")
    @GetMapping("/delById/{id}")
    public ResultDto<String> delById(
            @ApiParam(value = "功能权限id") @PathVariable(value = "id", required = true) String id) {
        // 删除接口
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("功能权限删除成功!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "功能权限删除成功!");
    }
}
