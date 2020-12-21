package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTType.qTType;

/**
* 分类
* @author weihe9
* @date 2020/12/20 17:02
*/
public class TypeService extends QuerydslService<TType, String, TType, StringPath, PageRequest<TType>> {

    private static final Logger logger = LoggerFactory.getLogger(TypeService.class);

    public TypeService(){
        super(qTType, qTType.id);
    }


    @ApiOperation(value = "获取分类")
    @GetMapping("/getType")
    public ResultDto getType(Integer type, String typeName) {
        List<TType> vendors = sqlQueryFactory.select(
                Projections.bean(
                        TType.class,
                        qTType.id,
                        qTType.type,
                        qTType.typeCode,
                        qTType.typeName
                )
        ).from(qTType).orderBy(qTType.updatedTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", vendors);
    }

}
