package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTPlugin.qTPlugin;

/**
 * 插件管理
 * @author czzhan
 */
@Slf4j
@RestController
public class PluginService extends QuerydslService<TPlugin, String, TPlugin, StringPath, PageRequest<TPlugin>> {
    public PluginService(){
        super(qTPlugin,qTPlugin.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(PluginService.class);

    @ApiOperation(value = "选择插件下拉")
    @GetMapping("/{version}/pb/pluginManage/getDisPlugin")
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
    @GetMapping("/{version}/pb/pluginManage/getPluginList")
    public ResultDto getPluginList(String pluginName,
                           @RequestParam(defaultValue = "1")Integer pageNo,
                           @RequestParam(defaultValue = "10")Integer pageSize){
        try {
            //查询条件
            ArrayList<Predicate> list = new ArrayList<>();
            //判断条件是否为空
            if(!StringUtils.isEmpty(pluginName)){
                list.add(qTPlugin.pluginName.like(Utils.createFuzzyText(pluginName)));
            }
            //根据查询条件获取医院列表
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

}
