package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.SysConfigDto;
import com.iflytek.integrated.platform.dto.SysDto;
import com.iflytek.integrated.platform.dto.SysHospitalDto;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;
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
	private BusinessInterfaceService businessInterfaceService;
	@Autowired
	private SysConfigService sysConfigService;
	@Autowired
	private BatchUidService batchUidService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private InterfaceService interfaceService;

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
							qTSys.createdBy, qTSys.createdTime, qTSys.updatedBy, qTSys.updatedTime,
							groupConcat(qTDrive.driveName, "|").as("driverNames")))
					.from(qTSys).leftJoin((qTSysDriveLink)).on(qTSys.id.eq(qTSysDriveLink.sysId)).leftJoin(qTDrive)
					.on(qTSysDriveLink.driveId.eq(qTDrive.id));
			if (StringUtils.isNotBlank(sysCode)) {
				list.add(qTSys.sysCode.eq(sysCode));
			}
			if (StringUtils.isNotBlank(sysName)) {
				list.add(qTSys.sysCode.like("%" + sysCode + "%"));
			}
			QueryResults<SysDto> queryResults = queryer.where(list.toArray(new Predicate[list.size()])).groupBy(qTSys.id)
					.limit(pageSize).offset((pageNo - 1) * pageSize).orderBy(qTSys.createdTime.desc()).fetchResults();
			// 分页
			TableData<SysDto> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "系统管理列表获取成功", tableData);
		} catch (Exception e) {
			logger.error("获取产品管理列表失败! MSG:{}", ExceptionUtil.dealException(e));
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取系统管理列表失败");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "系统管理删除")
	@PostMapping("/delSysById")
	public ResultDto<String> delSysById(
			@ApiParam(value = "系统id") @RequestParam(value = "id", required = true) String id) {
		// 删除系统前先查询该关联数据是否有项目相关联
		List<TSysConfig> tpplList = sysConfigService.getObjBySysId(id);
		if (CollectionUtils.isNotEmpty(tpplList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统已在项目中配置使用,无法删除!", "该系统已在项目中配置使用,无法删除!");
		}

		// 删除系统前先查询是否与接口关联
		List<TInterface> interfaceList = interfaceService.getObjBySysId(id);
		if (CollectionUtils.isNotEmpty(interfaceList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统已有关联接口,无法删除!", "该系统已有关联接口,无法删除!");
		}

		// 删除系统
		long count = this.delete(id);
		if (count <= 0) {
			throw new RuntimeException("系统删除失败!");
		}

		// 根据系统id删除系统与驱动关联信息
		long lon = sysDriveLinkService.deleteSysDriveLinkBySysId(id);

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "系统删除成功!", "");
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "系统管理新增/编辑")
	@PostMapping("/saveAndUpdateSys")
	public ResultDto<String> saveAndUpdateSys(@RequestBody SysDto dto) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
		}
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}
		// 新增编辑标识 1新增 2编辑
		String addOrUpdate = dto.getAddOrUpdate();
		if (Constant.Operation.ADD.equals(addOrUpdate)) {
			return saveSys(dto, loginUserName);
		}
		if (Constant.Operation.UPDATE.equals(addOrUpdate)) {
			return updateSys(dto, loginUserName);
		}
		return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "addOrUpdate参数有误!", null);
	}

	/** 新增系统 */
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

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增系统成功", null);
	}

	/** 编辑系统 */
	private ResultDto updateSys(SysDto dto, String loginUserName) {
		String sysId = dto.getId();
		String sysName = dto.getSysName();
		String isValid = dto.getIsValid();
		String driveIds = dto.getDriveIds();

		// 更新系统信息
		SQLUpdateClause updater = sqlQueryFactory.update(qTSys);
		if (StringUtils.isNotBlank(sysName)) {
			updater.set(qTSys.sysName, sysName);
		}
		if (StringUtils.isNotBlank(isValid)) {
			updater.set(qTSys.isValid, isValid);
		}
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
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改系统成功", "");
	}

	@ApiOperation(value = "选择系统下拉列表")
	@GetMapping("/getDisSys")
	public ResultDto<List<TSys>> getDisSys() {
		List<TSys> syss = sqlQueryFactory.select(Projections.bean(TSys.class, qTSys.id, qTSys.sysName, qTSys.sysCode))
				.from(qTSys).orderBy(qTSys.createdTime.desc()).fetch();
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

	@ApiOperation(value = "获取平台系统配置信息", notes = "获取平台系统配置信息")
	@GetMapping("/getSysConfigs")
	public ResultDto<SysConfigDto> getSysConfigList(
			@ApiParam(value = "分类id") @RequestParam(value = "platformId", required = true) String platformId) {
		try {
			List<TSysConfig> VCList = sqlQueryFactory
					.select(Projections
							.bean(TSysConfig.class, qTSysConfig.id, qTSysConfig.projectId, qTSysConfig.platformId,
									qTSysConfig.sysId, qTSysConfig.sysConfigType, qTSysConfig.connectionType,
									qTSysConfig.versionId, qTSysConfig.addressUrl, qTSysConfig.endpointUrl,
									qTSysConfig.namespaceUrl, qTSysConfig.databaseName, qTSysConfig.databaseUrl,
									qTSysConfig.databaseDriver, qTSysConfig.driverUrl, qTSysConfig.jsonParams,
									qTSysConfig.userName, qTSysConfig.userPassword, qTSysConfig.createdBy,
									qTSysConfig.createdTime, qTSysConfig.updatedBy, qTSysConfig.updatedTime,
									groupConcat(qTSysHospitalConfig.hospitalId.append(":")
											.append(qTSysHospitalConfig.hospitalCode)).as("hospitalConfigStr")))
					.from(qTSysConfig).leftJoin(qTSysHospitalConfig)
					.on(qTSysConfig.id.eq(qTSysHospitalConfig.sysConfigId)).groupBy(qTSysConfig.id)
					.where(qTSysConfig.platformId.eq(platformId)).fetch();
			SysConfigDto configDto = new SysConfigDto();
			configDto.setRequestedSysConfigs(new ArrayList<TSysConfig>());
			for (TSysConfig obj : VCList) {
				// 系统信息
				TSys tv = this.getOne(obj.getSysId());
				if (tv != null) {
					obj.setSysCode(tv.getSysCode());
					obj.setSysName(tv.getSysName());
				}
				String hospitalConfigs = obj.getHospitalConfigStr();
				if (StringUtils.isNotBlank(hospitalConfigs)) {
					List<SysHospitalDto> hospitalConfigList = new ArrayList<>();
					for (String s : hospitalConfigs.split(",")) {
						SysHospitalDto sysHospitalDto = new SysHospitalDto();
						String[] split = s.split(":");
						sysHospitalDto.setHospitalId(split[0]);
						if(split.length == 2) {
							sysHospitalDto.setHospitalCode(split[1]);
						}
						hospitalConfigList.add(sysHospitalDto);
					}
//					List<SysHospitalDto> hospitalConfigList = JSON.parseArray(hospitalConfigs, SysHospitalDto.class);
					obj.setHospitalConfigs(hospitalConfigList);
				}
				if (obj.getSysConfigType() == 1) {
					configDto.setRequestSysConfig(obj);
				} else {
					configDto.getRequestedSysConfigs().add(obj);
				}
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取系统配置信息成功!", configDto);
		} catch (BeansException e) {
			logger.error("获取系统配置信息失败! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取系统配置信息失败!");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "删除平台下系统配置信息", notes = "删除平台下厂商配置信息")
	@PostMapping("/delSysConfig")
	public ResultDto<String> delSysConfig(
			@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
			@ApiParam(value = "系统id") @RequestParam(value = "sysId", required = true) String sysId) {
		TSysConfig tvc = sysConfigService.getConfigByPlatformAndSys(platformId, sysId);
		if (tvc != null) {
			List<TBusinessInterface> tbiList = businessInterfaceService.getListBySysConfigId(tvc.getId());
			if (CollectionUtils.isNotEmpty(tbiList)) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统已有接口配置数据相关联,无法删除!", "该厂商已有接口配置数据相关联,无法删除!");
			}
			// 删除系统配置
			long l = sysConfigService.delete(tvc.getId());
			if (l < 1) {
				throw new RuntimeException("系统配置删除失败!");
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "系统配置删除成功!", "系统配置删除成功!");
		}
		return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据平台id与系统id未查到该系统配置信息!", "根据平台id与系统id未查到该系统配置信息!");
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "删除系统配置下医院信息", notes = "删除系统配置下医院信息")
	@PostMapping("/delSysHospitalBySysConfig")
	public ResultDto<String> delSysHospitalBySysConfig(
			@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
			@ApiParam(value = "系统id") @RequestParam(value = "sysId", required = true) String sysId,
			@ApiParam(value = "医院id，不传时，删除所有关联的医院") @RequestParam(value = "hospitalId", required = false) String hospitalId) {
		TSysConfig tvc = sysConfigService.getConfigByPlatformAndSys(platformId, sysId);
		if (tvc != null) {
			// 删除厂商配置关联的医院
			sysConfigService.delSysConfigHospital(tvc, hospitalId);
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "系统下医院配置信息删除成功!", null);
		}
		return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据平台与系统id未查到该系统配置信息!", null);
	}

	@ApiOperation(value = "选择系统下拉(可根据当前项目操作选择)")
	@GetMapping("/getDisSysByOpt")
	public ResultDto<List<TSys>> getDisSysByOpt(
			@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = false) String projectId,
			@ApiParam(value = "操作 1获取当前项目下的厂商 2获取非当前项目下的厂商") @RequestParam(defaultValue = "1", value = "status", required = false) String status) {
		List<TSys> curProjSyss = sqlQueryFactory.select(qTSys).from(qTSys).leftJoin(qTSysConfig)
				.on(qTSysConfig.sysId.eq(qTSys.id)).leftJoin(qTPlatform).on(qTPlatform.id.eq(qTSysConfig.platformId))
				.where(qTPlatform.projectId.eq(projectId)).orderBy(qTSys.createdTime.desc()).fetch();
		if (StringUtils.isNotBlank(projectId) && Constant.Operation.CURRENT.equals(status)) {
			// 返回当前项目下的厂商
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", curProjSyss);
		}
		// 获取所有厂商
		List<TSys> allSyss = sqlQueryFactory.select(qTSys).from(qTSys).orderBy(qTSys.createdTime.desc()).fetch();
		if (StringUtils.isBlank(projectId)) {
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", allSyss);
		}
		// 去除当前项目下的厂商
		allSyss.removeAll(curProjSyss);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", allSyss);
	}

	@ApiOperation(value = "根据平台id获取系统信息")
	@GetMapping("/getDisSysByPlatform")
	public ResultDto<List<SysDto>> getDisSysByPlatform(
			@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = true) String platformId,
			@ApiParam(value = "sysConfigType") @RequestParam(value = "sysConfigType", required = true) String sysConfigType) {
		List<SysDto> vendors = sqlQueryFactory
				.select(Projections.bean(SysDto.class, qTSys.id, qTSys.sysName, qTSys.sysCode, qTSys.createdBy,
						qTSys.createdTime, qTSys.updatedBy, qTSys.updatedTime,
						qTSysConfig.connectionType.as("connectionType") , qTSysConfig.id.as("sysConfigId")))
				.from(qTSys).join(qTSysConfig).on(qTSysConfig.sysId.eq(qTSys.id)).where(qTSysConfig.platformId
						.eq(platformId).and(qTSysConfig.sysConfigType.eq(Integer.valueOf(sysConfigType))))
				.fetch();
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", vendors);
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "根据系统配置id删除系统配置信息")
	@PostMapping("/delSysConfigById")
	public ResultDto<String> delSysConfigById(
			@ApiParam(value = "系统配置id") @RequestParam(value = "id", required = true) String id,
			@ApiParam(value = "平台id") @RequestParam(value = "platformId", required = false) String platformId) {
		TSysConfig tvc = sysConfigService.getOne(id);
		if (tvc == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id查询不到该厂商配置信息!", id);
		}
		// 获取接口配置列表信息
		List<TBusinessInterface> queryResults = businessInterfaceService.getInterfaceConfigureList(platformId);
		if (CollectionUtils.isNotEmpty(queryResults)) {
			for (TBusinessInterface tbi : queryResults) {
				String requestSysConfigId = tbi.getRequestSysconfigId();
				String requestedSysConfigId = tbi.getRequestedSysconfigId();
				if (id.equals(requestSysConfigId) || id.equals(requestedSysConfigId)) {
					return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统配置已有接口转换配置关联,无法删除!", id);
				}
			}
		}
		long count = sysConfigService.delete(id);
		if (count < 1) {
			throw new RuntimeException("删除失败!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "删除成功!", "删除成功!");
	}

}
