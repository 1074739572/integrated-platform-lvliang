package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTType.qTType;

/**
* 分类
* @author weihe9
* @date 2020/12/20 17:02
*/
@Slf4j
@Api(tags = "分类")
@RestController
@RequestMapping("/{version}/pt/typeManage")
public class TypeService extends QuerydslService<TType, String, TType, StringPath, PageRequest<TType>> {

    private static final Logger logger = LoggerFactory.getLogger(TypeService.class);

    public TypeService(){
        super(qTType, qTType.id);
    }


    @ApiOperation(value = "获取分类")
    @GetMapping("/getType")
    public ResultDto getType(@ApiParam(value = "分类类型 0其它 1接口 2驱动 3插件") @RequestParam(value = "type", required = true) Integer type) {
        ArrayList<Predicate> list = new ArrayList<>();
        if(type != null) {
            list.add(qTType.type.eq(type));
        }
        List<TType> typeList = sqlQueryFactory.select(
                Projections.bean(TType.class, qTType.id, qTType.type, qTType.typeCode, qTType.typeName))
                .from(qTType)
                .where(list.toArray(new Predicate[list.size()]))
                .orderBy(qTType.updatedTime.desc())
                .fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", typeList);
    }

}
