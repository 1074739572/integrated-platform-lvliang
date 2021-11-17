package com.iflytek.integrated.platform.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.integrated.common.dto.HttpResult;
import com.iflytek.integrated.common.utils.HttpClientUtil;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.ParamsDto;
import com.iflytek.integrated.platform.service.InterfaceService;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;

import groovy.xml.MarkupBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author czzhan 公用方法
 */
@Slf4j
public class PlatformUtil {
	
	public static final Map<String , String> headerParams = new HashMap<>();
	static {
		headerParams.put("Accept", "*/*");
	}

	/**
	 * 获取模糊查询
	 * 
	 * @param text
	 * @return
	 */
	public static String createFuzzyText(String text) {
		return String.format(Constant.Fuzzy.FUZZY_SEARCH, text.trim());
	}

	/**
	 * 获取右模糊查询
	 * 
	 * @param text
	 * @return
	 */
	public static String rightCreateFuzzyText(String text) {
		return String.format(Constant.Fuzzy.RIGHT_FUZZY_SEARCH, text.trim());
	}

	/**
	 * 校验字符串是否是json或者xml
	 * 
	 * @param mock
	 */
	public static String strIsJsonOrXml(String mock) {
		if (StringUtils.isEmpty(mock)) {
			throw new RuntimeException("不能为空！");
		}
		try {
			// 判断是否是JSONObject类型
			new JSONObject(mock);
			return Constant.ParamFormatType.JSON.getType();
		} catch (JSONException e) {
			try {
				// 判断是否是JSONArray类型
				new JSONArray(mock);
				return Constant.ParamFormatType.JSON.getType();
			} catch (JSONException f) {
				// 判断是否是xml结构
				try {
					// 如果是xml结构
					DocumentHelper.parseText(mock);
					return Constant.ParamFormatType.XML.getType();
				} catch (DocumentException i) {
					throw new RuntimeException("格式错误，非标准json或者xml格式！");
				}
			}
		}
	}

	/**
	 * 解析json获取建值数组
	 * 
	 * @param paramJson
	 * @return
	 */
	public static List<ParamsDto> jsonFormat(String paramJson) {
		List<ParamsDto> dtoList = new ArrayList<>();
		Object object;
		try {
			object = JackSonUtils.jsonToTransfer(paramJson, LinkedHashMap.class);
		} catch (IOException e) {
			object = JackSonUtils.jsonToTransferList(paramJson, LinkedHashMap.class);
		}
		format(object, dtoList, new ParamsDto());
		return dtoList;
	}

