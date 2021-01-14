package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.utils.ase.AesUtil;
import com.iflytek.integrated.platform.dto.MockDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.kvn.mockj.Mock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private BusinessInterfaceService businessInterfaceService;

    @ApiOperation(value = "mock测试")
    @GetMapping("/getMock")
    public ResultDto getMock(String id) {
        try {
            //获取mock
            TBusinessInterface businessInterface = businessInterfaceService.getOne(id);
            if(businessInterface == null || StringUtils.isEmpty(businessInterface.getId())){
                return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "没有找到接口配置");
            }
            //获取模板
            String template = StringUtils.isNotEmpty(businessInterface.getMockTemplate())?
                    businessInterface.getMockTemplate():businessInterface.getOutParamFormat();
            //如果无需模拟
            if(businessInterface.getMockIsUse() == 0){
                return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", template);
            }
            //根据模板类型处理
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", resultMock(template));
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "获取mock数值错误："+e.getMessage());
        }
    }

    @ApiOperation(value = "mock")
    @PostMapping("/mock")
    public ResultDto mock(@RequestBody String mock) {
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", resultMock(mock));
    }

    @ApiOperation(value = "encrypt")
    @PostMapping("/encrypt")
    public ResultDto encrypt(String content){
        try {
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", AesUtil.encrypt(content));
        }catch (Exception e){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "加密失败");
        }
    }

    @ApiOperation(value = "decrypt")
    @PostMapping("/decrypt")
    public ResultDto decrypt(String content){
        try {
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", AesUtil.decrypt(content));
        }catch (Exception e){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "解密失败");
        }
    }

    /**
     * mock模拟数据调取
     * @param template
     * @return
     */
    private String resultMock(String template){
        //根据模板类型处理
        if(StringUtils.isBlank(template)){
            throw new RuntimeException("mock模板不能为空");
        }
        MockDto mockDto = new MockDto();
        mockDto.setTemplate(template);
        //获取模板
        String mockResult = Mock.mock(JSONObject.toJSONString(mockDto));
        MockDto mock = JSONObject.parseObject(mockResult,MockDto.class);
        return StringUtils.isBlank(mock.getTemplate())?"":
            mock.getTemplate().replaceAll("\n","").replaceAll("\r","").replaceAll("  "," ");
    }

}
