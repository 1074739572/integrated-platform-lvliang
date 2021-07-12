package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTEtlPulldata.qTEtlPulldata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.TEtlPulldata;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.StringPath;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lsn
 */
@Slf4j
@Api(tags = "etl调试查询结点数据预览服务")
@RestController
@RequestMapping("/{version}/pt/etlpulldata")
public class EtlPulldataViewService extends BaseService<TEtlPulldata, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(EtlPulldataViewService.class);

	public EtlPulldataViewService() {
		super(qTEtlPulldata, qTEtlPulldata.id);
	}

	@ApiOperation(value = "获取调试查询结果列表")
	@GetMapping("/getEtlPulldatas/{etlGroupId}")
	public ResultDto<TableData<TEtlPulldata>> getEtlFlows(@PathVariable("etlGroupId") String etlGroupId , @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		QueryResults<TEtlPulldata> queryResults = sqlQueryFactory.select(qTEtlPulldata).from(qTEtlPulldata)
				.where(qTEtlPulldata.etlGroupId.eq(etlGroupId)).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTEtlPulldata.pageNum.asc()).fetchResults();

		// 分页
		TableData<TEtlPulldata> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取调试查询结果列表成功", tableData);
	}
}