	/**
	 * 根据参数模板（json）获取key-value
	 * 
	 * @param object
	 * @param dtoList
	 */
	private static void format(Object object, List<ParamsDto> dtoList, ParamsDto paramsDto) {
		// LinkedHashMap类型
		if (object instanceof LinkedHashMap) {
			LinkedHashMap hashMap = (LinkedHashMap) object;
			for (Object key : hashMap.keySet()) {
				Object o = hashMap.get(key);
				ParamsDto dto = new ParamsDto();
				dto.setParamKey((String) key);
				if (o instanceof LinkedHashMap) {
					dto.setParamValue("");
					dto.setParamType("Object");
					dtoListAddParamsDto(dtoList , dto);
				} else {
					dto.setParamType(o != null ? o.getClass().getSimpleName() : "");
					dto.setParamValue(o);
				}
				// 继续循环，直到所有字段都取到最后一层
				format(o, dtoList, dto);
			}
		} else if (object instanceof List) {
			List list = (List) object;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) instanceof LinkedHashMap) {
					paramsDto.setParamType("Object[]");
					paramsDto.setParamValue("");
				} else {
					paramsDto.setParamType("List");
					paramsDto.setParamValue(list.get(i));
				}
				dtoListAddParamsDto(dtoList, paramsDto);
				format(list.get(i), dtoList, paramsDto);
			}
		} else {
			dtoListAddParamsDto(dtoList, paramsDto);
		}
	}

	/**
	 * 保存键值对到实体列表
	 * 
	 * @param dtoList
	 * @param paramsDto
	 */
	private static void dtoListAddParamsDto(List<ParamsDto> dtoList, ParamsDto paramsDto) {
		// 保存键值对，并去掉已经存在的key
		if (paramsDto != null && StringUtils.isNotBlank(paramsDto.getParamKey())) {
			List<String> keyList = dtoList.stream().map(ParamsDto::getParamKey).collect(Collectors.toList());
			String paramsKey = paramsDto.getParamKey();
			if (!keyList.contains(paramsKey)) {
				dtoList.add(paramsDto);
			}
		}
	}

	public static List<String> getWsdlOperationNames(String wsdlUrl) {
		List<String> results = new ArrayList<>();
		try {
			WSDLParser parser = new WSDLParser();
			Definitions wsdl = parser.parse(wsdlUrl);
			for (Service service : wsdl.getServices()) {
				for (Port port : service.getPorts()) {
					Binding binding = port.getBinding();
					com.predic8.wsdl.PortType portType = binding.getPortType();
					for (com.predic8.wsdl.Operation op : portType.getOperations()) {
						String mixedOpName = op.getName() + "|" + binding.getName() + "|" + port.getName();
						results.add(mixedOpName);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public static String invokeWsService(String wsdlUrl, String methodName, String funCode, String params , Map<String , String> headerMap){
//		params = "<![CDATA[" + params + "]]>";
		WSDLParser parser = new WSDLParser();
		if(headerMap!= null) {
			headerMap.putAll(headerParams);
		}
		Definitions wsdl = null;
//		if(wsdlUrl.startsWith("https")) {
		HttpResult result = null;
		try {
			result = HttpClientUtil.doGet(wsdlUrl, headerMap, null);
			try (InputStream is = new ByteArrayInputStream(result.getContent().getBytes())){
				wsdl = parser.parse(is);
			}catch(Exception e) {
				log.error("https协议调用webservice接口解析wsdl文件异常" , e);
				return "https调用webservice接口解析wsdl文件异常:" + e.getLocalizedMessage();
			}
		}catch(Exception e1) {
			log.error("https协议获取wsdl文件内容异常" , e1);
			return "https协议获取wsdl文件内容异常:" + e1.getLocalizedMessage();
		}
//		}else {
//			wsdl = parser.parse(wsdlUrl);
//		}
		StringWriter writer = new StringWriter();
		SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(),
				new MarkupBuilder(writer));
		String[] mixedOpName = methodName.split("\\|");
		if (mixedOpName.length != 3) {
			return "传入方法名参数[" + methodName + "]不正确！";
		}
		String opName = mixedOpName[0];
		String bindingName = mixedOpName[1];
		String portName = mixedOpName[2];
		creator.createRequest(portName, opName, bindingName);
		writer.flush();
		String soapTpl = writer.toString();
		writer.getBuffer().setLength(0);

		soapTpl = soapTpl.replaceFirst("\\?XXX\\?", funCode);
		soapTpl = soapTpl.replaceFirst("\\?XXX\\?", params);
		String responseStr = "";
		try {
			responseStr = HttpClientCallSoapUtil.doPostSoap1_1(wsdlUrl, soapTpl, opName , headerMap);
		}catch(Exception e ) {
			log.error("发起webservice接口调用请求异常" , e);
			return "发起webservice接口调用请求异常" + e.getLocalizedMessage();
		}
		return responseStr;
	}
	
	
	public static String secondsToFormat(Long seconds){
        Long hour =0L;
        Long min =0L;
        Long second =0L;
        String result ="";
        
        if (seconds>60) {   //是否大于零
            min = seconds/60;  //分钟
            second = seconds%60;  //秒
            if (min>60) {   //存在时
                hour=min/60;
                min=min%60;
            }
        }else {
        	second = seconds;
        }
        result=hour+"小时";
        result=result+min+"分";
        result=result+second+"秒";   //秒必须出现无论是否大于零
        return result;
    }
}
