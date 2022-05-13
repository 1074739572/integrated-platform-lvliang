package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TQI;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTQI.qi;

@Slf4j
@Api(tags = "质检脚本接口")
@RestController
@RequestMapping("/{version}/pt/qi")
public class QIService extends BaseService<TQI,String, StringPath> {

    @Autowired
    private BatchUidService batchUidService;

    @Autowired
    private BusinessInterfaceService businessInterfaceService;

    @Autowired
    private RedisService redisService;

    public QIService() {super(qi, qi.QIId);}

    @ApiOperation(value = "获取列表", notes = "获取质检脚本列表")
    @GetMapping("/getList")
    public ResultDto getList(
            @ApiParam(value = "质检名称") @RequestParam(value = "QIName",required = false) String QIName,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1")Integer pageNo,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10")Integer pageSize
    ){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(QIName)) {
            list.add(qi.QIName.like("%"+QIName+"%"));
        }
        QueryResults<TQI> queryResults = sqlQueryFactory
                .select(Projections.bean(TQI.class,qi.QIId,qi.QIName,qi.QIScript,qi.createdBy,qi.createdTime,qi.updatedTime,qi.updatedBy))
                .from(qi)
                .where(list.toArray(new Predicate[list.size()])).limit(pageSize)
                .limit(pageSize)
                .offset((pageNo - 1) * pageSize)
                .orderBy(qi.createdTime.desc())
                .fetchResults();
        //分页
        TableData<TPlugin> tableData = new TableData(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取列表成功!", tableData);
    }


    @ApiOperation(value = "新增或修改质检脚本", notes = "新增或修改质检脚本")
    @PostMapping("/addOrMod")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "QIId", value = "ID"),
            @ApiImplicitParam(name = "QIName", value = "质检脚本名称", required = true),
            @ApiImplicitParam(name = "QIScript", value = "质检脚本", required = true)
    })
    public ResultDto<String> addOrMod(@RequestBody Map param){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }

        TQI record = sqlQueryFactory.select(Projections.bean(TQI.class,qi.QIId,qi.QIName)).from(qi).where(qi.QIName.eq(param.get("QIName").toString())).fetchFirst();

        String msg = "";

        TQI tqi = new TQI();
        tqi.setQIName(param.get("QIName").toString());
        tqi.setQIScript(param.get("QIScript").toString());
        tqi.setCreatedBy(loginUserName);
        tqi.setCreatedTime(new Date());
        tqi.setUpdatedBy(loginUserName);
        tqi.setUpdatedTime(new Date());
        Object QIId = param.get("QIId");
        if(QIId != null && StringUtils.isNotEmpty(QIId.toString())){
            //修改
            if(record != null && !record.getQIId().equals(QIId.toString())){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "名称已存在！");
            }
            tqi.setQIId(QIId.toString());
            this.put(QIId.toString(),tqi);
            msg = "已修改!";
            // redis缓存信息获取
            ArrayList<Predicate> arr = new ArrayList<>();
            arr.add(qTBusinessInterface.QIId.in(tqi.getQIId()));
            List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,msg, new RedisDto(redisKeyDtoList).toString());
        }else{
            //新增
            if(record != null){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "名称已存在！");
            }
            tqi.setQIId(batchUidService.getUid(qi.getTableName())+"");
            this.post(tqi);
            msg = "已新增!";
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, msg);
        }
    }


    @ApiOperation(value = "删除", notes = "删除质检脚本")
    @PostMapping(value = "/del")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "QIId", value = "ID",required = true)
    })
    public ResultDto del(@RequestBody Map param){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        Object QIId = param.get("QIId");
        if(QIId != null && StringUtils.isNotEmpty(QIId.toString())){
            if(businessInterfaceService.selectByQIId(QIId.toString()) > 0){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该质检脚本已被使用，不可删除！");
            }
            //redis缓存信息获取
            ArrayList<Predicate> arr = new ArrayList<>();
            arr.add(qTBusinessInterface.QIId.in(QIId.toString()));
            List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
            this.delete(QIId.toString());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "已删除!",new RedisDto(redisKeyDtoList).toString());
        }else{
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "QIId不能为空！");
        }
    }


    @ApiOperation(value = "下拉选", notes = "质检脚本下拉选")
    @GetMapping(value = "/getAll")
    public ResultDto getAll(){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        List<TQI> list = sqlQueryFactory
                .select(Projections.bean(TQI.class,qi.QIId,qi.QIName,qi.QIScript,qi.createdBy,qi.createdTime,qi.updatedTime,qi.updatedBy))
                .from(qi)
                .orderBy(qi.createdTime.desc())
                .fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取下拉选成功！",list);
    }


}
