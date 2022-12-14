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
import com.iflytek.integrated.platform.entity.TEtlFlow;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.iflytek.integrated.platform.entity.TSysHospitalConfig;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
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

import static com.iflytek.integrated.platform.entity.QTEtlGroup.qTEtlGroup;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;

/**
 * ????????????
 * 
 * @author weihe9
 * @date 2020/12/12 16:58
 */
@Slf4j
@Api(tags = "????????????")
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
	@Autowired
	private EtlFlowService etlFlowService;
	@Autowired
	private EtlGroupService etlGroupService;
	@Autowired
	private NiFiRequestUtil niFiRequestUtil;

	private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);

	public PlatformService() {
		super(qTPlatform, qTPlatform.id);
	}

	@ApiOperation(value = "????????????id????????????(??????)", notes = "????????????id????????????(??????)")
	@GetMapping("/getPlatformListById")
	public ResultDto<TableData<TPlatform>> getPlatformListById(
			@ApiParam(value = "??????id") @RequestParam(value = "projectId", required = true) String projectId,
			@ApiParam(value = "????????????") @RequestParam(value = "platformStatus", required = false) String platformStatus,
			@ApiParam(value = "????????????") @RequestParam(value = "platformName", required = false) String platformName,
			@ApiParam(value = "??????", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "????????????", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
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
			// ??????
			TableData<TPlatform> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????id??????????????????!", tableData);
		} catch (Exception e) {
			logger.error("????????????id??????????????????! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????id??????????????????!");
		}
	}

	@ApiOperation(value = "????????????id????????????(ETL??????????????????)", notes = "????????????id????????????(ETL??????????????????)")
	@GetMapping("/getEtlPlatformListById/{projectId}")
	public ResultDto<List<TPlatform>> getEtlPlatformListById(@ApiParam(value = "??????id") @PathVariable String projectId) {
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
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????id??????????????????!", queryResults);
		} catch (Exception e) {
			logger.error("????????????id??????????????????! MSG:{}", ExceptionUtil.dealException(e));
			e.printStackTrace();
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????id??????????????????!");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "??????or????????????", notes = "??????or????????????")
	@PostMapping("/saveAndUpdatePlatform")
	public ResultDto<String> saveAndUpdatePlatform(@RequestBody PlatformDto dto,@RequestParam("loginUserName") String loginUserName) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
		}
		// ?????????????????????????????????
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
		}
		// ??????????????????
		String platformId = dto.getId();
		String platformName = dto.getPlatformName();
		boolean isExist = getPlatformNameIsExistByProjectId(platformId, dto.getProjectId(), platformName);
		if (isExist) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????????????????????????????!", platformName);
		}
		SysConfigDto sysConfig = dto.getSysConfig();
		if ("1".equals(dto.getPlatformType())) {
			TSysConfig requestSysConfig = sysConfig.getRequestSysConfig();
			if (requestSysConfig.getSysConfigType() == 1) {
				// ?????????????????????id??????????????????
				if(!requestSysConfig.getSysId().equals(dto.getSysId())){
					boolean requestSysExsits = sysConfigService.requestSysExsits(requestSysConfig.getSysId());
					if (requestSysExsits) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????????????????", "??????????????????????????????!");
					}
				}
			}

			List<TSysConfig> jsonArr = sysConfig.getRequestedSysConfigs();
			if (CollectionUtils.isNotEmpty(jsonArr)) {
				String vendorIdStr = "";
				for (TSysConfig vcd : jsonArr) {
					if (StringUtils.isNotBlank(vcd.getSysId()) && vendorIdStr.contains(vcd.getSysId())) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????!", "????????????????????????!");
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

	/** ???????????? */
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
		// ????????????
		SysConfigDto configDto = dto.getSysConfig();
		if (configDto == null) {
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", null);
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
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", "??????????????????!");
	}

	/** ???????????? */
	private ResultDto updatePlatform(PlatformDto dto, String loginUserName) {
		// ??????id
		String platformId = dto.getId();
        // redis??????????????????
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTPlatform.id.eq(platformId));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		long l = sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformName, dto.getPlatformName())
				.set(qTPlatform.platformType, dto.getPlatformType()).set(qTPlatform.etlServerUrl, dto.getEtlServerUrl())
				.set(qTPlatform.etlUser, dto.getEtlUser()).set(qTPlatform.etlPwd, dto.getEtlPwd())
				.set(qTPlatform.projectId, dto.getProjectId()).set(qTPlatform.updatedTime, new Date())
				.set(qTPlatform.updatedBy, loginUserName).where(qTPlatform.id.eq(platformId)).execute();
		if (l < 1) {
			throw new RuntimeException("??????????????????!");
		}
		// ??????????????????????????????
		SysConfigDto configDto = dto.getSysConfig();
		if (configDto == null) {
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!", null);
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
				// ??????????????????
				if (StringUtils.isBlank(sysConfigId)) {
					sysConfigId = batchUidService.getUid(qTSysConfig.getTableName()) + "";
					tvc.setId(sysConfigId);
					if (tvc.getSysConfigType() != 1) {
						tvc.setInnerIdx(sysConfigId);
					}
					tvc.setCreatedTime(new Date());
					tvc.setCreatedBy(loginUserName);
					sysConfigService.post(tvc);
					// ????????????
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
						throw new RuntimeException("??????????????????????????????!");
					}
					// ????????????
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
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????!",
				new RedisDto(redisKeyDtoList).toString());
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "????????????????????????", notes = "????????????????????????")
	@PostMapping("/updateSysConfig/{platformId}/{sysId}")
	public ResultDto<String> updateSysConfig(@PathVariable("platformId") String platformId,
											 @PathVariable("sysId") String sysId,
			@RequestBody SysConfigDto dto) {
		if (dto == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
		}
		// ?????????????????????????????????
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
		}
		// redis??????????????????
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTPlatform.id.eq(platformId));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		TPlatform platform = this.getOne(platformId);
		if ("1".equals(platform.getPlatformType())) {
			TSysConfig requestSysConfig = dto.getRequestSysConfig();
			if (requestSysConfig.getSysConfigType() == 1) {
				if(!requestSysConfig.getSysId().equals(sysId)){
					// ?????????????????????id??????????????????
					boolean requestSysExsits = sysConfigService.requestSysExsits(requestSysConfig.getSysId());
					if (requestSysExsits) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????id???????????????", "???????????????id????????????!");
					}
				}
			}

			List<TSysConfig> jsonArr = dto.getRequestedSysConfigs();
			if (CollectionUtils.isNotEmpty(jsonArr)) {
				String vendorIdStr = "";
				for (TSysConfig vcd : jsonArr) {
					if (vendorIdStr.contains(vcd.getSysId())) {
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "????????????????????????!", "????????????????????????!");
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
				// ????????????????????????
				if (StringUtils.isBlank(sysConfigId)) {
					sysConfigId = batchUidService.getUid(qTSysConfig.getTableName()) + "";
					tvc.setId(sysConfigId);
					if (tvc.getSysConfigType() != 1) {
						tvc.setInnerIdx(sysConfigId);
					}
					tvc.setCreatedTime(new Date());
					tvc.setCreatedBy(loginUserName);
					sysConfigService.post(tvc);
					// ????????????
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
						throw new RuntimeException("??????????????????????????????!");
					}
					// ????????????
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
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????!", new RedisDto(redisKeyDtoList).toString());
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "?????????????????????", notes = "?????????????????????")
	@PostMapping("/updateStatus")
	public ResultDto<String> updateStatus(
			@ApiParam(value = "??????id") @RequestParam(value = "id", required = true) String id,
			@ApiParam(value = "???????????? 1?????? 2??????") @RequestParam(value = "platformStatus", required = true) String platformStatus) {
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
		}
		// redis??????????????????
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTPlatform.id.eq(id));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		long l = sqlQueryFactory.update(qTPlatform).set(qTPlatform.platformStatus, platformStatus)
				.set(qTPlatform.updatedTime, new Date()).set(qTPlatform.updatedBy, loginUserName)
				.where(qTPlatform.id.eq(id)).execute();
		if (l < 1) {
			throw new RuntimeException("????????????????????????!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "????????????????????????!", new RedisDto(redisKeyDtoList).toString());
	}

	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "????????????", notes = "????????????")
	@PostMapping("/deletePlatform")
	public ResultDto<String> deletePlatform(
			@ApiParam(value = "??????id") @RequestParam(value = "id", required = true) String id) {
		TPlatform tp = this.getOne(id);
		if (tp == null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
		}
		// redis??????????????????
		ArrayList<Predicate> arr = new ArrayList<>();
		arr.add(qTPlatform.id.eq(id));
		List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
		// ????????????
		long count = this.delete(id);
		if (count <= 0) {
			throw new RuntimeException("??????????????????!");
		}
		// ??????????????????????????????????????????
		List<TBusinessInterface> tbiList = businessInterfaceService.getListByPlatform(id);
		if (CollectionUtils.isNotEmpty(tbiList)) {
			for (TBusinessInterface tbi : tbiList) {
				long l = businessInterfaceService.delete(tbi.getId());
				if (l < 1) {
					logger.error("????????????????????????????????????????????????!");
					throw new RuntimeException("????????????????????????????????????????????????!");
				}
			}
		}
		// ??????????????????????????????????????????
		long vcCount = sysConfigService.delSysConfigAll(id);
		if (vcCount < 1) {
			logger.error("?????????????????????????????????????????????!");
			throw new RuntimeException("?????????????????????????????????????????????!");
		}
		
		//??????????????????????????????ETL??????
		List<TEtlFlow> tEtlFlowIds = etlFlowService.getTEtlFlowIds(id);
		if (!CollectionUtils.isEmpty(tEtlFlowIds)) {
			for (TEtlFlow tEtlFlow : tEtlFlowIds) {
				long l = etlFlowService.delete(tEtlFlow.getId());
				if (l < 1) {
					throw new RuntimeException("??????????????????ETL????????????????????????!");
				}
				try {
					niFiRequestUtil.deleteNifiEtlFlow(tp , tEtlFlow.getEtlGroupId() , tEtlFlow.getParentGroupId());
				} catch (Exception e) {
					throw new RuntimeException("??????ETL???????????????????????????????????????"+e.getLocalizedMessage());
				}
			}
		}
		//??????????????????????????????ETL?????????
		List<String> tEtlGroupIds = etlGroupService.getTEtlGroupIds(id);
		if (!CollectionUtils.isEmpty(tEtlGroupIds)) {
			for (String tEtlGroupId : tEtlGroupIds) {
//				long l = etlGroupService.delete(tEtlGroupId);
				long l = sqlQueryFactory.delete(qTEtlGroup).where(qTEtlGroup.etlGroupId.eq(tEtlGroupId)).execute();
				if (l < 1) {
					throw new RuntimeException("??????????????????ETL???????????????????????????!");
				}
//				try {
//					niFiRequestUtil.deleteNifiEtlFlow(tp , tEtlGroupId);
//				} catch (Exception e) {
//					throw new RuntimeException("??????ETL???????????????????????????????????????"+e.getLocalizedMessage());
//				}
			}
		}
		
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,
				"??????" + tp.getPlatformName() + "????????????,????????????????????????" + vcCount + "???????????????",
				new RedisDto(redisKeyDtoList).toString());
	}

	/**
	 * ?????????????????????????????????
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
	 * ??????????????????????????????????????????
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
