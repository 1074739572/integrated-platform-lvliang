package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.kvn.mockj.Mock;
import com.querydsl.sql.SQLQueryFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author czzhan
 * 匿名访问service，无需登录
 */
@Slf4j
@Api(tags = "匿名访问接口")
@RestController
@RequestMapping("/{version}/pb/anonymous")
public class AnonymousService {
    private static final Logger logger = LoggerFactory.getLogger(AnonymousService.class);

    @Autowired
    protected SQLQueryFactory sqlQueryFactory;
    @Autowired
    private InterfaceService interfaceService;

    @ApiOperation(value = "mock测试")
    @GetMapping("/getMock")
    public ResultDto getMock(String id) {
        try {
            //获取mock
            ResultDto dto = interfaceService.getMockTemplate(id);
            if(Constant.ResultCode.SUCCESS_CODE != dto.getCode()){
                return dto;
            }
            String template = (String) dto.getData();
            String json = Mock.mock(template);
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", json);
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "获取mock数值错误"+e.getMessage());
        }
    }

}
