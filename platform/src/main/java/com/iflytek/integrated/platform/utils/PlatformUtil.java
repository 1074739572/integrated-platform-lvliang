package com.iflytek.integrated.platform.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.integrated.common.dto.HttpResult;
import com.iflytek.integrated.common.utils.HttpClientUtil;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.ParamsDto;
import com.predic8.schema.Element;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;

import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.xml.MarkupBuilder;
import groovy.xml.QName;
import groovy.xml.SAXBuilder;
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
	
//	public static void main(String[] args) {
//		String req = "{\"format\":\"<Request>\\r\\n    <HosCode>1001_1</HosCode>\\r\\n    <VisitNo>291123_1</VisitNo>\\r\\n    <PatientId>291123_1</PatientId>\\r\\n    <VisitTimes>1_1</VisitTimes>\\r\\n</Request>\",\"funcode\":\"getChatRecorderList\",\"productcode\":\"kwjzxyyqqfxt\",\"orgids\":[\"H61020200076\"],\"inParams\":null,\"wsInParams\":\"<Request>\\r\\n    <HosCode>1001_1</HosCode>\\r\\n    <VisitNo>291123_1</VisitNo>\\r\\n    <PatientId>291123_1</PatientId>\\r\\n    <VisitTimes>1_1</VisitTimes>\\r\\n</Request>\",\"wsdlUrl\":\"http://10.64.211.118:9071/services/kwjzxyyqqfxt/H61020200076\",\"wsOperationNames\":[\"zlWS_HL7|zlHisSoapSoap11Binding|zlHisSoapHttpSoap11Endpoint\",\"NetTest|zlHisSoapSoap11Binding|zlHisSoapHttpSoap11Endpoint\",\"EncryptTest|zlHisSoapSoap11Binding|zlHisSoapHttpSoap11Endpoint\"],\"sysIntfParamFormatType\":\"2\",\"authFlag\":\"0\",\"wsOperationName\":\"zlWS_HL7|zlHisSoapSoap11Binding|zlHisSoapHttpSoap11Endpoint\"}";
//		String wsdlUrl = "http://10.64.211.118:9071/services/kwjzxyyqqfxt/H61020200076";
//		ObjectMapper objectMapper = new ObjectMapper();
//		Map<String , Object> paramsMap = null;
//		String wsdlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//				+ "<wsdl:definitions targetNamespace=\"http://tempuri.org/\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:tns=\"http://tempuri.org/\" xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\">\n"
//				+ "<wsdl:documentation>bbbddd</wsdl:documentation>\n"
//				+ "  <wsdl:types>\n"
//				+ "    <s:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://tempuri.org/\" xmlns:s=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:tm=\"http://microsoft.com/wsdl/mime/textMatching/\">\n"
//				+ "    <s:element name=\"NetTest\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"xml\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"NetTestResponse\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"NetTestResult\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"EncryptTest\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"xml\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"EncryptTestResponse\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"EncryptTestResult\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlBaseWS_HL7\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"InputXml\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlBaseWS_HL7Response\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"zlBaseWS_HL7Result\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlWS_HL7\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"InputXml\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlWS_HL7Response\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"zlWS_HL7Result\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlCdrWs_Xml\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"InputXml\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlCdrWs_XmlResponse\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"zlCdrWs_XmlResult\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlhis_intfc\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"Json_Head\" type=\"xs:string\"/>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"Json_Body\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"zlhis_intfcResponse\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element minOccurs=\"0\" name=\"zlhis_intfcResult\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"string\" nillable=\"true\" type=\"xs:string\"/>\n"
//				+ "    <s:element name=\"zlWS_HL7Fault\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element name=\"payload\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"NetTestFault\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element name=\"payload\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "    <s:element name=\"EncryptTestFault\">\n"
//				+ "        <s:complexType>\n"
//				+ "            <s:sequence>\n"
//				+ "                <s:element name=\"payload\" type=\"xs:string\"/>\n"
//				+ "            </s:sequence>\n"
//				+ "        </s:complexType>\n"
//				+ "    </s:element>\n"
//				+ "</s:schema>\n"
//				+ "  </wsdl:types>\n"
//				+ "  <wsdl:message name=\"zlWS_HL7Response\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:zlWS_HL7Response\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"zlWS_HL7Fault\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:zlWS_HL7Fault\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"NetTestResponse\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:NetTestResponse\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"NetTestFault\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:NetTestFault\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"NetTestRequest\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:NetTest\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"EncryptTestResponse\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:EncryptTestResponse\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"EncryptTestFault\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:EncryptTestFault\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"zlWS_HL7Request\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:zlWS_HL7\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:message name=\"EncryptTestRequest\">\n"
//				+ "    <wsdl:part name=\"parameters\" element=\"tns:EncryptTest\">\n"
//				+ "    </wsdl:part>\n"
//				+ "  </wsdl:message>\n"
//				+ "  <wsdl:portType name=\"zlHisSoapPortType\" wsp:PolicyURIs=\"#servicePolicy\">\n"
//				+ "    <wsdl:operation name=\"zlWS_HL7\">\n"
//				+ "<wsdl:documentation>HL7明文交易接口</wsdl:documentation>\n"
//				+ "      <wsdl:input message=\"tns:zlWS_HL7Request\" wsaw:Action=\"http://tempuri.org/zlWS_HL7\">\n"
//				+ "    </wsdl:input>\n"
//				+ "      <wsdl:output message=\"tns:zlWS_HL7Response\" wsaw:Action=\"http://tempuri.org/zlHisSoapSoap/zlWS_HL7Response\">\n"
//				+ "    </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"zlWS_HL7Fault\" message=\"tns:zlWS_HL7Fault\" wsaw:Action=\"http://tempuri.org//zlHisSoapPortType/zlWS_HL7Fault\">\n"
//				+ "    </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "    <wsdl:operation name=\"NetTest\">\n"
//				+ "<wsdl:documentation>测试接口 提供给外部调用</wsdl:documentation>\n"
//				+ "      <wsdl:input message=\"tns:NetTestRequest\" wsaw:Action=\"http://tempuri.org/NetTest\">\n"
//				+ "    </wsdl:input>\n"
//				+ "      <wsdl:output message=\"tns:NetTestResponse\" wsaw:Action=\"http://tempuri.org/zlHisSoapSoap/NetTestResponse\">\n"
//				+ "    </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"NetTestFault\" message=\"tns:NetTestFault\" wsaw:Action=\"http://tempuri.org//zlHisSoapPortType/NetTestFault\">\n"
//				+ "    </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "    <wsdl:operation name=\"EncryptTest\">\n"
//				+ "<wsdl:documentation>测试加密,提供给外部调用返回加密后数据</wsdl:documentation>\n"
//				+ "      <wsdl:input message=\"tns:EncryptTestRequest\" wsaw:Action=\"http://tempuri.org/EncryptTest\">\n"
//				+ "    </wsdl:input>\n"
//				+ "      <wsdl:output message=\"tns:EncryptTestResponse\" wsaw:Action=\"http://tempuri.org/zlHisSoapSoap/EncryptTestResponse\">\n"
//				+ "    </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"EncryptTestFault\" message=\"tns:EncryptTestFault\" wsaw:Action=\"http://tempuri.org//zlHisSoapPortType/EncryptTestFault\">\n"
//				+ "    </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "  </wsdl:portType>\n"
//				+ "  <wsdl:binding name=\"zlHisSoapSoap11Binding\" type=\"tns:zlHisSoapPortType\">\n"
//				+ "    <soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n"
//				+ "    <wsaw:UsingAddressing wsdl:required=\"false\"/>\n"
//				+ "    <wsdl:operation name=\"zlWS_HL7\">\n"
//				+ "      <soap:operation soapAction=\"http://tempuri.org/zlWS_HL7\" style=\"document\"/>\n"
//				+ "      <wsdl:input>\n"
//				+ "        <soap:body use=\"literal\"/>\n"
//				+ "      </wsdl:input>\n"
//				+ "      <wsdl:output>\n"
//				+ "        <soap:body use=\"literal\"/>\n"
//				+ "      </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"zlWS_HL7Fault\">\n"
//				+ "        <soap:fault name=\"zlWS_HL7Fault\" use=\"literal\"/>\n"
//				+ "      </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "    <wsdl:operation name=\"NetTest\">\n"
//				+ "      <soap:operation soapAction=\"http://tempuri.org/NetTest\" style=\"document\"/>\n"
//				+ "      <wsdl:input>\n"
//				+ "        <soap:body use=\"literal\"/>\n"
//				+ "      </wsdl:input>\n"
//				+ "      <wsdl:output>\n"
//				+ "        <soap:body use=\"literal\"/>\n"
//				+ "      </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"NetTestFault\">\n"
//				+ "        <soap:fault name=\"NetTestFault\" use=\"literal\"/>\n"
//				+ "      </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "    <wsdl:operation name=\"EncryptTest\">\n"
//				+ "      <soap:operation soapAction=\"http://tempuri.org/EncryptTest\" style=\"document\"/>\n"
//				+ "      <wsdl:input>\n"
//				+ "        <soap:body use=\"literal\"/>\n"
//				+ "      </wsdl:input>\n"
//				+ "      <wsdl:output>\n"
//				+ "        <soap:body use=\"literal\"/>\n"
//				+ "      </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"EncryptTestFault\">\n"
//				+ "        <soap:fault name=\"EncryptTestFault\" use=\"literal\"/>\n"
//				+ "      </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "  </wsdl:binding>\n"
//				+ "  <wsdl:binding name=\"zlHisSoapSoap12Binding\" type=\"tns:zlHisSoapPortType\">\n"
//				+ "    <soap12:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n"
//				+ "    <wsaw:UsingAddressing wsdl:required=\"false\"/>\n"
//				+ "    <wsdl:operation name=\"zlWS_HL7\">\n"
//				+ "      <soap12:operation soapAction=\"http://tempuri.org/zlWS_HL7\" style=\"document\"/>\n"
//				+ "      <wsdl:input>\n"
//				+ "        <soap12:body use=\"literal\"/>\n"
//				+ "      </wsdl:input>\n"
//				+ "      <wsdl:output>\n"
//				+ "        <soap12:body use=\"literal\"/>\n"
//				+ "      </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"zlWS_HL7Fault\">\n"
//				+ "        <soap12:fault name=\"zlWS_HL7Fault\" use=\"literal\"/>\n"
//				+ "      </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "    <wsdl:operation name=\"NetTest\">\n"
//				+ "      <soap12:operation soapAction=\"http://tempuri.org/NetTest\" style=\"document\"/>\n"
//				+ "      <wsdl:input>\n"
//				+ "        <soap12:body use=\"literal\"/>\n"
//				+ "      </wsdl:input>\n"
//				+ "      <wsdl:output>\n"
//				+ "        <soap12:body use=\"literal\"/>\n"
//				+ "      </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"NetTestFault\">\n"
//				+ "        <soap12:fault name=\"NetTestFault\" use=\"literal\"/>\n"
//				+ "      </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "    <wsdl:operation name=\"EncryptTest\">\n"
//				+ "      <soap12:operation soapAction=\"http://tempuri.org/EncryptTest\" style=\"document\"/>\n"
//				+ "      <wsdl:input>\n"
//				+ "        <soap12:body use=\"literal\"/>\n"
//				+ "      </wsdl:input>\n"
//				+ "      <wsdl:output>\n"
//				+ "        <soap12:body use=\"literal\"/>\n"
//				+ "      </wsdl:output>\n"
//				+ "      <wsdl:fault name=\"EncryptTestFault\">\n"
//				+ "        <soap12:fault name=\"EncryptTestFault\" use=\"literal\"/>\n"
//				+ "      </wsdl:fault>\n"
//				+ "    </wsdl:operation>\n"
//				+ "  </wsdl:binding>\n"
//				+ "  <wsdl:service name=\"zlHisSoap\">\n"
//				+ "    <wsdl:port name=\"zlHisSoapHttpSoap11Endpoint\" binding=\"tns:zlHisSoapSoap11Binding\">\n"
//				+ "      <soap:address location=\"http://172.20.7.12:8080${http.request.uri}\"/>\n"
//				+ "    </wsdl:port>\n"
//				+ "  </wsdl:service>\n"
//				+ "    <wsp:Policy wsu:Id=\"servicePolicy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsp:ExactlyOne><wsp:All><wsam:Addressing wsp:Optional=\"true\" xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\"><wsp:Policy><wsam:AnonymousResponses/></wsp:Policy></wsam:Addressing></wsp:All></wsp:ExactlyOne></wsp:Policy>\n"
//				+ "</wsdl:definitions>";
//		try {
//			paramsMap = objectMapper.readValue(req, new TypeReference<Map<String, Object>>() {
//			});
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		String wsdlContent2 = "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:tns=\"http://service.nurseinterface.iflytek.com/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" xmlns:ns1=\"http://schemas.xmlsoap.org/soap/http\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" targetNamespace=\"http://service.nurseinterface.iflytek.com/\">\n"
//				+ "   <wsdl:types>\n"
//				+ "      <xsd:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" attributeFormDefault=\"unqualified\" elementFormDefault=\"unqualified\" targetNamespace=\"http://service.nurseinterface.iflytek.com/\" version=\"1.0\">\n"
//				+ "         <xsd:element name=\"postData\" type=\"tns:postData\"/>\n"
//				+ "         <xsd:element name=\"postDataResponse\" type=\"tns:postDataResponse\"/>\n"
//				+ "         <xsd:complexType name=\"postData\">\n"
//				+ "            <xsd:sequence>\n"
//				+ "               <xsd:element minOccurs=\"0\" name=\"action\" type=\"xsd:string\"/>\n"
//				+ "               <xsd:element minOccurs=\"0\" name=\"message\" type=\"xsd:string\"/>\n"
//				+ "            </xsd:sequence>\n"
//				+ "         </xsd:complexType>\n"
//				+ "         <xsd:complexType name=\"postDataResponse\">\n"
//				+ "            <xsd:sequence>\n"
//				+ "               <xsd:element minOccurs=\"0\" name=\"return\" type=\"xsd:string\"/>\n"
//				+ "            </xsd:sequence>\n"
//				+ "         </xsd:complexType>\n"
//				+ "      </xsd:schema>\n"
//				+ "   </wsdl:types>\n"
//				+ "   <wsdl:message name=\"postData\">\n"
//				+ "      <wsdl:part name=\"parameters\" element=\"tns:postData\"/>\n"
//				+ "   </wsdl:message>\n"
//				+ "   <wsdl:message name=\"postDataResponse\">\n"
//				+ "      <wsdl:part name=\"parameters\" element=\"tns:postDataResponse\"/>\n"
//				+ "   </wsdl:message>\n"
//				+ "   <wsdl:portType name=\"ps_test_proxyPortType\">\n"
//				+ "      <wsdl:operation name=\"postData\">\n"
//				+ "         <wsdl:input message=\"tns:postData\" wsaw:Action=\"postData\"/>\n"
//				+ "         <wsdl:output message=\"tns:postDataResponse\" wsaw:Action=\"http://service.nurseinterface.iflytek.com/NurseServiceImpl/postDataResponse\"/>\n"
//				+ "      </wsdl:operation>\n"
//				+ "   </wsdl:portType>\n"
//				+ "   <wsdl:binding name=\"ps_test_proxySoap11Binding\" type=\"tns:ps_test_proxyPortType\">\n"
//				+ "      <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n"
//				+ "      <wsdl:operation name=\"postData\">\n"
//				+ "         <soap:operation soapAction=\"postData\" style=\"document\"/>\n"
//				+ "         <wsdl:input>\n"
//				+ "            <soap:body use=\"literal\"/>\n"
//				+ "         </wsdl:input>\n"
//				+ "         <wsdl:output>\n"
//				+ "            <soap:body use=\"literal\"/>\n"
//				+ "         </wsdl:output>\n"
//				+ "      </wsdl:operation>\n"
//				+ "   </wsdl:binding>\n"
//				+ "   <wsdl:binding name=\"ps_test_proxySoap12Binding\" type=\"tns:ps_test_proxyPortType\">\n"
//				+ "      <soap12:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n"
//				+ "      <wsdl:operation name=\"postData\">\n"
//				+ "         <soap12:operation soapAction=\"postData\" style=\"document\"/>\n"
//				+ "         <wsdl:input>\n"
//				+ "            <soap12:body use=\"literal\"/>\n"
//				+ "         </wsdl:input>\n"
//				+ "         <wsdl:output>\n"
//				+ "            <soap12:body use=\"literal\"/>\n"
//				+ "         </wsdl:output>\n"
//				+ "      </wsdl:operation>\n"
//				+ "   </wsdl:binding>\n"
//				+ "   <wsdl:binding name=\"ps_test_proxyHttpBinding\" type=\"tns:ps_test_proxyPortType\">\n"
//				+ "      <http:binding verb=\"POST\"/>\n"
//				+ "      <wsdl:operation name=\"postData\">\n"
//				+ "         <http:operation location=\"postData\"/>\n"
//				+ "         <wsdl:input>\n"
//				+ "            <mime:content type=\"text/xml\" part=\"parameters\"/>\n"
//				+ "         </wsdl:input>\n"
//				+ "         <wsdl:output>\n"
//				+ "            <mime:content type=\"text/xml\" part=\"parameters\"/>\n"
//				+ "         </wsdl:output>\n"
//				+ "      </wsdl:operation>\n"
//				+ "   </wsdl:binding>\n"
//				+ "   <wsdl:service name=\"ps_test_proxy\">\n"
//				+ "      <wsdl:port name=\"ps_test_proxyHttpsSoap11Endpoint\" binding=\"tns:ps_test_proxySoap11Binding\">\n"
//				+ "         <soap:address location=\"http://172.31.184.170:9071${http.request.uri}\"/>\n"
//				+ "      </wsdl:port>\n"
//				+ "   </wsdl:service>\n"
//				+ "</wsdl:definitions>";
//		String methodName = paramsMap.get("wsOperationName").toString();
//		String funcode = paramsMap.get("funcode").toString();
//		String param = paramsMap.get("format").toString();
//		
//		WSDLParser parser = new WSDLParser();
//		Definitions wsdl = null;
//		try {
//			try (InputStream is = new ByteArrayInputStream(wsdlContent.getBytes())){
//				wsdl = parser.parse(is);
//			}catch(Exception e) {
//				log.error("https协议调用webservice接口解析wsdl文件异常" , e);
//			}
//		}catch(Exception e1) {
//			log.error("https协议获取wsdl文件内容异常" , e1);
//		}
////		for (Service service : wsdl.getServices()) {
////			for (Port port : service.getPorts()) {
////				Binding binding = port.getBinding();
////				com.predic8.wsdl.PortType portType = binding.getPortType();
////				System.out.println(portType.getName());
////				for (com.predic8.wsdl.Operation op : portType.getOperations()) {
////					List<Part> parts = op.getInput().getMessage().getParts();
////					for(Part p: parts) {
////						p.getElement().getRequestTemplate();
////						p.getElement().getAsJson();
////					}
////					String mixedOpName = op.getName() + "|" + binding.getName() + "|" + port.getName();
////					System.out.println(mixedOpName);
////				}
////			}
////		}
//		String[] mixedOpName = methodName.split("\\|");
//		if (mixedOpName.length != 3) {
//			System.out.println("传入方法名参数[" + methodName + "]不正确！");
//		}
//		String opName = mixedOpName[0];
//		String bindingName = mixedOpName[1];
//		String portName = mixedOpName[2];
//		Binding binding = wsdl.getBinding(bindingName);
//		BindingOperation bo = binding.getOperation(opName);
//		bo.getInput().getBindingElements();
//		ObjectMapper om = new ObjectMapper();
//		Operation oper = binding.getPortType().getOperation(opName);
//		List<Part> parts = oper.getInput().getMessage().getParts();
//		Map<String , Map<String , String>> paramMap = new HashMap<>();
//		for(Part p: parts) {
//			String partStr = p.getElement().getAsJson();
//			try {
//				Map<String , Map<String , String>> tmpMap = objectMapper.readValue(partStr, new TypeReference<Map<String, Map<String , String>>>() {
//				});
//				paramMap.putAll(tmpMap);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
////		wsdl.getOperation(opName, "ps_test_proxyPortType");
//		StringWriter writer = new StringWriter();
//		SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(),
//				new MarkupBuilder(writer));
//		
//		Object resp = creator.createRequest(portName, opName, bindingName);
//		writer.flush();
//		String soapTpl = writer.toString();
//		System.out.println(soapTpl.indexOf("?XXX?"));
//		System.out.println(soapTpl.lastIndexOf("?XXX?"));
//		writer.getBuffer().setLength(0);
//		try {
//			Node dom = new XmlParser().parseText(soapTpl);
//			Iterator iter = dom.iterator();
//			iter.forEachRemaining((n)->{
//				
//			});
//			dom.children().forEach(n->{
//				if(n instanceof Node) {
//					Node tmpN = (Node)n;
//					Object qname = tmpN.name();
//					if(qname instanceof QName) {
//						QName qn = (QName) qname;
//						qn.getLocalPart();
//						qn.getQualifiedName();
//					}
//					tmpN.value();
//				}
//				System.out.println(n);
//			});
//			paramMap.forEach((key , m)->{
//				Object obj = dom.get(key);
//				System.out.println(obj);
//			});
//		} catch (IOException | SAXException | ParserConfigurationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		soapTpl = soapTpl.replaceFirst("\\?XXX\\?", funcode);
//		soapTpl = soapTpl.replaceFirst("\\?XXX\\?", param);
//		String responseStr = "";
//		try {
//			responseStr = HttpClientCallSoapUtil.doPostSoap1_1(wsdlUrl, soapTpl, opName , null);
//		}catch(Exception e ) {
//			log.error("发起webservice接口调用请求异常" , e);
//		}
//	}
	
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

		int firstIdx = soapTpl.indexOf("?XXX?");
		int lastIdx = soapTpl.lastIndexOf("?XXX?");
		if(firstIdx == lastIdx) {
			soapTpl = soapTpl.replaceFirst("\\?XXX\\?", params);
		}else {
			soapTpl = soapTpl.replaceFirst("\\?XXX\\?", funCode);
			soapTpl = soapTpl.replaceFirst("\\?XXX\\?", params);
		}
		String responseStr = "";
		try {
			responseStr = HttpClientCallSoapUtil.doPostSoap1_1(wsdlUrl, soapTpl, opName , headerMap);
		}catch(Exception e ) {
			log.error("发起webservice接口调用请求异常" , e);
			return "发起webservice接口调用请求异常" + e.getLocalizedMessage();
		}
		return responseStr;
	}
	
	public static String invokeWsServiceWithOrigin(String wsdlUrl, String methodName, String params , Map<String , String> headerMap){
		String[] mixedOpName = methodName.split("\\|");
		if (mixedOpName.length != 3) {
			return "传入方法名参数[" + methodName + "]不正确！";
		}
		String opName = mixedOpName[0];
		String responseStr = "";
		try {
			responseStr = HttpClientCallSoapUtil.doPostSoap1_1(wsdlUrl, params, opName , headerMap);
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
	
	public static String escapeSqlSingleQuotes(String sql) {
		if(StringUtils.isBlank(sql)) {
			return sql;
		}
		if(sql.indexOf("'") > -1) {
			sql = sql.replaceAll("'", "''");
		}
		if(sql.indexOf("\''") > -1) {
			sql = sql.replaceAll("\\''", "\\\\\\\\'");
		}
		if(sql.indexOf("\"") > -1) {
			sql = sql.replaceAll("\\\\\"", "\\\\\\\\\\\\\\\"");
		}
		return sql;
	}
	
}
