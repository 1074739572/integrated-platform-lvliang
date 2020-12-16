package com.iflytek.integrated.platform.utils;

import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.HttpResult;
import com.iflytek.integrated.common.utils.HttpClientUtil;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

/**
 * @author czzhan
 * 调取接口生成schema，jolt
 */
@Component
@PropertySource(value = {"classpath:config.properties"}, encoding = "utf-8")
public class ToolsGenerate {

    @Value("${param.schema.url}")
    private String schemaUrl;

    @Value("${param.jolt.url}")
    private String joltUrl;

    @Autowired
    public SQLQueryFactory sqlQueryFactory;

    /**
     * 调取对方接口，获取并保存schema
     * @param businessInterface
     */
    public void generateSchemaToInterface(TBusinessInterface businessInterface){
        if(StringUtils.isNotEmpty(businessInterface.getInParamFormat())){
            //获取参数类型
            String type = Constant.ParamFormatType.getByType(businessInterface.getInParamFormatType());
            if(StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)){
                throw new RuntimeException("入参参数类型无效");
            }
            //解析入参
            String schema = generateSchema(businessInterface.getInParamFormat(),type);
            if(StringUtils.isEmpty(schema)){
                throw new RuntimeException("入参schema获取失败");
            }
            businessInterface.setInParamSchema(schema);
        }
        if(StringUtils.isNotEmpty(businessInterface.getOutParamFormat())){
            //获取参数类型
            String type = Constant.ParamFormatType.getByType(businessInterface.getOutParamFormatType());
            if(StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)){
                throw new RuntimeException("出参参数类型无效");
            }
            //解析入参
            String schema = generateSchema(businessInterface.getOutParamFormat(),type);
            if(StringUtils.isEmpty(schema)){
                throw new RuntimeException("出参schema获取失败");
            }
            businessInterface.setOutParamSchema(schema);
        }
    }

    /**
     * 根据format生成schema
     * @param format
     * @param content
     * @return
     */
    private String generateSchema(String format, String content){
        try {
            String url = MessageFormat.format(schemaUrl,content);
            HttpResult result = HttpClientUtil.doPost(url,format);
            return result.getContent();
        }
        catch (Exception e){
            return "";
        }
    }

    /**
     * 根据format生成jolt
     * @param format
     * @param content
     * @return
     */
    public String generateJolt(String format, String content){
        String contentType = Constant.ParamFormatType.getByType(content);
        if(StringUtils.isBlank(contentType) || Constant.ParamFormatType.NONE.getType().equals(contentType)){
            throw new RuntimeException("参数类型无效");
        }
        try {
            String url = MessageFormat.format(joltUrl,contentType);
            HttpResult result = HttpClientUtil.doPost(url,format);
            return result.getContent();
        }
        catch (Exception e){
            return "";
        }
    }
}
