package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.TypeDto;
import com.iflytek.integrated.platform.entity.TType;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.dml.SQLInsertClause;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTType.qTType;

/**
* 分类
* @author weihe9
* @date 2020/12/20 17:02
*/
@Slf4j
@Api(tags = "分类管理")
@RestController
@RequestMapping("/{version}/pt/typeManage")
public class TypeService extends BaseService<TType, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(TypeService.class);

    public TypeService(){
        super(qTType, qTType.id);
    }

    @Autowired
    private InterfaceService interfaceService;

    @ApiOperation(value = "获取分类")
    @GetMapping("/getType")
    public ResultDto<List<TType>> getType(@ApiParam(value = "分类类型 0其它 1接口 2驱动 3插件") @RequestParam(value = "type", required = true) Integer type) {
        ArrayList<Predicate> list = new ArrayList<>();
        if(type != null) {
            list.add(qTType.type.eq(type));
        }
        List<TType> typeList = sqlQueryFactory.select(
                Projections.bean(TType.class, qTType.id, qTType.type, qTType.typeCode, qTType.typeName, qTType.updatedTime))
                .from(qTType)
                .where(list.toArray(new Predicate[list.size()]))
                .orderBy(qTType.createdTime.desc())
                .fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"数据获取成功!", typeList);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "批量新增分类", notes ="批量新增分类")
    @PostMapping("/saveBatchType")
    public ResultDto<String> saveBatchType(@RequestBody TypeDto dto){
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        return this.saveType(dto,loginUserName);
    }

    //新增分类
    private ResultDto saveType(TypeDto dto, String loginUserName) {
        List<TType> typeList = dto.getTypeList();
        String maxId = sqlQueryFactory.select(qTType.id.max().as("id")).from(qTType).
                where(qTType.type.eq(1)).fetchOne();
        if(typeList.size()>0){
            SQLInsertClause qTypeClause = sqlQueryFactory.insert(qTType);
            for(int i = 0;i<typeList.size(); i++){
                TType type = typeList.get(i);
                String typeName = type.getTypeName();
                if (StringUtils.isBlank(typeName)) {
                    return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "类型名为空!", "类型名为空!");
                }
                if (null != this.getTypeByName(typeName)) {
                    return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该类型名已存在!", "该类型名已存在!");
                }
                Integer typeId = Integer.valueOf(maxId) + i +1;
                type.setId(String.valueOf(typeId));
                type.setTypeCode(generateCode(qTType.typeCode,qTType,type.getTypeName()));
                type.setCreatedBy(loginUserName);
                type.setCreatedTime(new Date());
                qTypeClause.populate(type).addBatch();
            }
            qTypeClause.execute();
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "类型新增成功!", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "删除类型", notes = "删除类型")
    @DeleteMapping("/deleteType/{typeIds}")
    public ResultDto<String> deleteType(
            @ApiParam(value = "类型id") @PathVariable(value = "typeIds", required = true) String typeIds) {
        if (StringUtils.isBlank(typeIds)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        for (String id : typeIds.split(",")) {
            long l = this.delete(id);
            if(l<1){
                logger.error("接口类型删除失败!");
                throw new RuntimeException("接口类型删除失败!");
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"接口类型删除成功!","接口类型删除成功!");
    }

    //根据类型名获取类型
    private TType getTypeByName(String typeName){
        if (StringUtils.isBlank(typeName)) {
            return null;
        }
        return sqlQueryFactory.select(qTType).from(qTType).where(qTType.typeName.eq(typeName)).fetchFirst();
    }



}
