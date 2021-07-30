package com.iflytek.integrated.platform.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.nifi.api.toolkit.ApiClient;
import org.apache.nifi.api.toolkit.ApiException;
import org.apache.nifi.api.toolkit.api.AccessApi;
import org.apache.nifi.api.toolkit.api.ControllerServicesApi;
import org.apache.nifi.api.toolkit.api.FlowApi;
import org.apache.nifi.api.toolkit.api.FlowfileQueuesApi;
import org.apache.nifi.api.toolkit.api.ProcessGroupsApi;
import org.apache.nifi.api.toolkit.model.ConnectionEntity;
import org.apache.nifi.api.toolkit.model.ControllerServiceEntity;
import org.apache.nifi.api.toolkit.model.ControllerServiceRunStatusEntity;
import org.apache.nifi.api.toolkit.model.ControllerServicesEntity;
import org.apache.nifi.api.toolkit.model.DropRequestEntity;
import org.apache.nifi.api.toolkit.model.FlowDTO;
import org.apache.nifi.api.toolkit.model.ProcessGroupEntity;
import org.apache.nifi.api.toolkit.model.ProcessGroupFlowEntity;
import org.apache.nifi.api.toolkit.model.RevisionDTO;
import org.apache.nifi.api.toolkit.model.ScheduleComponentsEntity;
import org.apache.nifi.api.toolkit.model.ScheduleComponentsEntity.StateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.iflytek.integrated.common.dto.HttpResult;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.utils.HttpClientUtil;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.common.utils.OAuthApiClient;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.DbUrlTestDto;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.platform.dto.JoltDebuggerDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.querydsl.sql.SQLQueryFactory;

