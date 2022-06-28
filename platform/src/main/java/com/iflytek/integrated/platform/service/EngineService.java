package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.TEngine;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iflytek.integrated.platform.entity.QTEngine.qtEngine;

/**
 * @author fangkun
 */
@Slf4j
@Api(tags = "集成引擎")
@RestController
@RequestMapping("/{version}/pt/engineManage")
public class EngineService extends BaseService<TEngine, String, StringPath> {
    private static final Logger logger = LoggerFactory.getLogger(EngineService.class);

    @Autowired
    private BatchUidService batchUidService;

    public EngineService() {
        super(qtEngine, qtEngine.id);
    }

    @ApiOperation(value = "系统服务列表")
    @GetMapping("/getEngineList")
    public ResultDto<List<TEngine>> getEngineList(@ApiParam(value = "引擎id") @RequestParam(value = "engineId", required = false) String engineId, @ApiParam(value = "引擎名称") @RequestParam(value = "engineName", required = false) String engineName) {
        // 查询条件
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(engineId)) {
            list.add(qtEngine.id.eq(engineId));
        }
        if (StringUtils.isNotEmpty(engineName)) {
            list.add(qtEngine.engineName.like(PlatformUtil.createFuzzyText(engineName)));
        }
        List<TEngine> queryResults = sqlQueryFactory.select(Projections.bean(TEngine.class, qtEngine.id, qtEngine.engineName,
                qtEngine.isEtl, qtEngine.engineUrl,qtEngine.engineUser,
                qtEngine.enginePwd, qtEngine.createdTime)).from(qtEngine).where(list.toArray(new Predicate[list.size()])).orderBy(qtEngine.createdTime.desc()).fetch();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "引擎列表获取成功!", queryResults);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/修改引擎信息", notes = "新增/修改引擎信息")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TEngine entity) {
        if (entity == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入有误!", "数据传入有误!");
        }
        // 校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String engineId = entity.getId();
        //校验 校验“引擎名称”是存在
        if (!checkNameExist(engineId, entity.getEngineName())) {
            //查询系统名称和类型
            throw new RuntimeException(entity.getEngineName() + "引擎名称已存在");
        }

        // 新增系统配置信息
        if (StringUtils.isBlank(engineId)) {
            engineId = batchUidService.getUid(qtEngine.getTableName()) + "";
            entity.setId(engineId);
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(loginUserName);
            this.post(entity);
        } else {
            entity.setUpdatedTime(new Date());
            entity.setUpdatedBy(loginUserName);
            long l = this.put(engineId, entity);
            if (l < 1) {
                throw new RuntimeException("引擎信息编辑失败!");
            }
        }
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", engineId);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "保存引擎信息成功!", JSON.toJSONString(data));
    }

    /**
     * 校验新增或者修改是否重复
     *
     * @param id
     * @param engineName
     * @return
     */
    private Boolean checkNameExist(String id, String engineName) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(id)) {
            list.add(qtEngine.id.notEqualsIgnoreCase(id));
        }
        list.add(qtEngine.engineName.eq(engineName));

        List<TEngine> srList = sqlQueryFactory.select(Projections.bean(TEngine.class, qtEngine.id)).from(qtEngine).where(list.toArray(new Predicate[list.size()])).fetch();
        if (!CollectionUtils.isEmpty(srList)) {
            return false;
        }
        return true;
    }

    @ApiOperation(value = "引擎信息删除", notes = "引擎信息删除")
    @GetMapping("/delById/{id}")
    public ResultDto<String> delById(@ApiParam(value = "引擎id") @PathVariable(value = "id", required = true) String id) {
        // 删除接口
        long l = this.delete(id);
        if (l < 1) {
            throw new RuntimeException("删除成功!");
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "删除成功!");
    }

    @ApiOperation(value = "获取集成引擎", notes = "获取集成引擎")
    @GetMapping("/getEngine/{id}")
    public ResultDto<TEngine> getEngine(@ApiParam(value = "集成引擎id") @PathVariable(value = "id", required = false) String id) {
        try {
            TEngine tEngine = this.getOne(id);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取集成引擎信息成功!", tEngine);
        } catch (BeansException e) {
            logger.error("获取集成引擎信息失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取集成引擎信息失败!");
        }
    }
}
