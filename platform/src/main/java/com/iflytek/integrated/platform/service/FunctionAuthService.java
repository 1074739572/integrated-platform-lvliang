package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.CacheDeleteService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.CacheDeleteDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TFunctionAuth;
import com.iflytek.integrated.platform.entity.TSysPublish;
import com.iflytek.integrated.platform.entity.TSysRegistry;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
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
import org.springframework.util.CollectionUtils;
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

import static com.iflytek.integrated.platform.entity.QTFunctionAuth.qtFunctionAuth;
import static com.iflytek.integrated.platform.entity.QTSysPublish.qTSysPublish;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;

@Slf4j
@Api(tags = "功能权限管理")
@RestController
@RequestMapping("/{version}/pt/functionAuth")
public class FunctionAuthService extends BaseService<TFunctionAuth, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(FunctionAuthService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private SysPublishService sysPublishService;
    @Autowired
    private CacheDeleteService cacheDeleteService;

    public FunctionAuthService() {
        super(qtFunctionAuth, qtFunctionAuth.id);
    }

    @ApiOperation(value = "获取功能权限信息", notes = "获取功能权限信息")
    @GetMapping("/getFunctionAuthList")
    public ResultDto<TableData<TFunctionAuth>> getFunctionAuth(
            @ApiParam(value = "服务id") @RequestParam(value = "interfaceId", required = true) String interfaceId,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            QueryResults<TFunctionAuth> queryResults = sqlQueryFactory
                    .select(Projections.bean(TFunctionAuth.class, qtFunctionAuth.id,
                            qtFunctionAuth.publishId, qtFunctionAuth.createdBy,
                            qtFunctionAuth.createdTime, qtFunctionAuth.updatedTime,
                            qtFunctionAuth.updatedBy, qtFunctionAuth.interfaceId
                    ))
                    .from(qtFunctionAuth)
                    .where(qtFunctionAuth.interfaceId.eq(interfaceId))
                    .limit(pageSize).offset((pageNo - 1) * pageSize)
                    .orderBy(qtFunctionAuth.createdTime.desc()).fetchResults();
            // 分页
            TableData<TFunctionAuth> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取功能权限明细列表成功!", tableData);
        } catch (BeansException e) {
            logger.error("获取功能权限明细列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取功能权限明细列表失败!");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/修改功能权限信息", notes = "新增/修改功能权限信息")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TFunctionAuth dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
        }
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String id = dto.getId();
        //校验服务与发布是否之前配置过
        if (!checkExist(dto)) {
            throw new RuntimeException("该服务发布已经授权给该服务,请勿重复授权!");
        }
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
        //删除缓存
        cacheDelete(dto.getInterfaceId());
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", id);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存功能权限信息成功!", JSON.toJSONString(data));
    }

    private boolean checkExist(TFunctionAuth dto) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(dto.getId())) {
            list.add(qtFunctionAuth.id.notEqualsIgnoreCase(dto.getId()));
        }
        list.add(qtFunctionAuth.interfaceId.eq(dto.getInterfaceId()));
        list.add(qtFunctionAuth.publishId.eq(dto.getPublishId()));

        List<TFunctionAuth> srList = sqlQueryFactory
                .select(Projections
                        .bean(TFunctionAuth.class, qtFunctionAuth.id))
                .from(qtFunctionAuth)
                .where(list.toArray(new Predicate[list.size()]))
                .fetch();
        if (!CollectionUtils.isEmpty(srList)) {
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "功能权限删除", notes = "功能权限删除")
    @GetMapping("/delById/{id}")
    public ResultDto<String> delById(
            @ApiParam(value = "功能权限id") @PathVariable(value = "id", required = true) String id) {
        TFunctionAuth one = this.getOne(id);
        //删除缓存
        cacheDelete(one.getInterfaceId());
        // 删除接口
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("功能权限删除成功!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "功能权限删除成功!");
    }

    public List<TFunctionAuth> getByPublishId(String publishId) {
        // 删除接口
        return sqlQueryFactory
                .select(Projections
                        .bean(TFunctionAuth.class, qtFunctionAuth.id))
                .from(qtFunctionAuth)
                .where(qtFunctionAuth.publishId.eq(publishId))
                .fetch();
    }

    private void cacheDelete(String interfaceId) {
        //根据服务id查询功能
        List<TFunctionAuth> auths = sqlQueryFactory
                .select(Projections
                        .bean(TFunctionAuth.class, qtFunctionAuth.id, qtFunctionAuth.publishId))
                .from(qtFunctionAuth)
                .where(qtFunctionAuth.interfaceId.eq(interfaceId))
                .fetch();

        //删除所有服务发布对应系统的key
        if (CollectionUtils.isEmpty(auths)) {
            return;
        }

        List<String> publishList = auths.stream().map(TFunctionAuth::getPublishId).collect(Collectors.toList());

        //找到服务的系统
        List<TSysPublish> sysPublish = sysPublishService.getByIdList(publishList);

        if (CollectionUtils.isEmpty(sysPublish)) {
            return;
        }

        CacheDeleteDto keyDto = new CacheDeleteDto();
        keyDto.setInterfaceIds(Arrays.asList(interfaceId));
        keyDto.setSysIds(sysPublish.stream().map(TSysPublish::getSysId).collect(Collectors.toList()));
        //需要删除下面两种缓存key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }
}
