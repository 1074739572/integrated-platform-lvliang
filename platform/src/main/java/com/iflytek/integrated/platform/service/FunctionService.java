package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.entity.TFunction;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTFunction.qTFunction;
/**
* 产品功能
* @author weihe9
* @date 2020/12/16 14:21
*/
@Slf4j
@Api(tags = "产品功能")
@RestController
@RequestMapping("/{version}/pt/functionManage")
public class FunctionService extends BaseService<TFunction, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);

    public FunctionService(){
        super(qTFunction, qTFunction.id);
    }


    @ApiOperation(value = "获取产品功能下拉")
    @GetMapping("/getDisFunction")
    public ResultDto<List<TFunction>> getDisFunction() {
        List<TFunction> functions = sqlQueryFactory.select(
                Projections.bean(
                        TFunction.class,
                        qTFunction.id,
                        qTFunction.functionCode,
                        qTFunction.functionName
                )
        ).from(qTFunction).orderBy(qTFunction.createdTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"获取产品功能下拉成功", functions);
    }

    /**
     * 根据功能那个获取功能信息
     * @param functionName
     * @return
     */
    public TFunction getObjByName(String functionName) {
        return sqlQueryFactory.select(qTFunction).from(qTFunction).where(qTFunction.functionName.eq(functionName)).fetchFirst();
    }


}
