package com.iflytek.integrated.common.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 限制Request请求参数
 * @author czzhan
 * @version 1.0
 * @date 2021/1/28 9:02
 */
public class RequestLimitFilter implements Filter {

    private static String PAGE_NO = "pageNo";

    private static String PAGE_SIZE = "pageSize";

    private static Integer PAGE_NUMBER_MIN = 1;

    private static Integer PAGE_NUMBER_MAX = 100;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //获取入参
        RequestLimitWrapper requestLimitWrapper = new RequestLimitWrapper((HttpServletRequest) servletRequest);
        Map<String, String[]> parameterMap = new HashMap<>(requestLimitWrapper.getParameterMap());
        //如果什么都没有，直接通过
        if(!parameterMap.containsKey(PAGE_SIZE) && !parameterMap.containsKey(PAGE_NO)){
        }
        else {
            //req业务
            reqFilter(parameterMap,requestLimitWrapper);
        }
        filterChain.doFilter(requestLimitWrapper, servletResponse);
    }

    private void reqFilter(Map<String, String[]> parameterMap,RequestLimitWrapper requestLimitWrapper){

        //如果存在pageSize，限制request中pageSize的大小范围
        if(StringUtils.isNotBlank(parameterMap.get(PAGE_SIZE)[0]) && NumberUtils.isNumber(parameterMap.get(PAGE_SIZE)[0])){
            //处理pageSize的值
            Integer pageSize = Integer.parseInt(parameterMap.get(PAGE_SIZE)[0]);
            //限制pageSize值的大小在 0~PAGE_SIZE_MAX 之间
            pageSize = pageSize < PAGE_NUMBER_MIN ? PAGE_NUMBER_MIN : pageSize > PAGE_NUMBER_MAX ? PAGE_NUMBER_MAX : pageSize;
            parameterMap.put(PAGE_SIZE, new String[]{pageSize.toString()});
        }

        //如果存在pageNo，限制最小值大于0
        if(StringUtils.isNotBlank(parameterMap.get(PAGE_NO)[0]) && NumberUtils.isNumber(parameterMap.get(PAGE_NO)[0])){
            Integer pageNo = Integer.parseInt(parameterMap.get(PAGE_NO)[0]);
            pageNo = pageNo < PAGE_NUMBER_MIN ? PAGE_NUMBER_MIN : pageNo;
            parameterMap.put(PAGE_NO, new String[]{pageNo.toString()});
        }

        //替换request中的原值
        requestLimitWrapper.setParameterMap(parameterMap);
    }
}
