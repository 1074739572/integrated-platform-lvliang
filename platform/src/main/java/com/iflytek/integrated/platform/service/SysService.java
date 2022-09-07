package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.CacheDeleteService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.*;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLUpdateClause;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;
import static com.iflytek.integrated.platform.entity.QTVendor.qtVendor;
import static com.querydsl.sql.SQLExpressions.groupConcat;

/**
 * 系统管理
 *
 * @author czzhan
 */
@Slf4j
@Api(tags = "系统管理")
@RestController
@RequestMapping("/{version}/pt/sysManage")
public class SysService extends BaseService<TSys, String, StringPath> {

    public SysService() {
        super(qTSys, qTSys.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(SysService.class);

    @Autowired
    private SysDriveLinkService sysDriveLinkService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private CacheDeleteService cacheDeleteService;
    @Autowired
    private SysPublishService sysPublishService;
    @Autowired
    private SysRegistryService sysRegistryService;

    @ApiOperation(value = "系统列表")
    @GetMapping("/getSysList")
    public ResultDto<TableData<SysDto>> getSysList(
            @ApiParam(value = "系统编码") @RequestParam(value = "sysCode", required = false) String sysCode,
            @ApiParam(value = "系统名称") @RequestParam(value = "sysName", required = false) String sysName,
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        try {
            List<Predicate> list = new ArrayList<>();
            SQLQuery<SysDto> queryer = sqlQueryFactory
                    .select(Projections.bean(SysDto.class, qTSys.id, qTSys.sysName, qTSys.sysCode, qTSys.isValid,
                            qTSys.createdBy, qTSys.createdTime, qTSys.updatedBy, qTSys.updatedTime, qTSys.sysDesc,
                            qTSys.vendorId, qtVendor.vendorName.max().as("vendorName"),
                            groupConcat(qTDrive.driveName, "|").as("driverNames")))
                    .from(qTSys)
                    .leftJoin((qTSysDriveLink)).on(qTSys.id.eq(qTSysDriveLink.sysId))
                    .leftJoin(qTDrive).on(qTSysDriveLink.driveId.eq(qTDrive.id))
                    .leftJoin(qtVendor).on(qTSys.vendorId.eq(qtVendor.id));
            if (StringUtils.isNotBlank(sysCode)) {
                list.add(qTSys.sysCode.eq(sysCode));
            }
            if (StringUtils.isNotBlank(sysName)) {
                list.add(qTSys.sysName.like("%" + sysName + "%"));
            }
            QueryResults<SysDto> queryResults = queryer.where(list.toArray(new Predicate[list.size()])).groupBy(qTSys.id)
                    .limit(pageSize).offset((pageNo - 1) * pageSize).orderBy(qTSys.createdTime.desc()).fetchResults();
            // 分页
            TableData<SysDto> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "系统管理列表获取成功", tableData);
        } catch (Exception e) {
            logger.error("获取系统管理列表失败! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取系统管理列表失败");
        }
    }

    @ApiOperation(value = "详情")
    @GetMapping("/getSysInfo")
    public ResultDto<TSys> getSysInfo(
            @ApiParam(value = "系统id") @RequestParam(value = "id", required = true) String id) {
        try {
            TSys tSys = sqlQueryFactory
                    .select(qTSys)
                    .from(qTSys)
                    .where(qTSys.id.eq(id)).fetchFirst();
            // 分页
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "系统详情获取成功", tSys);
        } catch (Exception e) {
            logger.error("获取系统管理列表失败! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统详情获取失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "系统管理删除")
    @PostMapping("/delSysById/{id}")
    public ResultDto<String> delSysById(
            @ApiParam(value = "系统id") @PathVariable(value = "id", required = true) String id) {

        //服务注册、服务发布中是否有关联
        TSysPublish publish = sysPublishService.getOneBySysId(id);
        if (publish != null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统已关联服务发布方,无法删除!", "该系统已关联服务发布方,无法删除!");
        }
        TSysRegistry registry = sysRegistryService.getOneBySysId(id);
        if (registry != null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统已关联服务注册方,无法删除!", "该系统已关联服务注册方,无法删除!");
        }

        //删除缓存
        cacheDelete(id);

        // 删除系统
        long count = this.delete(id);
        if (count <= 0) {
            throw new RuntimeException("系统删除失败!");
        }

        // 根据系统id删除系统与驱动关联信息
        sysDriveLinkService.deleteSysDriveLinkBySysId(id);

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "系统删除成功!", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "系统管理新增/编辑")
    @PostMapping("/saveAndUpdateSys")
    public ResultDto<String> saveAndUpdateSys(@RequestBody SysDto dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        if (dto.getVendorId() == null || dto.getVendorId() == "") {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "厂商不能为空!", "厂商不能为空!");
        }
        // 新增编辑标识 1新增 2编辑
        String addOrUpdate = dto.getAddOrUpdate();
        if (Constant.Operation.ADD.equals(addOrUpdate)) {
            return saveSys(dto, loginUserName);
        }

        List<String> list = new ArrayList<>();
        if (Constant.Operation.UPDATE.equals(addOrUpdate)) {
            return updateSys(dto, loginUserName);
        }

        return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "addOrUpdate参数有误!", JSONObject.toJSONString(list));
    }

    /**
     * 新增系统
     */
    private ResultDto saveSys(SysDto dto, String loginUserName) {
        String sysName = dto.getSysName();
        if (StringUtils.isBlank(sysName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统名称未填!", dto);
        }
        // 判断系统名称是否存在
        TSys tp = getObjBySysName(sysName.trim());
        if (tp != null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统名称已存在，不能重复!", "该系统名称已存在!");
        }

        // 新增系统
        String sysId = batchUidService.getUid(qTSys.getTableName()) + "";
        tp = new TSys();
        tp.setId(sysId);
        tp.setSysCode(generateCode(qTSys.sysCode, qTSys, sysName));
        tp.setSysName(sysName);
        tp.setIsValid(Constant.IsValid.ON);
        tp.setCreatedTime(new Date());
        tp.setCreatedBy(loginUserName);
        tp.setVendorId(dto.getVendorId());
        tp.setSysDesc(dto.getSysDesc());
        this.post(tp);
        String driveIds = dto.getDriveIds();
        if (StringUtils.isNotBlank(driveIds)) {
            String[] driveIdArr = driveIds.split(",");
            for (int i = 0; i < driveIdArr.length; i++) {
                TSysDriveLink tvdl = new TSysDriveLink();
                tvdl.setId(batchUidService.getUid(qTSysDriveLink.getTableName()) + "");
                tvdl.setSysId(sysId);
                tvdl.setDriveId(driveIdArr[i]);
                tvdl.setDriveOrder(i + 1);
                tvdl.setCreatedTime(new Date());
                tvdl.setCreatedBy(loginUserName);
                sysDriveLinkService.post(tvdl);
            }
        }
        cacheDelete(tp.getId());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增系统成功", null);
    }

    /**
     * 编辑系统
     */
    private ResultDto updateSys(SysDto dto, String loginUserName) {
        String sysName = dto.getSysName();
        if (StringUtils.isBlank(sysName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统名称未填!", dto);
        }
        // 判断系统名称是否存在
        TSys tp = getObjBySysName(sysName.trim());
        if (tp != null && !tp.getId().equals(dto.getId())) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统名称已存在，不能重复!", "该系统名称已存在!");
        }
        String sysId = dto.getId();
        String isValid = dto.getIsValid();
        String vendorId = dto.getVendorId();
        String sysDesc = dto.getSysDesc();
        String driveIds = dto.getDriveIds();

		// 更新系统信息
		SQLUpdateClause updater = sqlQueryFactory.update(qTSys);
		if (StringUtils.isNotBlank(sysName)) {
			updater.set(qTSys.sysName, sysName);
//			updater.set(qTSys.sysCode, generateCode(qTSys.sysCode, qTSys, sysName));
		}
		if (StringUtils.isNotBlank(isValid)) {
			updater.set(qTSys.isValid, isValid);
		}
		if (StringUtils.isNotBlank(vendorId)) {
			updater.set(qTSys.vendorId, vendorId);
		}
		updater.set(qTSys.sysDesc, sysDesc);
		long l = updater.set(qTSys.updatedTime, new Date()).set(qTSys.updatedBy, loginUserName)
				.where(qTSys.id.eq(sysId)).execute();
		if (l <= 0) {
			throw new RuntimeException("系统信息更新失败!");
		}
		// 删除关联
		sysDriveLinkService.deleteSysDriveLinkBySysId(sysId);
		// 添加新关联
		if (StringUtils.isNotBlank(driveIds)) {
			String[] driveIdArr = driveIds.split(",");
			for (int i = 0; i < driveIdArr.length; i++) {
				TSysDriveLink tvdl = new TSysDriveLink();
				tvdl.setId(batchUidService.getUid(qTSysDriveLink.getTableName()) + "");
				tvdl.setSysId(sysId);
				tvdl.setDriveId(driveIdArr[i]);
				tvdl.setDriveOrder(i + 1);
				tvdl.setCreatedTime(new Date());
				tvdl.setCreatedBy(loginUserName);
				sysDriveLinkService.post(tvdl);
			}
		}
        cacheDelete(dto.getId());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改系统成功", null);
	}

    @ApiOperation(value = "选择系统下拉列表")
    @GetMapping("/getDisSys")
    public ResultDto<List<TSys>> getDisSys() {
        List<TSys> syss = sqlQueryFactory.select(Projections.bean(TSys.class, qTSys.id, qTSys.sysName, qTSys.sysCode, qTSys.isValid))
                .from(qTSys).where(qTSys.isValid.eq("1")).orderBy(qTSys.updatedTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "选择系统下拉列表获取成功!", syss);
    }

    /**
     * 根据系统名称获取系统信息
     *
     * @param sysName
     * @return
     */
    public TSys getObjBySysName(String sysName) {
        return sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.sysName.eq(sysName)).fetchFirst();
    }

    /**
     * 根据系统id列表查询所有系统
     *
     * @param sysIds
     * @return
     */
    public List<TSys> getBySysIds(List<String> sysIds) {
        return sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.id.in(sysIds)).fetch();
    }

    public List<TSys> getAll() {
        return sqlQueryFactory.select(qTSys).from(qTSys).fetch();
    }

    public TSys getByVendorId(String vendorId) {
        return sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.vendorId.eq(vendorId)).fetchFirst();
    }

    private void cacheDelete(String sysId) {
        TSys sys = this.getOne(sysId);
        CacheDeleteDto sysKeyDto = new CacheDeleteDto();
        sysKeyDto.setSysCodes(Arrays.asList(sys.getSysCode()));
        //需要获取四种类型的key  但是系统没有绑定funcode需要查询所有的
        sysKeyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.DRIVERS_TYPE,
                Constant.CACHE_KEY_PREFIX.SCHEMA_TYPE,
                Constant.CACHE_KEY_PREFIX.AUTHENTICATION_TYPE,
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));
        cacheDeleteService.cacheKeyDelete(sysKeyDto);
    }
}
