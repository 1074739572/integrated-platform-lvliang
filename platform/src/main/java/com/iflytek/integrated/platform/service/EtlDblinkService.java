package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTEtlDblink.qTEtlDblink;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.TEtlDblink;
import com.querydsl.core.types.dsl.StringPath;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lsn
 */
@Slf4j
@Api(tags = "etl数据库关联配置服务")
@RestController
@RequestMapping("/{version}/pt/etldblink")
public class EtlDblinkService extends BaseService<TEtlDblink, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(EtlDblinkService.class);

	public EtlDblinkService() {
		super(qTEtlDblink, qTEtlDblink.id);
	}

	@ApiOperation(value = "根据数据库配置获取关联流程及组件信息")
	@GetMapping("/getEtlDblinks/{dbConfigId}")
	public ResultDto<List<TEtlDblink>> getEtlDblinks(@PathVariable("dbConfigId") String dbConfigId ) {
		List<TEtlDblink> queryResults = sqlQueryFactory.select(qTEtlDblink).from(qTEtlDblink)
				.where(qTEtlDblink.dbConfigId.eq(dbConfigId)).fetch();
		return new ResultDto<List<TEtlDblink>>(Constant.ResultCode.SUCCESS_CODE, "根据数据库配置获取关联流程及组件信息成功", queryResults);
	}
	
	@ApiOperation(value = "保存流程组件数据库配置")
	@PostMapping("/saveEtlDblinks")
	public ResultDto<String> getEtlFlows(@RequestBody List<TEtlDblink> etlDbLinks) {
		try {
			for(TEtlDblink dbLink : etlDbLinks) {
				this.post(dbLink);
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存流程组件数据库配置成功", "success");
		}catch(Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "保存流程组件数据库配置失败", e.getLocalizedMessage());
		}
	}
	
	@ApiOperation(value = "删除流程数据库配置")
	@PostMapping("/delEtlDblinks/{etlGroupId}")
	public ResultDto<String> delEtlDblinks(@PathVariable String etlGroupId) {
		try {
			sqlQueryFactory.delete(qTEtlDblink).where(qTEtlDblink.etlGroupId.eq(etlGroupId)).execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "删除流程数据库配置成功", "success");
		}catch(Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "删除流程数据库配置失败", e.getLocalizedMessage());
		}
	}
	
	@ApiOperation(value = "修改流程時批量更新流程数据库配置成功")
	@PostMapping("/updateEtlByFlow")
	public ResultDto<String> updateEtlByFlow(@RequestBody List<TEtlDblink> etlDblinks) {
		try {
			for(TEtlDblink etlDblink : etlDblinks) {
				sqlQueryFactory.update(qTEtlDblink).set(qTEtlDblink.dbConfigId, etlDblink.getDbConfigId()).where(qTEtlDblink.etlGroupId.eq(etlDblink.getEtlGroupId()).and(qTEtlDblink.etlProcessorId.eq(etlDblink.getEtlProcessorId()))).execute();
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "批量更新流程数据库配置成功", "success");
		}catch(Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "批量更新流程数据库配置失败", e.getLocalizedMessage());
		}
	}
}
