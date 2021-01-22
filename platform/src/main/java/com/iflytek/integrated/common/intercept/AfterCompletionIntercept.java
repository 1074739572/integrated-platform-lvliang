package com.iflytek.integrated.common.intercept;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.dto.ResultDto;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQueryFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTProduct.qTProduct;
import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProductInterfaceLink.qTProductInterfaceLink;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;
import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;
import static com.iflytek.integrated.platform.entity.QTVendorDriveLink.qTVendorDriveLink;

/**
 * 缓存优化，统一接口返回处理
 * @author czzhan
 * @version 1.0
 * @date 2021/1/20 16:28
 */
@Data
@Component
public class AfterCompletionIntercept extends HandlerInterceptorAdapter {

    /**
     * 附加到request请求的名称
     */
    public static String Intercept = "intercept";

    private String key;

    private ResultDto resultDto;

    @Autowired
    protected SQLQueryFactory sqlQueryFactory;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 请求执行前方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(Intercept, this);
        return super.preHandle(request, response, handler);
    }

        /**
         * 业务处理器请求处理完成之后执行方法
         * @param request
         * @param response
         * @param handler
         * @throws Exception
         */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //获取返回结果
        ResultDto result = resultDto;
        if(result != null) {
            if(Constant.ResultCode.SUCCESS_CODE == result.getCode() && result.getData() != null) {
                String ids = result.getData().toString();
                if(StringUtils.isNotBlank(ids)) {
                    List<String> conditionList = Arrays.asList(ids.split(","));
                    ArrayList<Predicate> arr = new ArrayList<>();

                    //调取枚举，处理返回结果
                    StringPath sqlId = Constant.RedisMap.idByKey(key);
                    if(sqlId != null){
                        arr.add(sqlId.in(conditionList));
                        delKey(arr);
                    }
                }
            }
        }
    }

    /**
     * 回收方法
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //如果请求中存在拼接的字段，手动去处
        if(request.getAttribute(Intercept) != null){
            request.removeAttribute(Intercept);
        }
        //如果有保存返回，清空返回
        if(resultDto != null){
            resultDto = new ResultDto();
        }
    }

        /**
         * 删除key操作
         * @param arr
         */
    private void delKey(ArrayList<Predicate> arr) {

        arr.add(qTProject.projectCode.isNotNull().and(qTProduct.productCode.isNotNull().and(qTInterface.interfaceUrl.isNotNull())));
        arr.add(qTProject.projectStatus.eq(Constant.Status.START).and(qTPlatform.platformStatus.eq(Constant.Status.START)));

        List<RedisKeyDto> list =
                sqlQueryFactory.select(Projections.bean(RedisKeyDto.class, qTProject.projectCode.as("projectCode"),
                        qTHospital.hospitalCode.as("orgId"), qTProduct.productCode.as("productCode"), qTInterface.interfaceUrl.as("funCode")))
                        .from(qTProject)
                        .leftJoin(qTPlatform).on(qTPlatform.projectId.eq(qTProject.id))
                        .leftJoin(qTVendorConfig).on(qTVendorConfig.platformId.eq(qTPlatform.id))
                        .leftJoin(qTHospitalVendorLink).on(qTHospitalVendorLink.vendorConfigId.eq(qTVendorConfig.id))
                        .leftJoin(qTHospital).on(qTHospital.id.eq(qTHospitalVendorLink.hospitalId))
                        .leftJoin(qTProjectProductLink).on(qTProjectProductLink.projectId.eq(qTProject.id))
                        .leftJoin(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(qTProjectProductLink.productFunctionLinkId))
                        .leftJoin(qTProduct).on(qTProduct.id.eq(qTProductFunctionLink.productId))
                        .leftJoin(qTProductInterfaceLink).on(qTProductInterfaceLink.productId.eq(qTProduct.id))
                        .leftJoin(qTInterface).on(qTInterface.id.eq(qTProductInterfaceLink.interfaceId))
                        .leftJoin(qTBusinessInterface).on(qTBusinessInterface.vendorConfigId.eq(qTVendorConfig.id))
                        .leftJoin(qTVendorDriveLink).on(qTVendorDriveLink.vendorId.eq(qTVendorConfig.vendorId))
                        .where(arr.toArray(new Predicate[arr.size()]))
                        .groupBy(qTProject.projectCode, qTHospital.hospitalCode, qTProduct.productCode, qTInterface.interfaceUrl)
                        .fetch();

        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
            String key = "";
            for (RedisKeyDto obj : list) {
                key = obj.getProjectCode()+"_"+obj.getOrgId()+"_"+obj.getProductCode()+"_"+obj.getFunCode();
                redisUtil.hmDel("IntegratedPlatform:Configs:", key);
            }
        }

    }





}
