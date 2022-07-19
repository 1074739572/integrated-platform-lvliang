package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.config.MetricsConfig;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.Constant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "服务监控管理")
@RestController
@RequestMapping("/{version}/pt/metricsManage")
public class MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(com.iflytek.integrated.platform.service.CdaService.class);

    @Autowired
    private MetricsConfig config;

    @ApiOperation(value = "获取服务分类列表")
    @GetMapping("/getUrlList")
    public ResultDto<List<MetricsConfig.Metrics>> getMetrics() {
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", config.getList());
    }
}
