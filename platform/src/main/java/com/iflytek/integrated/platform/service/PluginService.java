package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.validator.ValidationResult;
import com.iflytek.integrated.platform.validator.ValidatorHelper;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTPlugin.qTPlugin;

/**
 * 插件管理
 * @author czzhan
 */
@Slf4j
@Api(tags = "插件管理")
@RestController
@RequestMapping("/{version}/pb/pluginManage")
public class PluginService extends QuerydslService<TPlugin, String, TPlugin, StringPath, PageRequest<TPlugin>> {
    public PluginService(){
        super(qTPlugin,qTPlugin.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(PluginService.class);

    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private ValidatorHelper validatorHelper;

    @ApiOperation(value = "选择插件下拉")
    @GetMapping("/getDisPlugin")
    public ResultDto getDisPlugin() {
        List<TPlugin> plugins = sqlQueryFactory.select(
                Projections.bean(
                        TPlugin.class,
                        qTPlugin.id,
                        qTPlugin.pluginCode,
                        qTPlugin.pluginName
                )
        ).from(qTPlugin).orderBy(qTPlugin.updatedTime.desc()).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"",plugins);
    }

    @ApiOperation(value = "插件管理列表")
    @GetMapping("/getPluginList")
    public ResultDto getPluginList(String pluginName,
                           @RequestParam(defaultValue = "1")Integer pageNo,
                           @RequestParam(defaultValue = "10")Integer pageSize){
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            //判断条件是否为空
            if(StringUtils.isNotEmpty(pluginName)){
                list.add(qTPlugin.pluginName.like(Utils.createFuzzyText(pluginName)));
            }
            //根据查询条件获取插件列表
            QueryResults<TPlugin> queryResults = sqlQueryFactory.select(
                    Projections.bean(
                            TPlugin.class,
                            qTPlugin.id,
                            qTPlugin.pluginName,
                            qTPlugin.pluginCode,
                            qTPlugin.updatedTime
                    ))
                    .from(qTPlugin)
                    .where(list.toArray(new Predicate[list.size()]))
                    .limit(pageSize)
                    .offset((pageNo - 1) * pageSize)
                    .orderBy(qTPlugin.updatedTime.desc())
                    .fetchResults();
            //分页
            TableData<TPlugin> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", tableData);
        }catch (Exception e){
            logger.error("获取插件管理列表失败!", ExceptionUtil.dealException(e));
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", ExceptionUtil.dealException(e));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "插件管理删除")
    @DeleteMapping("/delPluginById")
    public ResultDto delPluginById(String id){
        if(StringUtils.isEmpty(id)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "id不能为空");
        }
        //查看插件是否存在
        TPlugin plugin = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.id.eq(id)).fetchOne();
        if(plugin == null || StringUtils.isEmpty(plugin.getId())){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", "没有找到该插件，删除失败");
        }
        //删除插件
        Long lon = sqlQueryFactory.delete(qTPlugin).where(qTPlugin.id.eq(plugin.getId())).execute();
        if(lon <= 0){
            throw new RuntimeException("插件管理,插件删除失败");
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "", "插件管理，插件删除成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "插件新增/编辑")
    @PostMapping("/saveAndUpdatePlugin")
    public ResultDto saveAndUpdateProduct(@RequestBody TPlugin plugin){
        //校验参数是否完整
        ValidationResult validationResult = validatorHelper.validate(plugin);
        if (validationResult.isHasErrors()) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "", validationResult.getErrorMsg());
        }
        //校验是否存在重复插件
        isExistence(plugin.getId(),plugin.getPluginName(),plugin.getPluginCode());
        if(StringUtils.isEmpty(plugin.getId())){
            //新增插件
            Long lon = sqlQueryFactory.insert(qTPlugin)
                    .set(qTPlugin.id,batchUidService.getUid(qTPlugin.getTableName())+"")
                    .set(qTPlugin.pluginName,plugin.getPluginName())
                    .set(qTPlugin.pluginCode,plugin.getPluginCode())
                    .set(qTPlugin.pluginInstruction,plugin.getPluginInstruction())
                    .set(qTPlugin.pluginContent,plugin.getPluginContent())
                    .set(qTPlugin.createdBy,"")
                    .set(qTPlugin.createdTime,new Date()).execute();
            if(lon <= 0){
                throw new RuntimeException("新增插件失败");
            }
        }
        else{
            //编辑插件
            Long lon = sqlQueryFactory.update(qTPlugin)
                    .set(qTPlugin.pluginName,plugin.getPluginName())
                    .set(qTPlugin.pluginCode,plugin.getPluginCode())
                    .set(qTPlugin.pluginInstruction,plugin.getPluginInstruction())
                    .set(qTPlugin.pluginContent,plugin.getPluginContent())
                    .set(qTPlugin.updatedBy,"")
                    .set(qTPlugin.updatedTime,new Date())
                    .where(qTPlugin.id.eq(plugin.getId())).execute();
            if(lon <= 0){
                throw new RuntimeException("编辑插件失败");
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE,"","插件新增或编辑成功");
    }

    /**
     * 校验是否有重复插件
     * @param id
     * @param pluginName
     * @param pluginCode
     */
    private void isExistence(String id, String pluginName, String pluginCode){
        //校验是否存在重复插件
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTPlugin.pluginName.eq(pluginName)
                .or(qTPlugin.pluginCode.eq(pluginCode)));
        if(StringUtils.isNotEmpty(id)){
            list.add(qTPlugin.id.notEqualsIgnoreCase(id));
        }
        List<String> plugins = sqlQueryFactory.select(qTPlugin.id).from(qTPlugin)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        if(CollectionUtils.isNotEmpty(plugins)){
            throw new RuntimeException("插件名称或编码已存在");
        }
    }

}