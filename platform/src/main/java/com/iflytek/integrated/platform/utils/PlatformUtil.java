package com.iflytek.integrated.platform.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.ParamsDto;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;

import groovy.xml.MarkupBuilder;

/**
 * @author czzhan 公用方法
 */
public class PlatformUtil {

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
					dto.setParamType("object");
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
					paramsDto.setParamType("object[]");
					paramsDto.setParamValue("");
					dtoListAddParamsDto(dtoList, paramsDto);
				} else {
					paramsDto.setParamType("List");
					paramsDto.setParamValue(list.get(i));
				}
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

	public static String invokeWsService(String wsdlUrl, String methodName, String funCode, String params) {
		params = "<![CDATA[" + params + "]]>";
		WSDLParser parser = new WSDLParser();
		Definitions wsdl = parser.parse(wsdlUrl);
		StringWriter writer = new StringWriter();
		SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(),
				new MarkupBuilder(writer));
		String[] mixedOpName = methodName.split("\\|");
		if (mixedOpName.length != 3) {
			return "";
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

		String responseStr = HttpClientCallSoapUtil.doPostSoap1_1(wsdlUrl, soapTpl, opName);
		return responseStr;
	}

	static class ArtifactOutInterceptor extends AbstractPhaseInterceptor<Message> {

		private String postUrl;

		private String methodName;

		private Map<String, String> resultMap;

		public ArtifactOutInterceptor(String postUrl, String methodName, Map<String, String> resultMap) {
			// 这儿使用pre_stream，意思为在流关闭之前
			super(Phase.PRE_STREAM);
			this.postUrl = postUrl;
			this.methodName = methodName;
			this.resultMap = resultMap;
		}

		public void handleMessage(Message message) {

			try {
				OutputStream os = message.getContent(OutputStream.class);
				CacheAndWriteOutputStream cwos = new CacheAndWriteOutputStream(os);
				message.setContent(OutputStream.class, cwos);
				cwos.registerCallback(new LoggingOutCallBack(postUrl, methodName, resultMap));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static class LoggingOutCallBack implements CachedOutputStreamCallback {

		private String postUrl;

		private String methodName;

		private Map<String, String> resultMap;

		public LoggingOutCallBack(String postUrl, String methodName, Map<String, String> resultMap) {
			this.postUrl = postUrl;
			this.methodName = methodName;
			this.resultMap = resultMap;
		}

		@Override
		public void onClose(CachedOutputStream cos) {
			try {
				if (cos != null) {
					String soapxml = IOUtils.toString(cos.getInputStream());
					System.out.println("Response XML in out Interceptor : " + soapxml);
					String responseStr = HttpClientCallSoapUtil.doPostSoap1_1(postUrl, soapxml, methodName);
					System.out.println(responseStr);
					resultMap.put("data", responseStr);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFlush(CachedOutputStream arg0) {
		}
	}

	public static void main(String[] args) throws IOException {
		String wsdlStr = "http://172.31.184.170:9071/services/v2csxtwd/ahslyycs";
		String methodName = "postData";
		WSDLParser parser = new WSDLParser();
		Definitions wsdl = parser.parse(wsdlStr);
		StringWriter writer = new StringWriter();
		SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(),
				new MarkupBuilder(writer));

		String params = "<Request>\r\n" + "	<Header>\r\n" + "		<SourceSystem>pt</SourceSystem> \r\n"
				+ "		<MessageID>test</MessageID>\r\n" + "	</Header>\r\n" + "	<Body>\r\n"
				+ "		<PatientRegistryRt>\r\n" + "			<PATPatientID>test</PATPatientID>\r\n"
				+ "			<PATName>test</PATName>\r\n" + "			<PATDob>test</PATDob>\r\n"
				+ "			<PATSexCode>test</PATSexCode>\r\n"
				+ "			<PATMaritalStatusCode>test</PATMaritalStatusCode>\r\n"
				+ "			<PATNationCode>test</PATNationCode>\r\n"
				+ "			<PATCountryCode>test</PATCountryCode>\r\n"
				+ "			<PATDeceasedDate>test</PATDeceasedDate>\r\n"
				+ "			<PATDeceasedTime>test</PATDeceasedTime>\r\n"
				+ "			<PATHealthCardID>test</PATHealthCardID>\r\n"
				+ "			<PATMotherID>test</PATMotherID>\r\n"
				+ "			<PATOccupationCode>test</PATOccupationCode>\r\n"
				+ "			<PATWorkPlaceName>test</PATWorkPlaceName>\r\n"
				+ "			<PATWorkPlaceTelNum>test</PATWorkPlaceTelNum>\r\n" + "<PATAddressList>\r\n"
				+ "<PATAddress>\r\n" + "				<PATAddressType>test</PATAddressType>\r\n"
				+ "				<PATAddressDesc>test</PATAddressDesc>\r\n"
				+ "				<PATHouseNum>test</PATHouseNum>\r\n"
				+ "				<PATVillage>test</PATVillage>\r\n"
				+ "				<PATCountryside>test</PATCountryside>\r\n"
				+ "				<PATCounty>test</PATCounty>\r\n" + "				<PATCity>test</PATCity>\r\n"
				+ "				<PATProvince>test</PATProvince>\r\n"
				+ "				<PATPostalCode>test</PATPostalCode>\r\n" + "			</PATAddress>\r\n"
				+ "			    <PATIdentity>\r\n" + "<PATIdentityNum>test</PATIdentityNum>\r\n"
				+ "<PATPhotoURL>test</PATPhotoURL>\r\n" + "				 <PATIdType>test</PATIdType>\r\n"
				+ "			    </PATIdentity>\r\n" + "</PATAddressList>\r\n" + "			<PATRelation>\r\n"
				+ "				<PATRelationCode>test</PATRelationCode>\r\n"
				+ "				<PATRelationName>test</PATRelationName>\r\n"
				+ "				<PATRelationPhone>test</PATRelationPhone>\r\n"
				+ "				<PATRelationAddress>\r\n"
				+ "				<PATRelationAddressDesc>test</PATRelationAddressDesc>\r\n"
				+ "				<PATRelationHouseNum>test</PATRelationHouseNum>\r\n"
				+ "				<PATRelationVillage>test</PATRelationVillage>\r\n"
				+ "				<PATRelationCountryside>test</PATRelationCountryside>\r\n"
				+ "				<PATRelationCounty>test</PATRelationCounty>\r\n"
				+ "				<PATRelationCity>test</PATRelationCity>\r\n"
				+ "				<PATRelationProvince>test</PATRelationProvince>\r\n"
				+ "				<PATRelationPostalCode>test</PATRelationPostalCode>\r\n" + "</PATRelationAddress>\r\n"
				+ "			</PATRelation>\r\n" + "				<PATTelephone>test</PATTelephone>\r\n"
				+ "				<PATRemarks>test</PATRemarks>\r\n"
				+ "				<UpdateUserCode>test</UpdateUserCode>\r\n"
				+ "				<UpdateDate>test</UpdateDate>\r\n" + "				<UpdateTime>test</UpdateTime>\r\n"
				+ "		</PatientRegistryRt>\r\n" + "	</Body>\r\n" + "</Request>\r\n" + "";
		List<String> paramList = new ArrayList<>();
		paramList.add("S0001");
		paramList.add(params);
		String soapTpl = "";
		boolean breakall = false;
		for (Service service : wsdl.getServices()) {
			for (Port port : service.getPorts()) {
				Binding binding = port.getBinding();
				com.predic8.wsdl.PortType portType = binding.getPortType();
				for (com.predic8.wsdl.Operation op : portType.getOperations()) {
//					System.out.println(op.getName() + " -- " + op.getInput().getName() + " -- "
//							+ op.getOutput().getMessage().getName());
					if (methodName.equals(op.getName())) {
//						System.out.println(
//								"--------------" + op.getName() + ";" + binding.getName() + ";" + port.getName());
						creator.createRequest(port.getName(), op.getName(), binding.getName());
						writer.flush();
						soapTpl = writer.toString();
						System.out.println(soapTpl);
						writer.getBuffer().setLength(0);

						for (String arg : paramList) {
							soapTpl = soapTpl.replaceFirst("\\?XXX\\?", arg);
						}
						System.out.println("========replaced soap xml:" + soapTpl);
						breakall = true;
						break;
					}
				}
				if (breakall) {
					break;
				}
			}
			if (breakall) {
				break;
			}
		}

		List<String> opsNames = getWsdlOperationNames(wsdlStr);
		System.out.println(opsNames);
	}
}
