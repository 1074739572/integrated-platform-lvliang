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

import static com.iflytek.integrated.platform.entity.QTFunctionAuth.qtFunctionAuth;
import static com.iflytek.integrated.platform.entity.QTSysPublish.qTSysPublish;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;

@Slf4j
@Api(tags = "??????????????????")
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

    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    @GetMapping("/getFunctionAuthList")
    public ResultDto<TableData<TFunctionAuth>> getFunctionAuth(
            @ApiParam(value = "??????id") @RequestParam(value = "interfaceId", required = true) String interfaceId,
            @ApiParam(value = "??????", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "????????????", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
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
            // ??????
            TableData<TFunctionAuth> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????????????????!", tableData);
        } catch (BeansException e) {
            logger.error("????????????????????????????????????! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????????????????!");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "??????/????????????????????????", notes = "??????/????????????????????????")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TFunctionAuth dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
        }
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
        }
        String id = dto.getId();
        //??????????????????????????????????????????
        if (!checkExist(dto)) {
            throw new RuntimeException("???????????????????????????????????????,??????????????????!");
        }
        if (StringUtils.isBlank(id)) {
            // ????????????????????????
            id = batchUidService.getUid(qTSysRegistry.getTableName()) + "";
            dto.setId(id);
            dto.setCreatedTime(new Date());
            dto.setCreatedBy(loginUserName);
            this.post(dto);

        } else {
            //????????????
            cacheDelete(id);
            //??????
            dto.setUpdatedTime(new Date());
            dto.setUpdatedBy(loginUserName);
            long l = this.put(id, dto);
            if (l < 1) {
                throw new RuntimeException("????????????????????????!");
            }
        }
        //????????????
        cacheDelete(id);
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", id);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", JSON.toJSONString(data));
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
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @GetMapping("/delById/{id}")
    public ResultDto<String> delById(
            @ApiParam(value = "????????????id") @PathVariable(value = "id", required = true) String id) {
        //????????????
        cacheDelete(id);
        // ????????????
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("????????????????????????!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!");
    }

    public List<TFunctionAuth> getByPublishId(String publishId) {
        // ????????????
        return sqlQueryFactory
                .select(Projections
                        .bean(TFunctionAuth.class, qtFunctionAuth.id))
                .from(qtFunctionAuth)
                .where(qtFunctionAuth.publishId.eq(publishId))
                .fetch();
    }

    private void cacheDelete(String id) {
        //??????????????????id????????????????????????????????????
        TFunctionAuth functionAuth = this.getOne(id);
        if (functionAuth == null) {
            return;
        }

        //?????????????????????
        TSysPublish sysPublish = sysPublishService.getOne(functionAuth.getPublishId());

        CacheDeleteDto keyDto = new CacheDeleteDto();
        keyDto.setInterfaceIds(Arrays.asList(functionAuth.getInterfaceId()));
        keyDto.setSysIds(Arrays.asList(sysPublish.getSysId()));
        //??????????????????????????????key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }
}
