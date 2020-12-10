package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.ExceptionUtil;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.platform.entity.TArea;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTArea.qTArea;

/**
 * @author czzhan
 */
@Slf4j
@RestController
public class AreaService extends QuerydslService<TArea, String, TArea, StringPath, PageRequest<TArea>> {
    public AreaService(){
            super(qTArea, qTArea.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(AreaService.class);

    @GetMapping("/{version}/pb/test/{Id}")
    public ResultDto test(HttpServletRequest request,@PathVariable("Id") String Id) {
        try {
            System.out.println("======test");
            // 只查部分字段
            List<TArea> result = sqlQueryFactory.select(ignore(qTArea.id)).from(qTArea)
                    .where(qTArea.id.equalsIgnoreCase(Id)).fetch();
            // 类型转换
            return new ResultDto(Boolean.TRUE, "111", result );
        } catch (Exception e) {
            logger.error("根据用户id查询当前所属应用集合失败!", e);
            return new ResultDto(Boolean.FALSE, ExceptionUtil.dealException(e), null);
        }
    }
}
