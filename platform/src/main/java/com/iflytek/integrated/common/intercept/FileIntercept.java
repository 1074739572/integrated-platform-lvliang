package com.iflytek.integrated.common.intercept;

import com.iflytek.integrated.common.dto.UserDto;
import com.iflytek.integrated.platform.utils.JwtTokenUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;

/**
 * 用户登录拦截器
 * @author czzhan
 * @version 1.0
 * @date 2021/1/12 9:25
 */
@Component
public class FileIntercept extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(FileIntercept.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        boolean flag= true;
        // 判断是否为文件上传请求
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest =
                    (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> files =
                    multipartRequest.getFileMap();
            Iterator<String> iterator = files.keySet().iterator();
            //对多部件请求资源进行遍历
            while (iterator.hasNext()) {
                String formKey = (String) iterator.next();
                MultipartFile multipartfile =
                        multipartRequest.getFile(formKey);
                String filename=multipartfile.getOriginalFilename();
                //判断是否为限制文件类型
                if (! checkfile(filename)) {
                    throw new Exception("不支持的文件类型");
                }
            }
        }
        return flag;
    }

    /**
     * 判断是否为允许的上传文件类型,true表示允许
     */
    private boolean checkfile(String filename) {
        //设置允许上传文件类型
        String suffixList = "jpg,gif,png,ico,bmp,jpeg";
        // 获取文件后缀
        String suffix = filename.substring(filename.lastIndexOf(".")
                + 1, filename.length());
        if (suffixList.contains(suffix.trim().toLowerCase())) {
            return true;
        }
        return false;
    }
}
