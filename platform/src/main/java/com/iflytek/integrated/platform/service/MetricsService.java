package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.Constant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Api(tags = "服务监控管理")
@RestController
@RequestMapping("/{version}/pt/metricsManage")
public class MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(com.iflytek.integrated.platform.service.CdaService.class);

    @Value("${grafana:404}")
    private String grafana;

    @Value("#{'${monitorServers:404}'.split(',')}")
    private String[] serverHosts;

    @ApiOperation(value = "获取监控地址")
    @GetMapping("/getGrafanaUrl")
    public ResultDto<String> getMetrics() {
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", grafana);
    }

    @ApiOperation(value = "获取监控服务器列表")
    @GetMapping("/getServerHost")
    public ResultDto<List<String>> getServerHost() {
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", Arrays.asList(serverHosts));
    }
}
