package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.TFunctionAuth;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TVendor;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.iflytek.integrated.platform.entity.QTVendor.qtVendor;

@Slf4j
@Api(tags = "厂商接口")
@RestController
@RequestMapping("/{version}/pt/vendor")
public class VendorService extends BaseService<TVendor, String, StringPath> {

    public VendorService() {
        super(qtVendor, qtVendor.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(SysService.class);

    @Autowired
    RedisService redisService;

    @Autowired
    BatchUidService batchUidService;

    @Autowired
    SysService sysService;

    String uploadPath;

    @PostConstruct
    public void init() {
        ApplicationHome h = new ApplicationHome(getClass());
        String root = h.getSource()
                .getParentFile().getParentFile().toString();
        uploadPath = root + File.separator + "upload" + File.separator;
        logger.info("==>图片存储目录为：{}",uploadPath);
    }

    @ApiOperation(value = "获取列表", notes = "获取厂商列表")
    @GetMapping("/getList")
    public ResultDto getList(
            @ApiParam(value = "厂商名称") @RequestParam(value = "vendorName",required = false) String vendorName,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1")Integer pageNo,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10")Integer pageSize
    ){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(vendorName)) {
            list.add(qtVendor.vendorName.like("%"+vendorName+"%"));
        }
        QueryResults<TVendor> queryResults = sqlQueryFactory
                .select(Projections.bean(TVendor.class,qtVendor.id, qtVendor.vendorName, qtVendor.vendorCode, qtVendor.isValid,
                        qtVendor.createdBy, qtVendor.createdTime, qtVendor.updatedBy, qtVendor.updatedTime,qtVendor.logo))
                .from(qtVendor)
                .where(list.toArray(new Predicate[list.size()])).limit(pageSize)
                .limit(pageSize)
                .offset((pageNo - 1) * pageSize)
                .orderBy(qtVendor.createdTime.desc())
                .fetchResults();
        //分页
        TableData<TPlugin> tableData = new TableData(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取列表成功!", tableData);
    }


    @ApiOperation(value = "新增或修改厂商", notes = "新增或修改厂商")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/addOrMod")
    public ResultDto<String> addOrMod(@RequestBody TVendor dto){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }

        TVendor record = sqlQueryFactory.select(Projections.bean(TVendor.class,qtVendor.id, qtVendor.vendorName)).from(qtVendor).where(qtVendor.vendorName.eq(dto.getVendorName())).fetchFirst();

        String msg = "";

        dto.setIsValid("1");
        dto.setCreatedBy(loginUserName);
        dto.setCreatedTime(new Date());
        dto.setUpdatedBy(loginUserName);
        dto.setUpdatedTime(new Date());
        dto.setLogo(dto.getLogo());
        String id = dto.getId();
        if(id != null && StringUtils.isNotEmpty(id)){
            //修改
            if(record != null && !record.getId().equals(id)){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "名称已存在！");
            }
            dto.setId(id);
            this.put(id,dto);
            msg = "已修改!";
//            // redis缓存信息获取
//            ArrayList<Predicate> arr = new ArrayList<>();
//            arr.add(qtVendor.id.in(dto.getId()));
//            List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,msg, null);
        }else{
            //新增
            if(record != null){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "名称已存在！");
            }
            dto.setId(batchUidService.getUid(qtVendor.getTableName())+"");
            this.post(dto);
            msg = "已新增!";
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, msg);
        }
    }


    @ApiOperation(value = "删除", notes = "删除厂商")
    @PostMapping(value = "/del")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID",required = true)
    })
    public ResultDto del(@RequestBody Map param){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        Object id = param.get("id");
        if(id != null && StringUtils.isNotEmpty(id.toString())){
            //厂商是否被关联
            TSys tSys = sysService.getByVendorId(id.toString());
            if(tSys != null){
                return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该厂商已被关联，不可删除!");
            }

            //redis缓存信息获取
//            ArrayList<Predicate> arr = new ArrayList<>();
//            arr.add(qtVendor.id.in(id.toString()));
//            List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
            this.delete(id.toString());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "已删除!",null);
        }else{
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "Id不能为空！");
        }
    }


    @ApiOperation(value = "下拉选", notes = "厂商下拉选")
    @GetMapping(value = "/getAll")
    public ResultDto getAll(){
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!");
        }
        List<TVendor> list = sqlQueryFactory
                .select(Projections.bean(TVendor.class,qtVendor.id, qtVendor.vendorName, qtVendor.vendorCode, qtVendor.isValid,
                        qtVendor.createdBy,qtVendor.createdTime,qtVendor.updatedTime,qtVendor.updatedBy))
                .from(qtVendor)
                .orderBy(qtVendor.updatedTime.desc())
                .fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取下拉选成功！",list);
    }

}
