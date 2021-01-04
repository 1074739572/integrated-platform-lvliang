package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.utils.XmlJsonUtils;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.utils.Utils;
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
            //获取参数类型
            String type = Constant.ParamFormatType.getByType(businessInterface.getOutParamFormatType());
            if(StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)){
                throw new RuntimeException("出参参数类型无效");
            }
            //获取模板
            String template = StringUtils.isNotEmpty(businessInterface.getMockTemplate())?
                    businessInterface.getMockTemplate():businessInterface.getOutParamFormat();

            //如果无需模拟
            if(businessInterface.getMockIsUse() == 0){
                return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", template);
            }
            //根据模板类型处理
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", resultMock(template, type));
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "获取mock数值错误："+e.getMessage());
        }
    }

    @ApiOperation(value = "mock")
    @PostMapping("/mock")
    public ResultDto mock(@RequestBody String mock) {
        String type = Utils.strIsJsonOrXml(mock);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", resultMock(mock, type));
    }

    /**
     *
     * @param mock
     * @param type
     * @return
     */
    private String resultMock(String mock,String type){
        //根据模板类型处理
        if(Constant.ParamFormatType.JSON.getType().equals(type)){
            //JSON类型
            return Mock.mock(mock);
        }
        else {
            //XML类型
            mock = XmlJsonUtils.convertXmlIntoJSONObject(mock);
            String json = Mock.mock(mock);
            return XmlJsonUtils.jsonToXml(json);
        }
    }
}
