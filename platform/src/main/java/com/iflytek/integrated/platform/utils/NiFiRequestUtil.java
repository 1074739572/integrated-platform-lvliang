package com.iflytek.integrated.platform.utils;

import com.iflytek.integrated.common.dto.HttpResult;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.utils.HttpClientUtil;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.platform.dto.JoltDebuggerDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TInterface;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Map;

/**
 * @author czzhan
 * 调取接nifi接口
 */
@Component
public class NiFiRequestUtil {

    @Value("${param.schema.url}")
    private String schemaUrl;

    @Value("${param.jolt.url}")
    private String joltUrl;

    @Value("${param.interface.debug}")
    private String interfaceDebug;

    @Value("${param.groovy.url}")
    private String groovyUrl;

    @Value("${param.jolt.debugger}")
    private String joltDebuggerUrl;

    @Autowired
    public SQLQueryFactory sqlQueryFactory;

    /**
     * 调取NiFi接口，获取并保存schema
     * @param tInterface
     */
    public void generateSchemaToInterface(TInterface tInterface){
        if(StringUtils.isNotEmpty(tInterface.getInParamFormat())){
            //获取参数类型
            String type = Constant.ParamFormatType.getByType(tInterface.getInParamFormatType());
            if(StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)){
                throw new RuntimeException("入参参数类型无效");
            }
            //解析入参
            String schema = generateSchema(tInterface.getInParamFormat(),type);
            if(StringUtils.isEmpty(schema)){
                throw new RuntimeException("入参schema获取失败");
            }
            tInterface.setInParamSchema(schema);
        }
        if(StringUtils.isNotEmpty(tInterface.getOutParamFormat())){
            //获取参数类型
            String type = Constant.ParamFormatType.getByType(tInterface.getOutParamFormatType());
            if(StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)){
                throw new RuntimeException("出参参数类型无效");
            }
            //解析入参
            String schema = generateSchema(tInterface.getOutParamFormat(),type);
            if(StringUtils.isEmpty(schema)){
                throw new RuntimeException("出参schema获取失败");
            }
            tInterface.setOutParamSchema(schema);
        }
    }

    /**
     * 调取NiFi接口，获取并保存schema
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
     * 调取jolt调试接口
     * @param dto
     * @return
     */
    public Map joltDebugger(JoltDebuggerDto dto){
        try {
            String type = PlatformUtil.strIsJsonOrXml(dto.getOriginObj());
            String url = joltDebuggerUrl + "?contenttype=" + type;
            String param = JackSonUtils.transferToJson(dto);
            HttpResult result = HttpClientUtil.doPost(url,param);
            if(result != null && StringUtils.isNotBlank(result.getContent())){
                return JackSonUtils.jsonToTransfer(result.getContent(),Map.class);
            }
        }catch (Exception e){
            throw new RuntimeException("jolt调试接口调取失败");
        }
        return null;
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
            String schema = result.getContent();
            ResultDto resultDto = JackSonUtils.jsonToTransfer(schema,ResultDto.class);
            if(resultDto.getCode() != null && Constant.ResultCode.ERROR_CODE == resultDto.getCode()){
                return "";
            }
            return schema;
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
    public String generateJolt(String format, String content, String joltType){
        try {
            String url = MessageFormat.format(joltUrl,content,joltType);
            HttpResult result = HttpClientUtil.doPost(url,format);
            return result.getContent();
        }
        catch (Exception e){
            return "";
        }
    }

    /**
     * 校验groovy脚本格式是否正确
     * @param content
     * @return
     */
    public GroovyValidateDto groovyUrl(String content){
        try {
            HttpResult result = HttpClientUtil.doPost(groovyUrl,content);
            return JackSonUtils.jsonToTransfer(result.getContent(), GroovyValidateDto.class);
        }
        catch (Exception e){
            throw new RuntimeException("调取校验groovy接口错误");
        }
    }

    /**
     * 调试接口
     * @param format
     * @return
     */
    public String interfaceDebug(String format){
        try {
            HttpResult result = HttpClientUtil.doPost(interfaceDebug,format);
            return result.getContent();
        }
        catch (Exception e){
            throw new RuntimeException("调取校验调试接口错误");
        }
    }

}
