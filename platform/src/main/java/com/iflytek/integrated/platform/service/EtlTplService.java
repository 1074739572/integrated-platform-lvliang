package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTEtlTpl.qTEtlTpl;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.EtlTplDto;
import com.iflytek.integrated.platform.entity.TEtlTpl;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lsn
 */
@Slf4j
@Api(tags = "etl服务")
@RestController
@RequestMapping("/{version}/pt/etltpl")
public class EtlTplService extends BaseService<TEtlTpl, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(EtlTplService.class);

	@Autowired
	private BatchUidService batchUidService;

	public EtlTplService() {
		super(qTEtlTpl, qTEtlTpl.id);
	}

	@ApiOperation(value = "分页获取模板")
	@GetMapping("/getEtlTpls/{tplType}/{tplFunType}")
	public ResultDto<TableData<TEtlTpl>> getEtlTpls(@PathVariable Integer tplType, @PathVariable Integer tplFunType,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize , @RequestParam(value = "tplName" , required = false) String tplName) {
		BooleanExpression conditon = qTEtlTpl.tplType.eq(tplType);
		if (tplFunType != 0) {
			conditon = conditon.and(qTEtlTpl.tplFunType.eq(tplFunType));
		}
		if(StringUtils.isNotBlank(tplName)) {
			conditon = conditon.and(qTEtlTpl.tplName.like("%" + tplName + "%"));
		}
		QueryResults<TEtlTpl> queryResults = sqlQueryFactory.select(qTEtlTpl).from(qTEtlTpl).where(conditon)
				.limit(pageSize).offset((pageNo - 1) * pageSize).orderBy(qTEtlTpl.createdTime.desc()).fetchResults();

		// 分页
		TableData<TEtlTpl> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", tableData);
	}

	@ApiOperation(value = "获取模板")
	@GetMapping("/listEtlTpls/{tplType}/{tplFunType}")
	public ResultDto<List<TEtlTpl>> listEtlTpls(@PathVariable Integer tplType, @PathVariable Integer tplFunType , @RequestParam(value = "tplName" , required = false) String tplName) {
		BooleanExpression conditon = qTEtlTpl.tplType.eq(tplType);
		if (tplFunType != 0) {
			conditon = conditon.and(qTEtlTpl.tplFunType.eq(tplFunType));
		}
		if(StringUtils.isNotBlank(tplName)) {
			conditon = conditon.and(qTEtlTpl.tplName.like("%" + tplName + "%"));
		}
		List<TEtlTpl> queryResults = sqlQueryFactory.select(qTEtlTpl).from(qTEtlTpl).where(conditon)
				.orderBy(qTEtlTpl.createdTime.desc()).fetch();

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", queryResults);
	}

	@ApiOperation(value = "上传模板")
	@PostMapping(path = "/uploadEtlTpls/{tplType}/{tplFunType}")
	public ResultDto<String> uploadEtlTpls(@PathVariable Integer tplType, @PathVariable Integer tplFunType,
			@RequestParam("tplFiles") MultipartFile[] tplFiles , String tplDesp) {

		System.out.println(tplDesp);
		// 校验是否获取到登录用户
		String loginUserName = "admin";
//		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
//		if (StringUtils.isBlank(loginUserName)) {
//			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
//		}

		SQLInsertClause insertClause = sqlQueryFactory.insert(qTEtlTpl);
		// 流程模板
		try {
			if (tplFiles == null || tplFiles.length == 0) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到上传文件!", "没有获取到上传文件!");
			}
			for (MultipartFile file : tplFiles) {
				String fileName = file.getOriginalFilename();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				InputStream is = file.getInputStream();
				String tplContent = IOUtils.toString(is, "UTF-8");
				is.close();
				String id = batchUidService.getUid(qTEtlTpl.getTableName()) + "";
				insertClause.set(qTEtlTpl.id, id).set(qTEtlTpl.tplName, fileName).set(qTEtlTpl.tplType, tplType)
						.set(qTEtlTpl.tplFunType, tplFunType).set(qTEtlTpl.tplContent, tplContent)
						.set(qTEtlTpl.tplDesp, tplDesp)
						.set(qTEtlTpl.createdBy, loginUserName != null ? loginUserName : "")
						.set(qTEtlTpl.createdTime, new Date())
						.set(qTEtlTpl.updatedBy, loginUserName != null ? loginUserName : "")
						.set(qTEtlTpl.updatedTime, new Date()).addBatch();
			}
			long insertCount = insertClause.execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "流程模板文件上传成功", insertCount + "");
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "上传文件入库处理失败", e.getLocalizedMessage());
		}
	}

	@ApiOperation(value = "修改模板")
	@PostMapping(path = "/editEtlTpl/{id}")
	public ResultDto<String> editEtlTpl(@PathVariable Integer id, @RequestParam("tplFile") MultipartFile tplFile,
			@RequestBody EtlTplDto tplDto) {

		// 校验是否获取到登录用户
		String loginUserName = "admin";
//		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
//		if (StringUtils.isBlank(loginUserName)) {
//			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
//		}

		SQLUpdateClause updateClause = sqlQueryFactory.update(qTEtlTpl);
		// 流程模板
		try {
			String fileName = "";
			String tplContent = "";
			if (tplFile != null) {
				fileName = tplFile.getOriginalFilename();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				InputStream is = tplFile.getInputStream();
				tplContent = IOUtils.toString(is, "UTF-8");
				is.close();
			}
			if (StringUtils.isNotBlank(fileName)) {
				updateClause.set(qTEtlTpl.tplName, fileName);
			}
			if (StringUtils.isNotBlank(tplContent)) {
				updateClause.set(qTEtlTpl.tplContent, tplContent);
			}
			if (tplDto != null && tplDto.getTplType() != null) {
				updateClause.set(qTEtlTpl.tplType, tplDto.getTplType());
			}
			if (tplDto != null && tplDto.getTplFunType() != null) {
				updateClause.set(qTEtlTpl.tplFunType, tplDto.getTplFunType());
			}
			updateClause.set(qTEtlTpl.updatedBy, loginUserName != null ? loginUserName : "");
			updateClause.set(qTEtlTpl.updatedTime, new Date()).addBatch();
			long insertCount = updateClause.execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "流程模板文件更新成功", insertCount + "");
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "更新文件入库处理失败", e.getLocalizedMessage());
		}
	}
}
