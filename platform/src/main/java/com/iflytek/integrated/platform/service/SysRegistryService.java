package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.CacheDeleteService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.CacheDeleteDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TSysRegistry;
import com.iflytek.integrated.platform.utils.PlatformUtil;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;

@Slf4j
@Api(tags = "服务注册管理")
@RestController
@RequestMapping("/{version}/pt/sysRegistry")
public class SysRegistryService extends BaseService<TSysRegistry, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(SysRegistryService.class);

    @Autowired
    private BusinessInterfaceService businessInterfaceService;

    @Autowired
    private BatchUidService batchUidService;

    @Autowired
    private CacheDeleteService cacheDeleteService;

    public SysRegistryService() {
        super(qTSysRegistry, qTSysRegistry.id);
    }

    @ApiOperation(value = "服务注册列表")
    @GetMapping("/getRegistryList")
    public ResultDto<TableData<TSysRegistry>> getInterfaceList(
            @ApiParam(value = "系统id") @RequestParam(value = "sysId", required = false) String sysId,
            @ApiParam(value = "注册id") @RequestParam(value = "registryId", required = false) String registryId,
            @ApiParam(value = "注册名称") @RequestParam(value = "registryName", required = false) String registryName,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            // 查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            if (StringUtils.isNotEmpty(sysId)) {
                list.add(qTSysRegistry.sysId.eq(sysId));
            }
            if (StringUtils.isNotEmpty(registryId)) {
                list.add(qTSysRegistry.id.eq(registryId));
            }
            if (StringUtils.isNotEmpty(registryName)) {
                list.add(qTSysRegistry.registryName.like(PlatformUtil.createFuzzyText(registryName)));
            }

            QueryResults<TSysRegistry> queryResults = sqlQueryFactory
                    .select(Projections
                            .bean(TSysRegistry.class, qTSysRegistry.id, qTSysRegistry.registryName,
                                    qTSysRegistry.sysId, qTSys.sysName,qTSys.sysCode, qTSysRegistry.connectionType,
                                    qTSysRegistry.addressUrl, qTSysRegistry.endpointUrl,
                                    qTSysRegistry.namespaceUrl, qTSysRegistry.databaseName, qTSysRegistry.databaseUrl,
                                    qTSysRegistry.databaseDriver, qTSysRegistry.driverUrl, qTSysRegistry.databaseType, qTSysRegistry.jsonParams,
                                    qTSysRegistry.userName, qTSysRegistry.userPassword, qTSysRegistry.createdBy,
                                    qTSysRegistry.createdTime, qTSysRegistry.updatedBy, qTSysRegistry.updatedTime, qTSysRegistry.useStatus))
                    .from(qTSysRegistry).leftJoin(qTSys)
                    .on(qTSysRegistry.sysId.eq(qTSys.id))
                    .where(list.toArray(new Predicate[list.size()]))
                    .offset((pageNo - 1) * pageSize).orderBy(qTSysRegistry.createdTime.desc()).fetchResults();
            if (!queryResults.isEmpty()) {
                //如果是数据库视图  则url显示数据的url
                for (TSysRegistry result : queryResults.getResults()) {
                    if (Constant.ConnectionType.VIEW.getCode().equals(result.getConnectionType())) {
                        result.setAddressUrl(result.getDatabaseUrl());
                    }
                }
            }
            // 分页
            TableData<TSysRegistry> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务注册列表成功!", tableData);
        } catch (BeansException e) {
            logger.error("获取服务注册列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务注册列表失败!");
        }
    }

    @ApiOperation(value = "获取服务注册信息", notes = "获取服务注册信息")
    @GetMapping("/getSysRegistry/{id}")
    public ResultDto<TSysRegistry> getSysRegistry(
            @ApiParam(value = "服务注册id") @PathVariable(value = "id", required = false) String id) {
        try {
            TSysRegistry tSysRegistry = this.getOne(id);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务注册信息成功!", tSysRegistry);
        } catch (BeansException e) {
            logger.error("获取服务注册信息失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务注册信息失败!");
        }
    }

    @ApiOperation(value = "获取服务注册下拉列表", notes = "获取服务注册下拉列表")
    @GetMapping("/getRegistrySelect")
    public ResultDto<List<TSysRegistry>> getRegistryList(
            @ApiParam(value = "服务注册名称") @RequestParam(value = "registryName", required = false) String registryName,
            @ApiParam(value = "使用状态") @RequestParam(value = "useStatus", required = false, defaultValue = "1") String useStatus) {
        try {
            ArrayList<Predicate> pre = new ArrayList<>();
            if (StringUtils.isNotEmpty(registryName)) {
                pre.add(qTSysRegistry.registryName.like(PlatformUtil.createFuzzyText(registryName)));
            }
            if (StringUtils.isNotEmpty(useStatus)) {
                pre.add(qTSysRegistry.useStatus.eq(useStatus));
            }
            List<TSysRegistry> list = sqlQueryFactory
                    .select(Projections.bean(TSysRegistry.class, qTSysRegistry.id, qTSysRegistry.sysId, qTSysRegistry.registryName,qTSysRegistry.connectionType))
                    .from(qTSysRegistry)
                    .where(pre.toArray(new Predicate[pre.size()]))
                    .orderBy(qTSysRegistry.createdTime.desc())
                    .fetch();
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务注册下拉列表成功!", list);
        } catch (BeansException e) {
            logger.error("获取服务注册下拉列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务注册下拉列表失败!");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/修改服务注册信息", notes = "新增/修改服务注册信息")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TSysRegistry dto,@RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String registryId = dto.getId();
        if (StringUtils.isBlank(registryId)) {
            // 新增服务注册信息
            registryId = batchUidService.getUid(qTSysRegistry.getTableName()) + "";
            dto.setId(registryId);
            dto.setCreatedTime(new Date());
            dto.setCreatedBy(loginUserName);
            this.post(dto);
        } else {
            //修改
            dto.setUpdatedTime(new Date());
            dto.setUpdatedBy(loginUserName);
            long l = this.put(registryId, dto);
            if (l < 1) {
                throw new RuntimeException("服务注册编辑失败!");
            }
            //删除缓存
            cacheDelete(registryId);
        }
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", registryId);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存服务注册信息成功!", JSON.toJSONString(data));
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "服务注册删除", notes = "服务注册删除")
    @PostMapping("/delById/{id}")
    public ResultDto<String> delById(
            @ApiParam(value = "服务id") @PathVariable(value = "id", required = true) String id) {
        //先校验是否跟集成配置关联
        List<TBusinessInterface> list = businessInterfaceService.getListBySysRegistryId(id);
        if (CollectionUtils.isNotEmpty(list)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该注册服务已有集成配置关联,无法删除!", "该注册服务已有集成配置关联,无法删除!");
        }
        //删除缓存
        cacheDelete(id);

        // 删除接口
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("注册服务删除成功!");
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "注册服务删除成功!");
    }

    public TSysRegistry getOneBySysId(String sysId) {
        return sqlQueryFactory
                .select(Projections.bean(TSysRegistry.class, qTSysRegistry.id, qTSysRegistry.sysId, qTSysRegistry.registryName))
                .from(qTSysRegistry)
                .where(qTSysRegistry.sysId.eq(sysId))
                .fetchFirst();
    }

    private void cacheDelete(String id) {
        //根据服务注册查询 集成配置 找到对应的服务
        List<TBusinessInterface> businessInterfaces = businessInterfaceService.getListBySysRegistryId(id);
        if(CollectionUtils.isEmpty(businessInterfaces)){
            return;
        }
        List<String> funcodes = businessInterfaces.stream().map(TBusinessInterface::getInterfaceUrl).collect(Collectors.toList());
        CacheDeleteDto keyDto = new CacheDeleteDto();
        keyDto.setInterfaceCodes(funcodes);
        //需要删除下面两种缓存key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }
}
