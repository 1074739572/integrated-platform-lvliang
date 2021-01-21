package com.iflytek.integrated.common.intercept;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.querydsl.sql.SQLQueryFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.TProject;
import com.iflytek.integrated.platform.service.ProjectService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/20 16:28
 */
@Data
@Component
public class AfterCompletionIntercept extends HandlerInterceptorAdapter {

    private String key;
    @Autowired
    protected SQLQueryFactory sqlQueryFactory;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 业务处理器请求处理完成之后执行方法
     * @param request
     * @param response
     * @param handler
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //获取返回结果
        ResultDto result = (ResultDto) request.getAttribute("response");
        if(result != null) {
            if(Constant.ResultCode.SUCCESS_CODE == result.getCode() && result.getData() != null){
                String ids = result.getData().toString();
                if(StringUtils.isNotBlank(ids)) {
                    List<String> conditionList = Arrays.asList(ids.split(","));

                    ArrayList<Predicate> arr = new ArrayList<>();

                    if (Constant.RedisMapName.HOSPITAL.equals(key)) {
                        arr.add(qTHospital.id.in(conditionList));
                        delKey(arr);
                    }
                    if (Constant.RedisMapName.INTERFACE.equals(key)) {
                        arr.add(qTInterface.id.in(conditionList));
                        delKey(arr);
                    }
                    if (Constant.RedisMapName.PLATFORM.equals(key)) {
//                        arr.add();
                        delKey(arr);
                    }
                    if (Constant.RedisMapName.PLUGIN.equals(key)) {
//                        arr.add();
                        delKey(arr);
                    }
                    if (Constant.RedisMapName.PRODUCT.equals(key)) {
                        arr.add(qTProduct.id.in(conditionList));
                    }
                    if (Constant.RedisMapName.PROJECT.equals(key)) {
//                        arr.add();
                        delKey(arr);
                    }
                    if (Constant.RedisMapName.VENDOR.equals(key)) {
//                        arr.add();
                        delKey(arr);
                    }

                }
            }
        }
    }

    /**
     * 删除key操作
     * @param arr
     */
    private void delKey(ArrayList<Predicate> arr) {

        arr.add(qTProject.projectCode.isNotNull().and(qTProduct.productCode.isNotNull().and(qTInterface.interfaceUrl.isNotNull())));

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
                        .where(arr.toArray(new Predicate[arr.size()]))
                        .groupBy(qTProject.projectCode, qTHospital.hospitalCode, qTProduct.productCode, qTInterface.interfaceUrl)
                        .fetch();

                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
                        String key = "";
                        for (RedisKeyDto obj : list) {
//                            key = obj.getProjectCode()+"_"+obj.getOrgId()+"_"+obj.getProductCode()+"_"+obj.getFunCode();
//                        Boolean isDel = redisUtil.hmSet("IntegratedPlatform:Configs:", "gmcdcsxm1_smyy_jmxdcscp_getPatInfo", "222");
//                            Boolean isDel = redisUtil.hmDel("IntegratedPlatform:Configs:", "gmcdcsxm1_smyy_jmxdcscp_getPatInfo");
////                            if (!isDel) {
////                                rtnList.add(key);
////                            }
                        }
                    }

    }





}
