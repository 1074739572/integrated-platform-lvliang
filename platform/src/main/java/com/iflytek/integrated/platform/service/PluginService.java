package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.CacheDeleteService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.CacheDeleteDto;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.platform.dto.PluginDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TType;
import com.iflytek.integrated.platform.utils.NiFiRequestUtil;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTPlugin.qTPlugin;
import static com.iflytek.integrated.platform.entity.QTType.qTType;

/**
 * ????????????
 * @author czzhan
 */
@Slf4j
@Api(tags = "????????????")
@RestController
@RequestMapping("/{version}/pt/pluginManage")
public class PluginService extends BaseService<TPlugin, String, StringPath> {

    public PluginService(){
        super(qTPlugin,qTPlugin.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(PluginService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;
    @Autowired
    private NiFiRequestUtil niFiRequestUtil;
    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private CacheDeleteService cacheDeleteService;


    @ApiOperation(value = "??????????????????????????????")
    @GetMapping("/getDisPlugin")
    public ResultDto<List<PluginDto>> getDisPlugin() {
        //??????????????????list
        List<TType> typeList = sqlQueryFactory.select(qTType).from(qTType).where(qTType.type.eq(Constant.TypeStatus.PLUGIN)).orderBy(qTType.createdTime.desc()).fetch();

        List<PluginDto> rtnArr = new ArrayList<>();
        for (TType tt : typeList) {
            PluginDto jsonObj = new PluginDto();
            jsonObj.setTypeId(tt.getId());
            jsonObj.setName(tt.getTypeName());
            List<TPlugin> pluginList = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.typeId.eq(tt.getId())).orderBy(qTPlugin.createdTime.desc()).fetch();
            List<TPlugin> arr = new ArrayList();
            for (TPlugin tp : pluginList) {
                tp.setPluginId(tp.getId());
                tp.setName(tp.getPluginName());
                arr.add(tp);
            }
            jsonObj.setChildren(arr);
            rtnArr.add(jsonObj);
        }

        Iterator<PluginDto> it = rtnArr.iterator();
        while (it.hasNext()){
            PluginDto dto = it.next();
            List list = dto.getChildren();
            if(list == null || list.size() == 0){
                it.remove();
            }
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"????????????????????????????????????", rtnArr);
    }


    @ApiOperation(value = "??????????????????")
    @GetMapping("/getPluginList")
    public ResultDto<TableData<TPlugin>> getPluginList(@ApiParam(value = "????????????") @RequestParam(value = "pluginName", required = false) String pluginName,
                                   @ApiParam(value = "????????????id") @RequestParam(value = "typeId", required = false) String typeId,
                                   @ApiParam(value = "??????") @RequestParam(defaultValue = "1")Integer pageNo,
                                   @ApiParam(value = "????????????") @RequestParam(defaultValue = "10")Integer pageSize , @RequestParam(value = "id", required = false) String id){
        try {
            //????????????
            ArrayList<Predicate> list = new ArrayList<>();
            //????????????????????????
            if(StringUtils.isNotEmpty(pluginName)) {
                list.add(qTPlugin.pluginName.like(PlatformUtil.createFuzzyText(pluginName)));
            }
            if(StringUtils.isNotEmpty(typeId)) {
                list.add(qTPlugin.typeId.eq(typeId));
            }
            if(StringUtils.isNotBlank(id)) {
            	list.add(qTPlugin.id.eq(id));
            }
            //????????????????????????????????????
            QueryResults<TPlugin> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                            TPlugin.class,
                            qTPlugin.id,
                            qTPlugin.pluginName,
                            qTPlugin.pluginCode,
                            qTPlugin.pluginContent,
                            qTPlugin.pluginInstruction,
                            qTPlugin.createdTime,
                            qTPlugin.typeId,
                            qTPlugin.dependentPath,
                            qTPlugin.updatedTime,
                            qTType.typeName.as("pluginTypeName")
                    ))
                    .from(qTPlugin)
                    .leftJoin(qTType).on(qTType.id.eq(qTPlugin.typeId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTPlugin.createdTime.desc())
                    .fetchResults();
            //??????
            TableData<TPlugin> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????????????????", tableData);
        }catch (Exception e){
            logger.error("??????????????????????????????! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????????????????");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "??????????????????")
    @PostMapping("/delPluginById")
    public ResultDto<String> delPluginById(@ApiParam(value = "??????id") @RequestParam(value = "id", required = true) String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "id????????????", "id????????????");
        }
        //????????????????????????
        TPlugin plugin = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.id.eq(id)).fetchFirst();
        if(plugin == null || StringUtils.isEmpty(plugin.getId())){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????,????????????!");
        }
        //???????????????????????????????????????????????????
        List<TBusinessInterface> tbiList = businessInterfaceService.getListByPluginId(id);
        if (CollectionUtils.isNotEmpty(tbiList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????????????????,????????????!");
        }
        //????????????
        cacheDelete(plugin.getId());

        //????????????
        Long lon = sqlQueryFactory.delete(qTPlugin).where(qTPlugin.id.eq(plugin.getId())).execute();
        if(lon <= 0){
            throw new RuntimeException("??????????????????!");
        }

        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "??????????????????", null);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "????????????/??????")
    @PostMapping("/saveAndUpdatePlugin")
    public ResultDto<String> saveAndUpdatePlugin(@RequestBody TPlugin plugin,@RequestParam("loginUserName") String loginUserName){
        if (plugin == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "??????????????????!", "??????????????????!");
        }
        //????????????????????????
        ValidationResult validationResult = validatorHelper.validate(plugin);
        if (validationResult.isHasErrors()) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "?????????????????????", validationResult.getErrorMsg());
        }
        //?????????????????????????????????
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "???????????????????????????!", "???????????????????????????!");
        }
        //??????????????????????????????
        Map<String, Object> isExist = this.isExistence(plugin.getId(),plugin.getPluginName(),plugin.getPluginContent());
        boolean bool = (boolean) isExist.get("isExist");
        if (bool) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, isExist.get("message")+"");
        }
        if(StringUtils.isEmpty(plugin.getId())){
            //????????????
            plugin.setId(batchUidService.getUid(qTPlugin.getTableName())+"");
            plugin.setPluginCode(generateCode(qTPlugin.pluginCode,qTPlugin,plugin.getPluginName()));
            plugin.setCreatedTime(new Date());
            plugin.setCreatedBy(loginUserName);
            this.post(plugin);
            TType tType = typeService.getOne(plugin.getTypeId());
            plugin.setPluginTypeName(tType.getTypeName());
            historyService.insertHis(plugin,3,loginUserName,null,plugin.getId(),null);
            //????????????
            cacheDelete(plugin.getId());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"??????????????????", null);
        }
        //????????????
        TPlugin old = this.getOne(plugin.getId());
        TType tType = typeService.getOne(old.getTypeId());
        old.setPluginTypeName(tType.getTypeName());
        historyService.insertHis(old,3,loginUserName,plugin.getId(),plugin.getId(),null);
        //????????????
        plugin.setUpdatedTime(new Date());
        plugin.setUpdatedBy(loginUserName);
        long lon = this.put(plugin.getId(), plugin);
        if(lon <= 0){
            throw new RuntimeException("??????????????????!");
        }
        //????????????
        cacheDelete(plugin.getId());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"??????????????????!", null);
    }

    /**
     * ???????????????????????????
     * @param id
     * @param pluginName
     * @param pluginContent
     */
    private Map<String, Object> isExistence(String id, String pluginName, String pluginContent){
        Map<String, Object> rtnMap = new HashMap<>();
        //??????false
        rtnMap.put("isExist", false);

        //??????????????????????????????
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTPlugin.pluginName.eq(pluginName));
        if(StringUtils.isNotEmpty(id)){
            list.add(qTPlugin.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTPlugin.id).from(qTPlugin)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(plugins)){
            rtnMap.put("isExist", true);
            rtnMap.put("message", "?????????????????????!");
        }
        GroovyValidateDto result = niFiRequestUtil.groovyUrl(pluginContent);
        if(StringUtils.isNotBlank(result.getError()) || StringUtils.isBlank(result.getValidResult())){
            rtnMap.put("isExist", true);
            rtnMap.put("message", "????????????????????????!");
        }
        return rtnMap;
    }

    public void cacheDelete(String id) {
        //??????????????????????????????
        List<TBusinessInterface> businessInterfaces = businessInterfaceService.getByPlugin(id);
        if(CollectionUtils.isEmpty(businessInterfaces)){
            return;
        }
        List<String> interfaceIds = businessInterfaces.stream().map(TBusinessInterface::getRequestInterfaceId).collect(Collectors.toList());
        CacheDeleteDto keyDto = new CacheDeleteDto();
        keyDto.setInterfaceIds(interfaceIds);
        //??????????????????????????????key
        keyDto.setCacheTypeList(Arrays.asList(
                Constant.CACHE_KEY_PREFIX.COMMON_TYPE
        ));

        cacheDeleteService.cacheKeyDelete(keyDto);
    }


}
