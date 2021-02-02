package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.integrated.platform.entity.THospital;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;

/**
 * 医院管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "医院管理")
@RestController
@RequestMapping("/{version}/pt/hospitalManage")
public class HospitalService extends BaseService<THospital, String, StringPath> {
    public HospitalService(){
        super(qTHospital,qTHospital.id);
    }
    private static final Logger logger = LoggerFactory.getLogger(HospitalService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private RedisService redisService;


    @ApiOperation(value = "获取医院管理列表")
    @GetMapping("/getHospitalList")
    public ResultDto<TableData<THospital>> getHospitalListPage(@RequestParam(value = "hospitalName", required = false) String hospitalName,
                                         @RequestParam(value = "areaCode", required = false) String areaCode,
                                         @RequestParam(defaultValue = "1")Integer pageNo,
                                         @RequestParam(defaultValue = "10")Integer pageSize){
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            list.add(qTHospital.status.eq(Constant.Status.YES));

            //如果区域id不为空，关联区域表查询所属区域下的医院
            if (StringUtils.isNotBlank(areaCode)) {
                List<String> areaCodeList = Arrays.asList(areaCode.split(","));
                list.add(qTHospital.areaId.in(areaCodeList));
            }

            if(StringUtils.isNotEmpty(hospitalName)){
                list.add(qTHospital.hospitalName.like(PlatformUtil.createFuzzyText(hospitalName)));
            }
            //根据查询条件获取医院列表
            QueryResults<THospital> queryResults = sqlQueryFactory.select(
                Projections.bean(
                        THospital.class,
                        qTHospital.id,
                        qTHospital.hospitalName,
                        qTHospital.hospitalCode,
                        qTHospital.createdTime,
                        qTHospital.areaId
                    )
                ).from(qTHospital)
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTHospital.createdTime.desc())
                    .fetchResults();
            //分页
            TableData<THospital> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            List<THospital> rows = tableData.getRows();
            for (THospital th : rows) {
                List<String> areaCodes = areaService.getAreaCodes(new ArrayList<>(), th.getAreaId());
                Collections.reverse(areaCodes);
                th.setAreaCodes(areaCodes);
            }
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取医院管理列表成功", tableData);
        }catch (Exception e){
            logger.error("获取医院管理列表失败! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取医院管理列表失败");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理删除")
    @PostMapping("/delHospitalById")
    public ResultDto<String> delHospitalById(@ApiParam(value = "医院id") @RequestParam(value = "id", required = true) String id){
        //判断是否存在医院
        THospital hospital = sqlQueryFactory.select(qTHospital).from(qTHospital)
                .where(qTHospital.id.eq(id).and(qTHospital.status.eq(Constant.Status.YES))).fetchFirst();
        if(hospital == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "不存在该医院", "不存在该医院");
        }
        //删除前校验该医院是否关联厂商
        List<THospitalVendorLink> thvlList = hospitalVendorLinkService.getThvlListByHospitalId(id);
        if (CollectionUtils.isNotEmpty(thvlList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该医院已有厂商相关联,无法删除!", "该医院已有厂商相关联,无法删除!");
        }
        //redis缓存信息获取
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTHospital.id.in(id));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
        //逻辑删除
        Long lon = sqlQueryFactory.update(qTHospital).set(qTHospital.status, Constant.Status.NO)
                .where(qTHospital.id.eq(id)).execute();
        if(lon <= 0){
            throw new RuntimeException("医院管理删除失败");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "医院管理删除成功", new RedisDto(redisKeyDtoList).toString());
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "医院管理新增/编辑")
    @PostMapping("/saveAndUpdateHospital")
    public ResultDto<String> saveAndUpdateHospital(@RequestBody THospital hospital){
        if (hospital == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "传入数据不正确!", "传入数据不正确!");
        }
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(hospital);
        if (validationResult.isHasErrors()) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, validationResult.getErrorMsg(), validationResult.getErrorMsg());
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        //校验是否有重复医院
        boolean isExist = isExistence(hospital.getId(), hospital.getHospitalName(), hospital.getHospitalCode());
        if (isExist) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "医院名称或编码已存在!", "医院名称或编码已存在!");
        }
        if(StringUtils.isEmpty(hospital.getId())){
            //没有id，新增医院
            hospital.setId(batchUidService.getUid(qTHospital.getTableName())+"");
            hospital.setStatus(Constant.Status.YES);
            hospital.setCreatedTime(new Date());
            hospital.setCreatedBy(loginUserName);
            this.post(hospital);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "医院管理新增成功", null);
        }
        else {
            //存在id时，编辑医院
            hospital.setUpdatedTime(new Date());
            hospital.setUpdatedBy(loginUserName);
            Long lon = this.put(hospital.getId(),hospital);
            if(lon <= 0){
                throw new RuntimeException("医院管理编辑失败!");
            }
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "医院管理编辑成功", new RedisDto(hospital.getId()).toString());
    }


    @ApiOperation(value = "医院配置")
    @GetMapping("/getAllHospital")
    public ResultDto<List<THospital>> getAllHospital(){
        List<THospital> hospitals = sqlQueryFactory.select(
                Projections.bean(
                        THospital.class,
                        qTHospital.id,
                        qTHospital.hospitalName,
                        qTHospital.hospitalCode
                )
            ).from(qTHospital).where(qTHospital.status.eq(Constant.Status.YES)).orderBy(qTHospital.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"医院配置信息获取成功", hospitals);
    }

    /**
     * 校验是否存在重复的医院
     * @param id
     * @param hospitalName
     * @param hospitalCode
     * @return
     */
    private boolean isExistence(String id, String hospitalName, String hospitalCode) {
        //校验是否存在重复医院
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTHospital.status.eq(Constant.Status.YES));
        list.add(qTHospital.hospitalCode.eq(hospitalCode).or(qTHospital.hospitalName.eq(hospitalName)));
        if(StringUtils.isNotEmpty(id)){
            list.add(qTHospital.id.notEqualsIgnoreCase(id));
        }
        List<String> hospitals = sqlQueryFactory.select(qTHospital.id).from(qTHospital)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(hospitals)){
            return true;
        }
        return false;
    }


}