/**
 * @author czzhan 调取接nifi接口
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

	@Value("${param.wsdl.url}")
	private String wsdlServiceUrl;

	@Value("${param.xml2json.url}")
	private String xml2jsonUrl;

	@Value("${param.db.test.url}")
	private String testDbUrl;

	@Autowired
	public SQLQueryFactory sqlQueryFactory;
	
	private static final Logger logger = LoggerFactory.getLogger(NiFiRequestUtil.class);

	/**
	 * 根据paramFormat和formatType生成schema
	 *
	 * @param paramFormat
	 * @param formatType
	 * @return
	 */
	public String generateSchemaToInterface(String paramFormat, String formatType) {
		if (StringUtils.isNotEmpty(paramFormat)) {
			// 获取参数类型
			String type = Constant.ParamFormatType.getByType(formatType);
			if (StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)) {
				throw new RuntimeException("参数类型无效");
			}
			// 解析参数
			String schema = generateSchema(paramFormat, type);
			if (StringUtils.isEmpty(schema)) {
				throw new RuntimeException("schema获取失败");
			}
			return schema;
		}
		return "";
	}

	public String xml2json(String xml) {
		if (StringUtils.isNotEmpty(xml)) {
			// 获取参数类型
			HttpResult result;
			try {
				result = HttpClientUtil.doPost(xml2jsonUrl, xml);
				return result.getContent();
			} catch (Exception e) {
				throw new RuntimeException("xml转json处理异常", e);
			}
		}
		return "";
	}

	/**
	 * 调取NiFi接口，获取并保存schema
	 *
	 * @param businessInterface
	 */
	public void generateSchemaToInterface(TBusinessInterface businessInterface) {
		if (StringUtils.isNotEmpty(businessInterface.getInParamFormat())) {
			// 获取参数类型
			String type = Constant.ParamFormatType.getByType(businessInterface.getInParamFormatType());
			if (StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)) {
				throw new RuntimeException("入参参数类型无效");
			}
			// 解析入参
			String schema = generateSchema(businessInterface.getInParamFormat(), type);
			if (StringUtils.isEmpty(schema)) {
				throw new RuntimeException("入参schema获取失败");
			}
			businessInterface.setInParamSchema(schema);
		}
		if (StringUtils.isNotEmpty(businessInterface.getOutParamFormat())) {
			// 获取参数类型
			String type = Constant.ParamFormatType.getByType(businessInterface.getOutParamFormatType());
			if (StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)) {
				throw new RuntimeException("出参参数类型无效");
			}
			// 解析入参
			String schema = generateSchema(businessInterface.getOutParamFormat(), type);
			if (StringUtils.isEmpty(schema)) {
				throw new RuntimeException("出参schema获取失败");
			}
			businessInterface.setOutParamSchema(schema);
		}
	}

	/**
	 * 调取jolt调试接口
	 *
	 * @param dto
	 * @return
	 */
	public Map joltDebugger(JoltDebuggerDto dto) {
		try {
			String type = PlatformUtil.strIsJsonOrXml(dto.getOriginObj());
			String url = joltDebuggerUrl + "?contenttype=" + type;
			String param = JackSonUtils.transferToJson(dto);
			HttpResult result = HttpClientUtil.doPost(url, param);
			if (result != null && StringUtils.isNotBlank(result.getContent())) {
				return JackSonUtils.jsonToTransfer(result.getContent(), Map.class);
			}
		} catch (Exception e) {
			throw new RuntimeException("jolt调试接口调取失败");
		}
		return null;
	}

	/**
	 * 根据format生成schema
	 *
	 * @param format
	 * @param content
	 * @return
	 */
	private String generateSchema(String format, String content) {
		try {
			String url = MessageFormat.format(schemaUrl, content);
			HttpResult result = HttpClientUtil.doPost(url, format);
			String schema = result.getContent();
			ResultDto resultDto = JackSonUtils.jsonToTransfer(schema, ResultDto.class);
			if (resultDto.getCode() != null && Constant.ResultCode.ERROR_CODE == resultDto.getCode()) {
				return "";
			}
			return schema;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 根据format生成jolt
	 *
	 * @param format
	 * @param content
	 * @return
	 */
	public String generateJolt(String format, String content, String joltType) {
		try {
			String url = MessageFormat.format(joltUrl, content, joltType);
			HttpResult result = HttpClientUtil.doPost(url, format);
			return result.getContent();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 校验groovy脚本格式是否正确
	 *
	 * @param content
	 * @return
	 */
	public GroovyValidateDto groovyUrl(String content) {
		try {
			HttpResult result = HttpClientUtil.doPost(groovyUrl, content);
			return JackSonUtils.jsonToTransfer(result.getContent(), GroovyValidateDto.class);
		} catch (Exception e) {
			throw new RuntimeException("调取校验groovy接口错误");
		}
	}

	/**
	 * 调试接口
	 *
	 * @param format
	 * @return
	 */
	public String interfaceDebug(String format) {
		try {
			HttpResult result = HttpClientUtil.doPost(interfaceDebug, format);
			return result.getContent();
		} catch (Exception e) {
			throw new RuntimeException("调取校验调试接口错误");
		}
	}

	/**
	 * 测试数据库连接
	 * @param dto
	 * @return
	 */
	public String testDbUrl(DbUrlTestDto dto) {
		try {
			String param = JackSonUtils.transferToJson(dto);
			HttpResult result = HttpClientUtil.doPost(testDbUrl, param);
			return result.getContent();
		} catch (Exception e) {
			throw new RuntimeException("调取数据库测试接口错误");
		}
	}

	public String getWsServiceUrl() {
		return wsdlServiceUrl;
	}
	
	public void deleteNifiEtlFlow(TPlatform platform , String tEtlGroupId , String parentGroupId) throws Exception {
		if (platform != null) {
			String serverUrl = platform.getEtlServerUrl();
			if(StringUtils.isNotBlank(serverUrl) && serverUrl.endsWith("/")) {
				serverUrl = serverUrl.substring(0 , serverUrl.lastIndexOf("/"));
			}
			String userName = platform.getEtlUser();
			String password = platform.getEtlPwd();
			ApiClient client = new OAuthApiClient();
			client.setBasePath(serverUrl);
			client.addDefaultHeader("Content-Type", "application/json");
			client.addDefaultHeader("Accept", "application/json");
			AccessApi api = new AccessApi(client);
			
			try {
				client.setVerifyingSsl(false);
//				if (serverUrl.startsWith("https")) {
					if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
						String token = api.createAccessToken(userName, password);
						client.setAccessToken(token);
					}
//				}
				FlowApi flowApi = new FlowApi(client);
				ScheduleComponentsEntity compEntity = new ScheduleComponentsEntity();
				compEntity.setId(tEtlGroupId);
				compEntity.setState(StateEnum.STOPPED);
				compEntity.setDisconnectedNodeAcknowledged(false);
				flowApi.scheduleComponents(tEtlGroupId, compEntity);
				
				ProcessGroupsApi groupApi = new ProcessGroupsApi(client);
				
				if(StringUtils.isNotBlank(parentGroupId)) {
					ProcessGroupEntity groupEntity = groupApi.getProcessGroup(parentGroupId);
					ControllerServicesEntity csEntity = flowApi.getControllerServicesFromGroup(parentGroupId, false, true);
					if(csEntity != null) {
						List<ControllerServiceEntity> css = csEntity.getControllerServices();
						if(css != null && css.size() > 0) {
							ControllerServicesApi csApi = new ControllerServicesApi(client);
							for(ControllerServiceEntity cs : css) {
								ControllerServiceRunStatusEntity csrunstate = new ControllerServiceRunStatusEntity();
								RevisionDTO rev = cs.getRevision();
								csrunstate.setDisconnectedNodeAcknowledged(false);
								csrunstate.setRevision(rev);
								csrunstate.setState(org.apache.nifi.api.toolkit.model.ControllerServiceRunStatusEntity.StateEnum.DISABLED);
								csApi.updateRunStatus(cs.getId(), csrunstate);
								ControllerServiceEntity csEnty = csApi.getControllerService(cs.getId());
								RevisionDTO revNew = csEnty.getRevision();
								csApi.removeControllerService(cs.getId(), revNew.getVersion()+"", revNew.getClientId(), false);
							}
						}
					}
					
					RevisionDTO revision = groupEntity.getRevision();
					groupApi.removeProcessGroup(parentGroupId, String.valueOf(revision.getVersion()), revision.getClientId(), groupEntity.getDisconnectedNodeAcknowledged());
				}else {
					ProcessGroupEntity groupEntity = groupApi.getProcessGroup(tEtlGroupId);
					RevisionDTO revision = groupEntity.getRevision();
					groupApi.removeProcessGroup(tEtlGroupId, String.valueOf(revision.getVersion()), revision.getClientId(), groupEntity.getDisconnectedNodeAcknowledged());
				}
			}catch(Exception e) {
				if (e instanceof ApiException) {
					e.printStackTrace();
					ApiException ae = (ApiException) e;
					if (ae.getCode() != 404) {
						if(ae.getCode() == 409){
							logger.error("删除平台[%s]下ETL流程[%s]异常!异常信息：服务器上队列不为空，请先去服务器上删除！" , platform.getId() , tEtlGroupId);
							throw new Exception("服务器上队列不为空，请先去服务器上删除！");
						}
						logger.error("删除平台[%s]下ETL流程[%s]异常!异常信息："+e.getLocalizedMessage() , platform.getId() , tEtlGroupId);
						throw e;
					}
				}else{
					logger.error("删除平台[%s]下ETL流程[%s]异常!异常信息："+e.getLocalizedMessage() , platform.getId() , tEtlGroupId);
					throw e;
				}
			}
		}
	}
	
	public void emptyNifiCollections(Map<String, Object> params) throws Exception {
		if (params != null) {
			String serverUrl = params.containsKey("etlServerUrl") && params.get("etlServerUrl") != null ? params.get("etlServerUrl").toString() : "";
			if(StringUtils.isNotBlank(serverUrl) && serverUrl.endsWith("/")) {
				serverUrl = serverUrl.substring(0 , serverUrl.lastIndexOf("/"));
			}
			String userName = params.containsKey("etlUser") && params.get("etlUser") != null ? params.get("etlUser").toString() : "";
			String password = params.containsKey("etlPwd") && params.get("etlPwd") != null ? params.get("etlPwd").toString() : "";
			String tEtlGroupId = params.containsKey("tEtlGroupId") && params.get("tEtlGroupId") != null ? params.get("tEtlGroupId").toString() : "";
			ApiClient client = new OAuthApiClient();
			client.setBasePath(serverUrl);
			client.addDefaultHeader("Content-Type", "application/json");
			client.addDefaultHeader("Accept", "application/json");
			AccessApi api = new AccessApi(client);
			
			try {
				client.setVerifyingSsl(false);
//				if (serverUrl.startsWith("https")) {
					if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
						String token = api.createAccessToken(userName, password);
						client.setAccessToken(token);
					}
//				}
				FlowApi flowApi = new FlowApi(client);
				FlowfileQueuesApi queueApi = new FlowfileQueuesApi(client);
				ProcessGroupFlowEntity flowEntity = flowApi.getFlow(tEtlGroupId);
				FlowDTO flowDto = flowEntity.getProcessGroupFlow().getFlow();
				List<ConnectionEntity> connections = new ArrayList<ConnectionEntity>();
				if(flowDto.getConnections() != null) {
					connections.addAll(flowDto.getConnections());
				}
				for(ProcessGroupEntity pgentity : flowDto.getProcessGroups()) {
					ProcessGroupFlowEntity innerFlowEntity = flowApi.getFlow(pgentity.getId());
					FlowDTO innerFlowDto = innerFlowEntity.getProcessGroupFlow().getFlow();
					if(innerFlowDto.getConnections() != null) {
						connections.addAll(innerFlowDto.getConnections());
					}
				}
				if(connections != null && connections.size() > 0) {
					for(ConnectionEntity conn : connections) {
						DropRequestEntity dropEntity = queueApi.createDropRequest(conn.getId());
						queueApi.removeDropRequest(conn.getId(), dropEntity.getDropRequest().getId());
					}
				}
				
			}catch(Exception e) {
				if (e instanceof ApiException) {
					ApiException ae = (ApiException) e;
					if (ae.getCode() != 404) {
						if(ae.getCode() == 409){
							logger.error("清空ETL流程[%s]操作处理冲突，请稍后重试！" , tEtlGroupId);
							throw new Exception("清空流程队列操作处理冲突，请稍后重试！");
						}
						logger.error("清空ETL流程[%s]队列异常!异常信息："+e.getLocalizedMessage() , tEtlGroupId);
						throw e;
					}
				}else{
					logger.error("清空ETL流程[%s]队列异常!异常信息："+e.getLocalizedMessage() , tEtlGroupId);
					throw e;
				}
			}
		}
	}
}
