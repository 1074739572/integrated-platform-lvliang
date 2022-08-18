package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.TServerAlert;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Date;

import static com.iflytek.integrated.platform.entity.QTServerAlert.qtServerAlert;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;

/**
 * 服务告警管理
 *
 * @author fangkun
 * @date 2022/07/07 12:02
 */
@Slf4j
@Api(tags = "服务告警管理")
@RestController
@RequestMapping("/{version}/pt/serverAlert")
public class ServerAlertService extends BaseService<TServerAlert, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(ServerAlertService.class);

    @Autowired
    private BatchUidService batchUidService;

    public ServerAlertService() {
        super(qtServerAlert, qtServerAlert.id);
    }

    @ApiOperation(value = "获取服务告警配置")
    @GetMapping("/getServerAlert")
    public ResultDto<TServerAlert> getType() {
        TServerAlert serverAlert = sqlQueryFactory.selectFrom(qtServerAlert).fetchOne();
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", serverAlert);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增/修改告警配置", notes = "新增/修改分类告警配置")
    @PostMapping("/saveOrUpdate")
    public ResultDto<String> saveOrUpdate(@RequestBody TServerAlert dto, @RequestParam("loginUserName") String loginUserName) {
        if (dto == null) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "数据传入错误!", "数据传入错误!");
        }
        // 校验是否获取到登录用户
        if (StringUtils.isBlank(loginUserName)) {
            return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }

        if (StringUtils.isEmpty(dto.getId())) {
            // 新增服务注册信息
            dto.setId(batchUidService.getUid(qTSysRegistry.getTableName()) + "");
            dto.setCreatedTime(new Date());
            dto.setCreatedBy(loginUserName);
            dto.setUpdatedTime(new Date());
            dto.setUpdatedBy(loginUserName);
            this.post(dto);
        } else {
            dto.setUpdatedBy(loginUserName);
            dto.setUpdatedTime(new Date());
            this.put(dto.getId(), dto);
        }
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "服务告警配置保存成功!", dto.getId());
    }
}
