package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysPublish;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysPublish.qTSysPublish;

@Slf4j
@Api(tags = "服务发布管理")
@RestController
@RequestMapping("/{version}/pt/sysPublish")
public class SysPublishService extends BaseService<TSysPublish, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(SysPublishService.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysService sysService;

    @Autowired
    private BatchUidService batchUidService;

    public SysPublishService() {
        super(qTSysPublish, qTSysPublish.id);
    }

    @ApiOperation(value = "服务发布列表")
    @GetMapping("/getPublishList")
    public ResultDto<TableData<TSysPublish>> getInterfaceList(
            @ApiParam(value = "系统id") @RequestParam(value = "sysId", required = false) String sysId,
            @ApiParam(value = "发布id") @RequestParam(value = "publishId", required = false) String publishId,
            @ApiParam(value = "发布名称") @RequestParam(value = "publishName", required = false) String publishName,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            // 查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            if (StringUtils.isNotEmpty(sysId)) {
                list.add(qTSysPublish.sysId.eq(sysId));
            }
            if (StringUtils.isNotEmpty(publishId)) {
                list.add(qTSysPublish.id.eq(publishId));
            }
            if (StringUtils.isNotEmpty(publishName)) {
                list.add(qTSysPublish.publishName.like(PlatformUtil.createFuzzyText(publishName)));
            }

            QueryResults<TSysPublish> queryResults = sqlQueryFactory
                    .select(Projections
                            .bean(TSysPublish.class, qTSysPublish.id,qTSysPublish.publishName,
                                    qTSysPublish.sysId,qTSys.sysName,qTSysPublish.connectionType,
                                    qTSysPublish.addressUrl, qTSysPublish.limitIps,
                                    qTSysPublish.createdBy, qTSysPublish.createdTime,
                                    qTSysPublish.updatedBy, qTSysPublish.updatedTime,qTSysPublish.isValid,qTSysPublish.isAuthen))
                    .from(qTSysPublish).leftJoin(qTSys)
                    .on(qTSysPublish.sysId.eq(qTSys.id))
                    .where(list.toArray(new Predicate[list.size()]))
                    .offset((pageNo - 1) * pageSize).orderBy(qTSysPublish.createdTime.desc()).fetchResults();
            // 分页
            TableData<TSysPublish> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务发布列表成功!", tableData);
        } catch (BeansException e) {
            logger.error("获取服务发布列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务发布列表失败!");
        }
    }

    @ApiOperation(value = "获取服务发布信息", notes = "获取服务发布信息")
    @GetMapping("/getPublish/{id}")
    public ResultDto<TSysPublish> getSysPublish(
            @ApiParam(value = "发布id") @PathVariable(value = "id", required = true) String id) {
        try {
            TSysPublish publish = this.getOne(id);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务发布信息成功!", publish);
        } catch (BeansException e) {
            logger.error("获取服务发布信息失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务发布信息失败!");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/修改服务发布信息", notes = "新增/修改服务发布信息")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TSysPublish dto) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
        }
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String registryId = dto.getId();
        //校验 校验“接入系统”是否发布过
        if(!checkPublishIsExist(registryId,dto.getSysId())){
            //查询系统名称和类型
            TSys sys = sysService.getOne(dto.getSysName());
            if(sys!=null){
                dto.setSysName(sys.getSysName());
            }
            throw new RuntimeException(dto.getSysName()+"系统已发布过服务");
        }

        // 新增系统配置信息
        if (StringUtils.isBlank(registryId)) {
            registryId = batchUidService.getUid(qTSysPublish.getTableName()) + "";
            dto.setId(registryId);
            dto.setCreatedTime(new Date());
            dto.setCreatedBy(loginUserName);
            this.post(dto);
        } else {
            dto.setUpdatedTime(new Date());
            dto.setUpdatedBy(loginUserName);
            long l = this.put(registryId, dto);
            if (l < 1) {
                throw new RuntimeException("服务发布编辑失败!");
            }
        }
        Map<String , String> data = new HashMap<String , String>();
        data.put("id", registryId);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存服务发布信息成功!", JSON.toJSONString(data));
    }

    /**
     * 校验新增或者修改是否重复
     * @param id
     * @param sysId
     * @return
     */
    private Boolean checkPublishIsExist(String id,String sysId){
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(id)) {
            list.add(qTSysPublish.id.notEqualsIgnoreCase(id));
        }
        list.add(qTSysPublish.sysId.eq(sysId));

        List<TSysRegistry> srList = sqlQueryFactory
                .select(Projections
                        .bean(TSysRegistry.class, qTSysPublish.id))
                .from(qTSysPublish)
                .where(list.toArray(new Predicate[list.size()]))
                .fetch();
        if(!CollectionUtils.isEmpty(srList)){
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "服务发布删除", notes = "服务发布删除")
    @PostMapping("/delById/{id}")
    public ResultDto<String> delById(
            @ApiParam(value = "服务id") @PathVariable(value = "id", required = true) String id) {
        // 删除接口
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("删除成功!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "删除成功!");
    }

    @ApiOperation(value = "获取服务发布下拉列表", notes = "获取服务发布下拉列表")
    @GetMapping("/getPublishSelect")
    public ResultDto<List<TSysPublish>> getRegistryList(
            @ApiParam(value = "服务发布名称") @PathVariable(value = "publishName", required = false) String publishName) {
        try {
            ArrayList<Predicate> pre = new ArrayList<>();
            if (StringUtils.isNotEmpty(publishName)) {
                pre.add(qTSysPublish.publishName.like(PlatformUtil.createFuzzyText(publishName)));
            }
            List<TSysPublish> list = sqlQueryFactory
                    .select(Projections.bean(TSysPublish.class, qTSysPublish.id, qTSysPublish.sysId, qTSysPublish.publishName))
                    .from(qTSysPublish)
                    .where(pre.toArray(new Predicate[pre.size()]))
                    .orderBy(qTSysPublish.createdTime.desc())
                    .fetch();
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务发布下拉列表成功!", list);
        } catch (BeansException e) {
            logger.error("获取服务发布下拉列表失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务发布下拉列表失败!");
        }
    }

    public TSysPublish getOneBySysId(String sysId){
        return sqlQueryFactory
                .select(Projections.bean(TSysPublish.class, qTSysPublish.id, qTSysPublish.sysId, qTSysPublish.publishName))
                .from(qTSysPublish)
                .where(qTSysPublish.sysId.eq(sysId))
                .fetchFirst();
    }

}
