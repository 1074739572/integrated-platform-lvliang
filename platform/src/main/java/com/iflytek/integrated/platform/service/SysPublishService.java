package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.CacheDeleteService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.CacheDeleteDto;
import com.iflytek.integrated.platform.entity.TFunctionAuth;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysPublish.qTSysPublish;

@Slf4j
@Api(tags = "服务发布管理")
@RestController
@RequestMapping("/{version}/pt/sysPublish")
public class SysPublishService extends BaseService<TSysPublish, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(SysPublishService.class);

    @Autowired
    private BatchUidService batchUidService;

    @Autowired
    private FunctionAuthService functionAuthService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CacheDeleteService cacheDeleteService;

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
                            .bean(TSysPublish.class, qTSysPublish.id, qTSysPublish.publishName,
                                    qTSysPublish.sysId, qTSys.sysName,qTSys.sysCode, qTSysPublish.connectionType,
                                    qTSysPublish.addressUrl, qTSysPublish.limitIps,
                                    qTSysPublish.createdBy, qTSysPublish.createdTime,
                                    qTSysPublish.updatedBy, qTSysPublish.updatedTime, qTSysPublish.isValid, qTSysPublish.isAuthen))
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
            @ApiParam(value = "发布id") @PathVariable(value = "id", required = true) String id,@RequestParam("loginUserName") String loginUserName) {
        try {
            TSysPublish publish = this.getOne(id);
            //获取签名密钥
            String key=id+"::"+loginUserName;
            Object sign = redisUtil.get(key);
            if(sign==null){
                publish.setIsShow("0");
                //密码中间用*替换
                String signKey = publish.getSignKey();
                if(StringUtils.isNotEmpty(signKey)){
                    StringBuffer buffer = new StringBuffer(signKey);
                    buffer.replace(1,buffer.length()-1,"******");
                    publish.setSignKey(buffer.toString());
                }
            }else{
                //明文显示
                publish.setIsShow("1");
            }
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务发布信息成功!", publish);
        } catch (BeansException e) {
            logger.error("获取服务发布信息失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务发布信息失败!");
        }
    }

    @ApiOperation(value = "获取签名密钥", notes = "获取签名密钥")
    @GetMapping("/getSignKey/{id}")
    public ResultDto<String> getSignKey(
            @ApiParam(value = "发布id") @PathVariable(value = "id", required = true) String id, @RequestParam("loginUserName") String loginUserName) {
        try {
            TSysPublish publish = this.getOne(id);
            if(publish==null){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "未查询到该服务发布!", "未查询到该服务发布!");
            }
            //将明文保存进redis  时效1分钟
            String key=id+"::"+loginUserName;
            redisUtil.set(key,publish.getSignKey(),60000L);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取签名密钥成功!", publish.getSignKey());
        } catch (BeansException e) {
            logger.error("获取签名密钥失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取签名密钥失败!");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/修改服务发布信息", notes = "新增/修改服务发布信息")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TSysPublish dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String pubId = dto.getId();
        //校验 校验“接入系统”是否发布过
        if (!checkPublishIsExist(pubId, dto.getSysId())) {
            throw new RuntimeException("该系统已经发布过服务");
        }
        // 新增系统配置信息
        if (StringUtils.isBlank(pubId)) {
            pubId = batchUidService.getUid(qTSysPublish.getTableName()) + "";
            dto.setId(pubId);
            dto.setCreatedTime(new Date());
            dto.setCreatedBy(loginUserName);
            this.post(dto);
        } else {
            //重新查询下签名密码防止传过来密文覆盖数据库明文
            dto.setSignKey(getOne(pubId).getSignKey());
            dto.setUpdatedTime(new Date());
            dto.setUpdatedBy(loginUserName);
            long l = this.put(pubId, dto);
            //删除缓存
            cacheDelete(pubId);
            if (l < 1) {
                throw new RuntimeException("服务发布编辑失败!");
            }
        }
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", pubId);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存服务发布信息成功!", JSON.toJSONString(data));
    }

    /**
     * 校验新增或者修改是否重复
     *
     * @param id
     * @param sysId
     * @return
     */
    private Boolean checkPublishIsExist(String id, String sysId) {
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
        if (!CollectionUtils.isEmpty(srList)) {
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "服务发布删除", notes = "服务发布删除")
    @PostMapping("/delById/{id}")
    public ResultDto<String> delById(
            @ApiParam(value = "服务id") @PathVariable(value = "id", required = true) String id) {
        //先判断有没有关联权限
        List<TFunctionAuth> list = functionAuthService.getByPublishId(id);
        if (!CollectionUtils.isEmpty(list)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该服务发布已有功能权限关联,无法删除!", "该服务发布已有功能权限关联,无法删除!");
        }
        //删除缓存
        cacheDelete(id);

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
            @ApiParam(value = "服务发布名称") @RequestParam(value = "publishName", required = false) String publishName,
            @ApiParam(value = "服务发布状态") @RequestParam(value = "isValid", required = false, defaultValue = "1") String isValid
    ) {
        try {
            ArrayList<Predicate> pre = new ArrayList<>();
            if (StringUtils.isNotEmpty(publishName)) {
                pre.add(qTSysPublish.publishName.like(PlatformUtil.createFuzzyText(publishName)));
            }
            if (StringUtils.isNotEmpty(isValid)) {
                pre.add(qTSysPublish.isValid.eq(isValid));
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

    public TSysPublish getOneBySysId(String sysId) {
        return sqlQueryFactory
                .select(Projections.bean(TSysPublish.class, qTSysPublish.id, qTSysPublish.sysId, qTSysPublish.publishName))
                .from(qTSysPublish)
                .where(qTSysPublish.sysId.eq(sysId))
                .fetchFirst();
    }

    private void cacheDelete(String pubId) {
        //根据驱动id查询系统
        TSysPublish publish = this.getOne(pubId);

        CacheDeleteDto sysKeyDto=new CacheDeleteDto();
        sysKeyDto.setSysIds(Arrays.asList(publish.getSysId()));

        //需要生成两种类型的key 因为驱动无法获取到funcode所以获取所有的funcode
        sysKeyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.SCHEMA_TYPE,
                Constant.CACHE_KEY_PREFIX.AUTHENTICATION_TYPE,
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE));

        //获得到系统编码的缓存键集合
        cacheDeleteService.cacheKeyDelete(sysKeyDto);
    }

}
