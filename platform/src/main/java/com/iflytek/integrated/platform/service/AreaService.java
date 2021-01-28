package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.platform.dto.AreaDto;
import com.iflytek.integrated.platform.entity.TArea;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTArea.qTArea;

/**
 * @author czzhan
 */
@Slf4j
@Api(tags = "地区数据表")
@RestController
@RequestMapping("/{version}/pt/sysManage")
public class AreaService extends BaseService<TArea, String, StringPath> {
    private static final Logger logger = LoggerFactory.getLogger(AreaService.class);

    public AreaService() {
        super(qTArea, qTArea.id);
    }

    @ApiOperation(value = "省市区分层显示")
    @GetMapping("/getAreaInfo")
    public ResultDto<List<AreaDto>> getAreaInfo() {
        List<TArea> areaList = sqlQueryFactory.select(qTArea).from(qTArea).fetch();
        //省市区转换分层
        List<TArea> provinceList = new ArrayList<>();
        //区
        List<TArea> cityList = new ArrayList<>();
        Map<String,List<TArea>> cityMap = new HashMap<>();
        //县
        Map<String,List<TArea>> countyMap = new HashMap<>();
        for (TArea area : areaList){
            //省级列表
            if(Constant.Area.PROVINCE.equals(area.getAreaLevel())){
                provinceList.add(area);
            }
            //市级对象
            else if(Constant.Area.CITY.equals(area.getAreaLevel())){
                cityList.add(area);
            }
            //区级对象Map
            else if(Constant.Area.COUNTY.equals(area.getAreaLevel())){
                List<TArea> list = countyMap.containsKey(
                        area.getSuperId()) ? countyMap.get(area.getSuperId()) : new ArrayList<>();
                list.add(area);
                countyMap.put(area.getSuperId(),list);
            }
        }
        //保存区（县）
        for (TArea city : cityList){
            //取出区（县）
            if(countyMap.containsKey(city.getAreaCode())){
                city.setChildren(countyMap.get(city.getAreaCode()));
            }
            //保存市集合
            List<TArea> list = cityMap.containsKey(
                    city.getSuperId()) ? cityMap.get(city.getSuperId()) : new ArrayList<>();
            list.add(city);
            cityMap.put(city.getSuperId(),list);
        }
        //保存列表
        for (TArea province : provinceList){
            //取出市
            if(cityMap.containsKey(province.getAreaCode())){
                province.setChildren(cityMap.get(province.getAreaCode()));
            }
        }

        List<AreaDto> areaDtoList = JackSonUtils.jsonToTransferList(provinceList,AreaDto.class);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", areaDtoList);
    }


    @ApiOperation(value = "省市区分层获取")
    @GetMapping("/getAreaInfoByAreaCode")
    public ResultDto getAreaInfoByAreaCode(String areaCode) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotBlank(areaCode)) {
            list.add(qTArea.superId.eq(areaCode));
        }else {
            list.add(qTArea.areaLevel.eq(1));
        }
        List<TArea> areaList = sqlQueryFactory.select(qTArea).from(qTArea)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取成功!", areaList);
    }


}
