package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.validator.ValidationResult;
import com.iflytek.integrated.common.validator.ValidatorHelper;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.platform.dto.PluginDto;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTPlugin.qTPlugin;
import static com.iflytek.integrated.platform.entity.QTType.qTType;

/**
 * 插件管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "插件管理")
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


    @ApiOperation(value = "接口配置选择插件下拉")
    @GetMapping("/getDisPlugin")
    public ResultDto<List<PluginDto>> getDisPlugin() {
        //获取插件类型list
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
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"选择插件下拉数据获取成功", rtnArr);
    }


    @ApiOperation(value = "插件管理列表")
    @GetMapping("/getPluginList")
    public ResultDto<TableData<TPlugin>> getPluginList(@ApiParam(value = "插件名称") @RequestParam(value = "pluginName", required = false) String pluginName,
                                   @ApiParam(value = "插件分类id") @RequestParam(value = "typeId", required = false) String typeId,
                                   @ApiParam(value = "页码") @RequestParam(defaultValue = "1")Integer pageNo,
                                   @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10")Integer pageSize){
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            //判断条件是否为空
            if(StringUtils.isNotEmpty(pluginName)) {
                list.add(qTPlugin.pluginName.like(PlatformUtil.createFuzzyText(pluginName)));
            }
            if(StringUtils.isNotEmpty(typeId)) {
                list.add(qTPlugin.typeId.eq(typeId));
            }
            //根据查询条件获取插件列表
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
                            qTType.typeName.as("pluginTypeName")
                    ))
                    .from(qTPlugin)
                    .leftJoin(qTType).on(qTType.id.eq(qTPlugin.typeId))
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTPlugin.createdTime.desc())
                    .fetchResults();
            //分页
            TableData<TPlugin> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取插件管理列表成功", tableData);
        }catch (Exception e){
            logger.error("获取插件管理列表失败! MSG:{}", ExceptionUtil.dealException(e));
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取插件管理列表失败");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "插件管理删除")
    @PostMapping("/delPluginById")
    public ResultDto<String> delPluginById(@ApiParam(value = "插件id") @RequestParam(value = "id", required = true) String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "id不能为空", "id不能为空");
        }
        //查看插件是否存在
        TPlugin plugin = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.id.eq(id)).fetchFirst();
        if(plugin == null || StringUtils.isEmpty(plugin.getId())){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有找到该插件,删除失败!");
        }
        //删除插件前校验是否有接口配置相关联
        List<TBusinessInterface> tbiList = businessInterfaceService.getListByPluginId(id);
        if (CollectionUtils.isNotEmpty(tbiList)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "该插件有接口配置相关联,无法删除!");
        }
        //redis缓存信息获取
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.pluginId.in(id));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);
        //删除插件
        Long lon = sqlQueryFactory.delete(qTPlugin).where(qTPlugin.id.eq(plugin.getId())).execute();
        if(lon <= 0){
            throw new RuntimeException("插件删除失败!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "插件删除成功", new RedisDto(redisKeyDtoList).toString());
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "插件新增/编辑")
    @PostMapping("/saveAndUpdatePlugin")
    public ResultDto<String> saveAndUpdatePlugin(@RequestBody TPlugin plugin){
        if (plugin == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(plugin);
        if (validationResult.isHasErrors()) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "参数校验不通过", validationResult.getErrorMsg());
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        //校验是否存在重复插件
        Map<String, Object> isExist = this.isExistence(plugin.getId(),plugin.getPluginName(),plugin.getPluginContent());
        boolean bool = (boolean) isExist.get("isExist");
        if (bool) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, isExist.get("message")+"");
        }
        if(StringUtils.isEmpty(plugin.getId())){
            //新增插件
            plugin.setId(batchUidService.getUid(qTPlugin.getTableName())+"");
            plugin.setPluginCode(generateCode(qTPlugin.pluginCode,qTPlugin,plugin.getPluginName()));
            plugin.setCreatedTime(new Date());
            plugin.setCreatedBy(loginUserName);
            this.post(plugin);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"插件新增成功", null);
        }
        //编辑插件
        plugin.setUpdatedTime(new Date());
        plugin.setUpdatedBy(loginUserName);
        long lon = this.put(plugin.getId(), plugin);
        if(lon <= 0){
            throw new RuntimeException("插件编辑失败!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE,"插件编辑成功!", new RedisDto(plugin.getId()).toString());
    }

    /**
     * 校验是否有重复插件
     * @param id
     * @param pluginName
     * @param pluginContent
     */
    private Map<String, Object> isExistence(String id, String pluginName, String pluginContent){
        Map<String, Object> rtnMap = new HashMap<>();
        //默认false
        rtnMap.put("isExist", false);

        //校验是否存在重复插件
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTPlugin.pluginName.eq(pluginName));
        if(StringUtils.isNotEmpty(id)){
            list.add(qTPlugin.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTPlugin.id).from(qTPlugin)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(plugins)){
            rtnMap.put("isExist", true);
            rtnMap.put("message", "插件名称已存在!");
        }
        GroovyValidateDto result = niFiRequestUtil.groovyUrl(pluginContent);
        if(StringUtils.isNotBlank(result.getError()) || StringUtils.isBlank(result.getValidResult())){
            rtnMap.put("isExist", true);
            rtnMap.put("message", "插件内容格式错误!");
        }
        return rtnMap;
    }


}
