package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.InterfaceDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceHis;
import com.iflytek.integrated.platform.entity.TSysRegistry;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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

import static com.iflytek.integrated.platform.entity.QTInterfaceHis.qTInterfaceHis;

@Slf4j
@Api(tags = "服务历史管理")
@RestController
@RequestMapping("/{version}/pt/interfaceHis")
public class InterfaceHisService extends BaseService<TInterfaceHis, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceHisService.class);

    @Autowired
    private BatchUidService batchUidService;

    @Autowired
    private InterfaceService interfaceService;

    public InterfaceHisService() {
        super(qTInterfaceHis, qTInterfaceHis.id);
    }

    @ApiOperation(value = "服务历史列表")
    @GetMapping("/getServerHisList")
    public ResultDto<TableData<TInterfaceHis>> getServerHisList(
            @ApiParam(value = "服务id") @RequestParam(value = "serverId", required = true) String serverId,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        try {
            QueryResults<TInterfaceHis> queryResults = sqlQueryFactory
                    .select(Projections
                            .bean(TInterfaceHis.class, qTInterfaceHis.interfaceName, qTInterfaceHis.updatedTime,
                                    qTInterfaceHis.id, qTInterfaceHis.originInterfaceId))
                    .from(qTInterfaceHis)
                    .where(qTInterfaceHis.originInterfaceId.eq(serverId))
                    .offset((pageNo - 1) * pageSize).orderBy(qTInterfaceHis.updatedTime.desc()).fetchResults();
            // 分页
            TableData<TInterfaceHis> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务历史成功!", tableData);
        } catch (BeansException e) {
            logger.error("获取服务历史失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务历史失败!");
        }
    }


    @ApiOperation(value = "获取服务历史详情", notes = "获取服务历史详情")
    @GetMapping("/getInterfaceInfoByHisId")
    public ResultDto<InterfaceDto> getInterfaceInfoById(
            @ApiParam(value = "服务历史id") @RequestParam(value = "id", required = true) String id) {
        TInterface ti = this.getOne(id);
        if (ti == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该服务!", null);
        }
        try {
            InterfaceDto iDto=interfaceService.handleInterface(ti);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务历史详情成功!", iDto);
        } catch (Exception e) {
            logger.error("获取服务历史详情失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务历史详情失败!");
        }
    }

    @ApiOperation(value = "回滚服务历史", notes = "回滚服务历史")
    @GetMapping("/rollback")
    public ResultDto<InterfaceDto> rollback(
            @ApiParam(value = "服务历史id") @RequestParam(value = "id", required = true) String id) {
        TInterface ti = this.getOne(id);
        if (ti == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "根据id未查出该服务!", null);
        }
        try {
            InterfaceDto iDto=interfaceService.handleInterface(ti);
            return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取服务历史详情成功!", iDto);
        } catch (Exception e) {
            logger.error("获取服务历史详情失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "获取服务历史详情失败!");
        }
    }

    /**
     * 保存服务历史
     *
     * @param interfaceId
     */
    public void saveHis(String interfaceId) {
        //查询服务
        TInterface tInterface = interfaceService.getOne(interfaceId);

        //插入历史
        TInterfaceHis his=new TInterfaceHis();

        BeanUtils.copyProperties(tInterface,his);

        //生成新的id ,并将原id记录下来
        his.setId(batchUidService.getUid(qTInterfaceHis.getTableName()) + "");
        his.setOriginInterfaceId(tInterface.getId());
        his.setUpdatedTime(new Date());

        this.post(his);

    }
}
