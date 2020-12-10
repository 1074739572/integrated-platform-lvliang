package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.ExceptionUtil;
import com.iflytek.integrated.common.ResultDto;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author czzhan
 * 测试Service
 */
@RestController
public class TestService {
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    @ApiOperation(value = "测试接口", notes = "测试接口")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = "v1", required = true)
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResultDto test(@RequestParam("test") String test) {
        try {
            return new ResultDto(Boolean.TRUE, "111", null);
        } catch (Exception e) {
            logger.error("根据用户id查询当前所属应用集合失败!", e);
            return new ResultDto(Boolean.FALSE, ExceptionUtil.dealException(e), null);
        }
    }
}
