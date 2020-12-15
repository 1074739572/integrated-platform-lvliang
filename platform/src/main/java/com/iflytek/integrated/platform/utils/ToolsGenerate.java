package com.iflytek.integrated.platform.utils;

import com.iflytek.integrated.common.HttpResult;
import com.iflytek.integrated.common.utils.HttpClientUtil;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;

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
     * 开启一个新线程，调取对方接口
     * @param businessInterface
     */
    public void generateSchemaToInterface(TBusinessInterface businessInterface){
        if(StringUtils.isNotEmpty(businessInterface.getInParamFormat())){
            //解析入参
            String schema = generateSchema(businessInterface.getInParamFormat());
            if(StringUtils.isEmpty(schema)){
                throw new RuntimeException("入参schema获取失败");
            }
            businessInterface.setInParamSchema(schema);
        }
        if(StringUtils.isNotEmpty(businessInterface.getOutParamFormat())){
            String schema = generateSchema(businessInterface.getOutParamFormat());
            if(StringUtils.isEmpty(schema)){
                throw new RuntimeException("出参schema获取失败");
            }
            businessInterface.setOutParamSchema(schema);
        }
        Long lon = sqlQueryFactory.update(qTBusinessInterface)
                .set(qTBusinessInterface.inParamSchema,businessInterface.getInParamSchema())
                .set(qTBusinessInterface.outParamSchema,businessInterface.getOutParamSchema())
                .where(qTBusinessInterface.id.eq(businessInterface.getId())).execute();
        if(lon <= 0){
            throw new RuntimeException("保存schema失败");
        }
    }

    /**
     * 根据format生成schema
     * @param format
     * @return
     */
    private String generateSchema(String format){
        try {
            HttpResult result = HttpClientUtil.doPost(schemaUrl,format);
            return result.getContent();
        }
        catch (Exception e){
            return "";
        }
    }

    /**
     * 根据format生成jolt
     * @param format
     * @return
     */
    public String generateJolt(String format){
        try {
            HttpResult result = HttpClientUtil.doPost(schemaUrl,format);
            return result.getContent();
        }
        catch (Exception e){
            return "";
        }
    }
}
