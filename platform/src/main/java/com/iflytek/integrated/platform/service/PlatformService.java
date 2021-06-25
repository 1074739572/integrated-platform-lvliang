package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.*;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.iflytek.integrated.platform.entity.TSysHospitalConfig;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.dml.SQLInsertClause;
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
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;

/**
 * 平台管理
 * 
 * @author weihe9
 * @date 2020/12/12 16:58
 */
@Slf4j
@Api(tags = "平台管理")
@RestController
@RequestMapping("/{version}/pt/platformManage")
public class PlatformService extends BaseService<TPlatform, String, StringPath> {

	@Autowired
	private SysConfigService sysConfigService;
	@Autowired
	private BusinessInterfaceService businessInterfaceService;
	@Autowired
	private BatchUidService batchUidService;
	@Autowired
	private RedisService redisService;

	private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);

	public PlatformService() {
		super(qTPlatform, qTPlatform.id);
	}

	@ApiOperation(value = "根据项目id获取平台(分页)", notes = "根据项目id获取平台(分页)")
	@GetMapping("/getPlatformListById")
	public ResultDto<TableData<TPlatform>> getPlatformListById(
			@ApiParam(value = "项目id") @RequestParam(value = "projectId", required = true) String projectId,
			@ApiParam(value = "平台状态") @RequestParam(value = "platformStatus", required = false) String platformStatus,
			@ApiParam(value = "平台名称") @RequestParam(value = "platformName", required = false) String platformName,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		try {
			ArrayList<Predicate> list = new ArrayList<>();
			list.add(qTPlatform.projectId.eq(projectId));
			if (StringUtils.isNotBlank(platformStatus)) {
				list.add(qTPlatform.platformStatus.eq(platformStatus));
			}
			if (StringUtils.isNotBlank(platformName)) {
				list.add(qTPlatform.platformName.like(PlatformUtil.createFuzzyText(platformName)));
			}
			QueryResults<TPlatform> queryResults = sqlQueryFactory
					.select(Projections.bean(TPlatform.class, qTPlatform.id, qTPlatform.projectId,
							qTPlatform.platformCode, qTPlatform.platformName, qTPlatform.platformStatus,
							qTPlatform.platformType, qTPlatform.etlServerUrl, qTPlatform.etlUser, qTPlatform.etlPwd,
							qTPlatform.createdTime, qTPlatform.updatedTime, qTPlatform.createdBy, qTPlatform.updatedBy))
					.from(qTPlatform).where(list.toArray(new Predicate[list.size()])).limit(pageSize)
					.offset((pageNo - 1) * pageSize).orderBy(qTPlatform.createdTime.desc()).fetchResults();
			// 分页
			TableData<TPlatform> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "根据项目id获取平台成功!", tableData);
		} catch (Exception e) {
			logger.error("根据项目id获取平台失败! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据项目id获取平台失败!");
		}
	}

	@ApiOperation(value = "根据项目id获取平台(ETL平台下拉列表)", notes = "根据项目id获取平台(ETL平台下拉列表)")
	@GetMapping("/getEtlPlatformListById/{projectId}")
	public ResultDto<List<TPlatform>> getEtlPlatformListById(@ApiParam(value = "项目id") @PathVariable String projectId) {
		try {
			ArrayList<Predicate> list = new ArrayList<>();
			list.add(qTPlatform.projectId.eq(projectId));
			list.add(qTPlatform.platformType.eq("2"));
			List<TPlatform> queryResults = sqlQueryFactory
					.select(Projections.bean(TPlatform.class, qTPlatform.id, qTPlatform.projectId,
							qTPlatform.platformCode, qTPlatform.platformName, qTPlatform.platformStatus,
							qTPlatform.platformType, qTPlatform.etlServerUrl, qTPlatform.etlUser, qTPlatform.etlPwd,
							qTPlatform.createdTime, qTPlatform.updatedTime, qTPlatform.createdBy, qTPlatform.updatedBy))
					.from(qTPlatform).where(list.toArray(new Predicate[list.size()]))
					.orderBy(qTPlatform.createdTime.desc()).fetch();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "根据项目id获取平台成功!", queryResults);
		} catch (Exception e) {
			logger.error("根据项目id获取平台失败! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据项目id获取平台失败!");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "新增or修改分类", notes = "新增or修改分类")
	@PostMapping("/saveAndUpdatePlatform")
	public ResultDto<String> saveAndUpdatePlatform(@RequestBody PlatformDto dto) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
		}
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}
		// 平台名称校验
		String platformId = dto.getId();
		String platformName = dto.getPlatformName();
		boolean isExist = getPlatformNameIsExistByProjectId(platformId, dto.getProjectId(), platformName);
		if (isExist) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "平台名称为空或此项目下该名称已存在!", platformName);
		}
		SysConfigDto sysConfig = dto.getSysConfig();
		if ("1".equals(dto.getPlatformType())) {
			TSysConfig requestSysConfig = sysConfig.getRequestSysConfig();
			if (requestSysConfig.getSysConfigType() == 1) {
				// 判断请求方系统id是否存在多条
				if(!requestSysConfig.getSysId().equals(dto.getSysId())){
					boolean requestSysExsits = sysConfigService.requestSysExsits(requestSysConfig.getSysId());
					if (requestSysExsits) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方系统id不能重复！", "请求方系统id不能重复!");
					}
				}
			}

			List<TSysConfig> jsonArr = sysConfig.getRequestedSysConfigs();
			if (CollectionUtils.isNotEmpty(jsonArr)) {
				String vendorIdStr = "";
				for (TSysConfig vcd : jsonArr) {
					if (vendorIdStr.contains(vcd.getSysId())) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统名称不能重复!", "系统名称不能重复!");
					}
					vendorIdStr += vcd.getSysId();
				}
			}
		}
		if (StringUtils.isBlank(platformId)) {
			return savePlatform(dto, loginUserName);
		}
		return updatePlatform(dto, loginUserName);
	}

	/** 新增平台 */
	private ResultDto savePlatform(PlatformDto dto, String loginUserName) {
		TPlatform tp = new TPlatform();
		String platformId = batchUidService.getUid(qTPlatform.getTableName()) + "";
		tp.setId(platformId);
		tp.setPlatformCode(generateCode(qTPlatform.platformCode, qTPlatform, dto.getPlatformName()));
		tp.setProjectId(dto.getProjectId());
		tp.setPlatformName(dto.getPlatformName());
		tp.setPlatformType(dto.getPlatformType());
		tp.setEtlServerUrl(dto.getEtlServerUrl());
		tp.setEtlUser(dto.getEtlUser());
		tp.setEtlPwd(dto.getEtlPwd());
		tp.setPlatformStatus(Constant.Status.START);
		tp.setCreatedTime(new Date());
		tp.setCreatedBy(loginUserName);
		this.post(tp);
		// 关联系统
		SysConfigDto configDto = dto.getSysConfig();
		if (configDto == null) {
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", null);
		}
		List<TSysConfig> allConfig = new ArrayList<>();
		allConfig.add(configDto.getRequestSysConfig());
		allConfig.addAll(configDto.getRequestedSysConfigs());
		allConfig.remove(null);
		if (allConfig.size() > 0) {
			SQLInsertClause syshosConfigClause = sqlQueryFactory.insert(qTSysHospitalConfig);
			boolean storehosconfig = false;
			for (int i = 0; i < allConfig.size(); i++) {
				TSysConfig tvc = allConfig.get(i);
				if(StringUtils.isBlank(tvc.getPlatformId())){
					tvc.setPlatformId(platformId);
				}
				if(StringUtils.isBlank(tvc.getProjectId())) {
					tvc.setProjectId(dto.getProjectId());
				}
				if("2".equals(dto.getPlatformType())) {
					tvc.setSysConfigType(3);
					tvc.setSysId("0");
					tvc.setConnectionType("0");
				}
				List<SysHospitalDto> sysHosList = tvc.getHospitalConfigs();
				String sysConfigId = batchUidService.getUid(qTSysConfig.getTableName()) + "";
				tvc.setId(sysConfigId);
				tvc.setProjectId(dto.getProjectId());
				tvc.setPlatformId(platformId);
				if (tvc.getSysConfigType() != 1) {
					tvc.setInnerIdx(sysConfigId);
				}
				tvc.setCreatedTime(new Date());
				tvc.setCreatedBy(loginUserName);
				sysConfigService.post(tvc);
				if (sysHosList != null && sysHosList.size() > 0) {
					storehosconfig = true;
					sysHosList.forEach(hosDto -> {
						TSysHospitalConfig thosconfig = new TSysHospitalConfig();
						thosconfig.setId(batchUidService.getUid(qTSysHospitalConfig.getTableName()) + "");
						thosconfig.setHospitalCode(hosDto.getHospitalCode());
						thosconfig.setHospitalId(hosDto.getHospitalId());
						thosconfig.setSysConfigId(sysConfigId);
						syshosConfigClause.populate(thosconfig).addBatch();
					});
				}
			}
			if (storehosconfig) {
				syshosConfigClause.execute();
			}
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "新增平台成功!", "新增平台成功!");
	}

	/** 修改平台 */
	private ResultDto updatePlatform(PlatformDto dto, String loginUserName) {
		// 平台id
		String platformId = dto.getId();
        // redis缓存信息获取
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTPlatform.id.eq(platformId));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		long l = sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformName, dto.getPlatformName())
				.set(qTPlatform.platformType, dto.getPlatformType()).set(qTPlatform.etlServerUrl, dto.getEtlServerUrl())
				.set(qTPlatform.etlUser, dto.getEtlUser()).set(qTPlatform.etlPwd, dto.getEtlPwd())
				.set(qTPlatform.projectId, dto.getProjectId()).set(qTPlatform.updatedTime, new Date())
				.set(qTPlatform.updatedBy, loginUserName).where(qTPlatform.id.eq(platformId)).execute();
		if (l < 1) {
			throw new RuntimeException("修改平台失败!");
		}
		// 前台传递的新系统信息
		SysConfigDto configDto = dto.getSysConfig();
		if (configDto == null) {
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改平台成功!", null);
		}
		List<TSysConfig> allConfig = new ArrayList<>();
		allConfig.add(configDto.getRequestSysConfig());
		allConfig.addAll(configDto.getRequestedSysConfigs());
		allConfig.remove(null);
		if (allConfig.size() > 0) {
			for (int i = 0; i < allConfig.size(); i++) {
				TSysConfig tvc = allConfig.get(i);
				if(StringUtils.isBlank(tvc.getPlatformId())){
					tvc.setPlatformId(platformId);
				}
				if(StringUtils.isBlank(tvc.getProjectId())) {
					tvc.setProjectId(dto.getProjectId());
				}
				if("2".equals(dto.getPlatformType())) {
					tvc.setSysConfigType(3);
					tvc.setSysId("0");
					tvc.setConnectionType("0");
				}
				tvc.setProjectId(dto.getProjectId());
				tvc.setPlatformId(platformId);
				String sysConfigId = tvc.getId();
				// 新增系统信息
				if (StringUtils.isBlank(sysConfigId)) {
					sysConfigId = batchUidService.getUid(qTSysConfig.getTableName()) + "";
					tvc.setId(sysConfigId);
					if (tvc.getSysConfigType() != 1) {
						tvc.setInnerIdx(sysConfigId);
					}
					tvc.setCreatedTime(new Date());
					tvc.setCreatedBy(loginUserName);
					sysConfigService.post(tvc);
					// 医院配置
					List<SysHospitalDto> sysHosList = tvc.getHospitalConfigs();
					if (sysHosList != null && sysHosList.size() > 0) {
						SQLInsertClause syshosConfigClause = sqlQueryFactory.insert(qTSysHospitalConfig);
						for (SysHospitalDto hosDto : sysHosList) {
							TSysHospitalConfig thosconfig = new TSysHospitalConfig();
							thosconfig.setId(batchUidService.getUid(qTSysHospitalConfig.getTableName()) + "");
							thosconfig.setHospitalCode(hosDto.getHospitalCode());
							thosconfig.setHospitalId(hosDto.getHospitalId());
							thosconfig.setSysConfigId(sysConfigId);
							syshosConfigClause.populate(thosconfig).addBatch();
						}
						syshosConfigClause.execute();
					}
				} else {
					if (tvc.getSysConfigType() == null || tvc.getSysConfigType() == 3) {
						tvc.setSysConfigType(3);
						tvc.setInnerIdx(sysConfigId);
						tvc.setSysId(sysConfigId);
					}
					if (tvc.getSysConfigType() == 2) {
						tvc.setInnerIdx(sysConfigId);
					}
					tvc.setUpdatedTime(new Date());
					tvc.setUpdatedBy(loginUserName);
					l = sysConfigService.put(sysConfigId, tvc);
					if (l < 1) {
						throw new RuntimeException("系统配置信息编辑失败!");
					}
					// 医院配置
					List<SysHospitalDto> sysHosList = tvc.getHospitalConfigs();
					sqlQueryFactory.delete(qTSysHospitalConfig).where(qTSysHospitalConfig.sysConfigId.eq(sysConfigId))
							.execute();
					if (sysHosList != null && sysHosList.size() > 0) {
						SQLInsertClause syshosConfigClause = sqlQueryFactory.insert(qTSysHospitalConfig);
						for (SysHospitalDto hosDto : sysHosList) {
							TSysHospitalConfig thosconfig = new TSysHospitalConfig();
							thosconfig.setId(batchUidService.getUid(qTSysHospitalConfig.getTableName()) + "");
							thosconfig.setHospitalCode(hosDto.getHospitalCode());
							thosconfig.setHospitalId(hosDto.getHospitalId());
							thosconfig.setSysConfigId(sysConfigId);
							syshosConfigClause.populate(thosconfig).addBatch();
						}
						syshosConfigClause.execute();
					}
				}
			}
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改平台成功!",
				new RedisDto(redisKeyDtoList).toString());
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "修改系统配置信息", notes = "修改系统配置信息")
	@PostMapping("/updateSysConfig/{platformId}/{sysId}")
	public ResultDto<String> updateSysConfig(@PathVariable("platformId") String platformId,
											 @PathVariable("sysId") String sysId,
			@RequestBody SysConfigDto dto) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
		}
		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}
		// redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTPlatform.id.eq(platformId));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		TPlatform platform = this.getOne(platformId);
		if ("1".equals(platform.getPlatformType())) {
			TSysConfig requestSysConfig = dto.getRequestSysConfig();
			if (requestSysConfig.getSysConfigType() == 1) {
				if(!requestSysConfig.getSysId().equals(sysId)){
					// 判断请求方系统id是否存在多条
					boolean requestSysExsits = sysConfigService.requestSysExsits(requestSysConfig.getSysId());
					if (requestSysExsits) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "请求方系统id不能重复！", "请求方系统id不能重复!");
					}
				}
			}

			List<TSysConfig> jsonArr = dto.getRequestedSysConfigs();
			if (CollectionUtils.isNotEmpty(jsonArr)) {
				String vendorIdStr = "";
				for (TSysConfig vcd : jsonArr) {
					if (vendorIdStr.contains(vcd.getSysId())) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统名称不能重复!", "系统名称不能重复!");
					}
					vendorIdStr += vcd.getSysId();
				}
			}
		}
		List<TSysConfig> allConfig = new ArrayList<>();
		allConfig.add(dto.getRequestSysConfig());
		allConfig.addAll(dto.getRequestedSysConfigs());
		allConfig.remove(null);
		if (allConfig.size() > 0) {
			for (int i = 0; i < allConfig.size(); i++) {
				TSysConfig tvc = allConfig.get(i);
				if(StringUtils.isBlank(tvc.getPlatformId())){
					tvc.setPlatformId(platformId);
				}
				if(StringUtils.isBlank(tvc.getProjectId())) {
					tvc.setProjectId(platform.getProjectId());
				}
				String sysConfigId = tvc.getId();
				if("2".equals(platform.getPlatformType())) {
					tvc.setSysConfigType(3);
					tvc.setSysId("0");
					tvc.setConnectionType("0");
				}
				// 新增系统配置信息
				if (StringUtils.isBlank(sysConfigId)) {
					sysConfigId = batchUidService.getUid(qTSysConfig.getTableName()) + "";
					tvc.setId(sysConfigId);
					if (tvc.getSysConfigType() != 1) {
						tvc.setInnerIdx(sysConfigId);
					}
					tvc.setCreatedTime(new Date());
					tvc.setCreatedBy(loginUserName);
					sysConfigService.post(tvc);
					// 医院配置
					List<SysHospitalDto> sysHosList = tvc.getHospitalConfigs();
					if (sysHosList != null && sysHosList.size() > 0) {
						SQLInsertClause syshosConfigClause = sqlQueryFactory.insert(qTSysHospitalConfig);
						for (SysHospitalDto hosDto : sysHosList) {
							TSysHospitalConfig thosconfig = new TSysHospitalConfig();
							thosconfig.setId(batchUidService.getUid(qTSysHospitalConfig.getTableName()) + "");
							thosconfig.setHospitalCode(hosDto.getHospitalCode());
							thosconfig.setHospitalId(hosDto.getHospitalId());
							thosconfig.setSysConfigId(sysConfigId);
							syshosConfigClause.populate(thosconfig).addBatch();
						}
						syshosConfigClause.execute();
					}
				} else {
					if (tvc.getSysConfigType() != 1) {
						tvc.setInnerIdx(sysConfigId);
					}
					tvc.setUpdatedTime(new Date());
					tvc.setUpdatedBy(loginUserName);
					long l = sysConfigService.put(sysConfigId, tvc);
					if (l < 1) {
						throw new RuntimeException("系统配置信息编辑失败!");
					}
					// 医院配置
					List<SysHospitalDto> sysHosList = tvc.getHospitalConfigs();
					sqlQueryFactory.delete(qTSysHospitalConfig).where(qTSysHospitalConfig.sysConfigId.eq(sysConfigId))
							.execute();
					if (sysHosList != null && sysHosList.size() > 0) {
						SQLInsertClause syshosConfigClause = sqlQueryFactory.insert(qTSysHospitalConfig);
						for (SysHospitalDto hosDto : sysHosList) {
							TSysHospitalConfig thosconfig = new TSysHospitalConfig();
							thosconfig.setId(batchUidService.getUid(qTSysHospitalConfig.getTableName()) + "");
							thosconfig.setHospitalCode(hosDto.getHospitalCode());
							thosconfig.setHospitalId(hosDto.getHospitalId());
							thosconfig.setSysConfigId(sysConfigId);
							syshosConfigClause.populate(thosconfig).addBatch();
						}
						syshosConfigClause.execute();
					}
				}
			}
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "修改系统配置信息成功!", new RedisDto(redisKeyDtoList).toString());
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "更改启停用状态", notes = "更改启停用状态")
	@PostMapping("/updateStatus")
	public ResultDto<String> updateStatus(
			@ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id,
			@ApiParam(value = "平台状态 1启用 2停用") @RequestParam(value = "platformStatus", required = true) String platformStatus) {
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}
		// redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTPlatform.id.eq(id));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		long l = sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformStatus, platformStatus)
				.set(qTPlatform.updatedTime, new Date()).set(qTPlatform.updatedBy, loginUserName)
				.where(qTPlatform.id.eq(id)).execute();
		if (l < 1) {
			throw new RuntimeException("平台状态更改失败!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "平台状态更改成功!", new RedisDto(redisKeyDtoList).toString());
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "删除平台", notes = "删除平台")
	@PostMapping("/deletePlatform")
	public ResultDto<String> deletePlatform(
			@ApiParam(value = "平台id") @RequestParam(value = "id", required = true) String id) {
		TPlatform tp = this.getOne(id);
		if (tp == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该平台不存在!", "该平台不存在!");
		}
		// redis缓存信息获取
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTPlatform.id.eq(id));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
		// 删除平台
		long count = this.delete(id);
		if (count <= 0) {
			throw new RuntimeException("平台删除失败!");
		}
		// 删除平台下所有关联的接口配置
		List<TBusinessInterface> tbiList = businessInterfaceService.getListByPlatform(id);
		if (CollectionUtils.isNotEmpty(tbiList)) {
			for (TBusinessInterface tbi : tbiList) {
				long l = businessInterfaceService.delete(tbi.getId());
				if (l < 1) {
					logger.error("平台下所有关联的接口配置删除失败!");
					throw new RuntimeException("平台下所有关联的接口配置删除失败!");
				}
			}
		}
		// 删除平台下的所有系统配置信息
		long vcCount = sysConfigService.delSysConfigAll(id);
		if (vcCount < 1) {
			logger.error("平台下所有系统配置信息删除失败!");
			throw new RuntimeException("平台下所有系统配置信息删除失败!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,
				"平台" + tp.getPlatformName() + "删除成功,同时删除该平台下" + vcCount + "条系统配置",
				new RedisDto(redisKeyDtoList).toString());
	}

	/**
	 * 获取项目下所有平台信息
	 * 
	 * @param projectId
	 * @return
	 */
	public List<TPlatform> getListByProjectId(String projectId) {
		List<TPlatform> list = sqlQueryFactory.select(qTPlatform).from(qTPlatform)
				.where(qTPlatform.projectId.eq(projectId)).fetch();
		return list;
	}

	/**
	 * 校验项目下是否有相同平台名称
	 * 
	 * @param platformId
	 * @param projectId
	 * @param platformName
	 * @return
	 */
	public boolean getPlatformNameIsExistByProjectId(String platformId, String projectId, String platformName) {
		if (StringUtils.isBlank(platformName)) {
			return true;
		}
		ArrayList<Predicate> list = new ArrayList<>();
		list.add(qTPlatform.projectId.eq(projectId));
		list.add(qTPlatform.platformName.eq(platformName.trim()));
		if (StringUtils.isNotEmpty(platformId)) {
			list.add(qTPlatform.id.notEqualsIgnoreCase(platformId));
		}
		TPlatform tp = sqlQueryFactory.select(qTPlatform).from(qTPlatform)
				.where(list.toArray(new Predicate[list.size()])).fetchFirst();
		if (tp != null) {
			return true;
		}
		return false;
	}

}
