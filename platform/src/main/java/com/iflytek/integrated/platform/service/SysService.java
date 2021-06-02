package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.SysDto;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
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
	private SysConfigService sysConfigService;
	@Autowired
	private BatchUidService batchUidService;
	@Autowired
	private RedisService redisService;

	@ApiOperation(value = "系统列表")
	@GetMapping("/getSysList")
	public ResultDto<TableData<TSys>> getSysList(
			@ApiParam(value = "系统编码") @RequestParam(value = "sysCode", required = false) String sysCode,
			@ApiParam(value = "系统名称") @RequestParam(value = "sysName", required = false) String sysName,
			@ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		try {
			List<Predicate> list = new ArrayList<>();
			SQLQuery<TSys> queryer = sqlQueryFactory.select(qTSys).from(qTSys);
			if (StringUtils.isNotBlank(sysCode)) {
				list.add(qTSys.sysCode.eq(sysCode));
			}
			if (StringUtils.isNotBlank(sysName)) {
				list.add(qTSys.sysCode.like("%" + sysCode + "%"));
			}
			QueryResults<TSys> queryResults = queryer.where(list.toArray(new Predicate[list.size()])).limit(pageSize)
					.offset((pageNo - 1) * pageSize).orderBy(qTSys.createdTime.desc()).fetchResults();
			;
			// 分页
			TableData<TSys> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
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
		// 删除产品功能关联关系前先查询该关联数据是否有项目相关联
		List<TSysConfig> tpplList = sysConfigService.getObjBySysId(id);
		if (CollectionUtils.isNotEmpty(tpplList)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统已在项目中配置使用,无法删除!", "该系统已在项目中配置使用,无法删除!");
		}

		// 删除产品和功能的关联关系
		long lon = sysDriveLinkService.deleteSysDriveLinkBySysId(id);
		if (lon <= 0) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统删除失败!", "系统删除失败!");
		}

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
		String id = dto.getId();
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

	/** 新增产品 */
	private ResultDto saveSys(SysDto dto, String loginUserName) {
		String sysName = dto.getSysName();
		if (StringUtils.isBlank(sysName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "系统名称未填!", dto);
		}
		// 判断输入产品是否是新产品
		TSys tp = getObjBySysName(sysName.trim());
		// 判断输入功能是否是新功能
		if (tp != null) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该系统名称已存在，不能重复!", "该系统名称已存在!");
		}

		// 新增产品
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

	/** 编辑产品 */
	private ResultDto updateSys(SysDto dto, String loginUserName) {
		String sysId = dto.getId();
		String sysName = dto.getSysName();
		String isValid = dto.getIsValid();
		String driveIds = dto.getDriveIds();

		// 更新厂商信息
		long l = 0;
		SQLUpdateClause updater = sqlQueryFactory.update(qTSys);
		if (StringUtils.isNotBlank(sysName)) {
			updater.set(qTSys.sysName, sysName);
		}
		if (StringUtils.isNotBlank(isValid)) {
			updater.set(qTSys.isValid, isValid);
		}
		updater.set(qTSys.updatedTime, new Date()).set(qTSys.updatedBy, loginUserName).where(qTSys.id.eq(sysId))
				.execute();
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
	 * 根据产品名称获取产品信息
	 * 
	 * @param productName
	 * @return
	 */
	public TSys getObjBySysName(String sysName) {
		return sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.sysName.eq(sysName)).fetchFirst();
	}

}
