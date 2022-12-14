package com.iflytek.integrated.platform.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.lang.StringUtils;
import org.apache.nifi.api.toolkit.ApiClient;
import org.apache.nifi.api.toolkit.ApiException;
import org.apache.nifi.api.toolkit.Pair;
import org.apache.nifi.api.toolkit.api.AccessApi;
import org.apache.nifi.api.toolkit.api.ControllerServicesApi;
import org.apache.nifi.api.toolkit.api.FlowApi;
import org.apache.nifi.api.toolkit.api.FlowfileQueuesApi;
import org.apache.nifi.api.toolkit.api.ProcessGroupsApi;
import org.apache.nifi.api.toolkit.api.ProcessorsApi;
import org.apache.nifi.api.toolkit.model.ConnectionEntity;
import org.apache.nifi.api.toolkit.model.ControllerServiceEntity;
import org.apache.nifi.api.toolkit.model.ControllerServiceRunStatusEntity;
import org.apache.nifi.api.toolkit.model.ControllerServicesEntity;
import org.apache.nifi.api.toolkit.model.DropRequestEntity;
import org.apache.nifi.api.toolkit.model.FlowDTO;
import org.apache.nifi.api.toolkit.model.ProcessGroupEntity;
import org.apache.nifi.api.toolkit.model.ProcessGroupFlowEntity;
import org.apache.nifi.api.toolkit.model.ProcessorEntity;
import org.apache.nifi.api.toolkit.model.RevisionDTO;
import org.apache.nifi.api.toolkit.model.ScheduleComponentsEntity;
import org.apache.nifi.api.toolkit.model.ScheduleComponentsEntity.StateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.integrated.common.dto.HttpResult;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.utils.HttpClientUtil;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.common.utils.OAuthApiClient;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.common.utils.ase.AesUtil;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.DbUrlTestDto;
import com.iflytek.integrated.platform.dto.GroovyValidateDto;
import com.iflytek.integrated.platform.dto.JoltDebuggerDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.querydsl.sql.SQLQueryFactory;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

/**
 * @author czzhan ?????????nifi??????
 */
@Component
public class NiFiRequestUtil {

	@Value("${param.schema.url}")
	private String schemaUrl;

	@Value("${param.jolt.url}")
	private String joltUrl;

	@Value("${param.interface.debug}")
	private String interfaceDebug;

	@Value("${param.interface.debugWithAuth}")
	private String interfaceDebugWithAuth;

	@Value("${param.groovy.url}")
	private String groovyUrl;

	@Value("${param.jolt.debugger}")
	private String joltDebuggerUrl;

	@Value("${param.jslt.debugger}")
	private String jsltDebuggerUrl;

	@Value("${param.wsdl.url}")
	private String wsdlServiceUrl;

	@Value("${param.wsdl.urlWithAuth}")
	private String wsdlServiceUrlWithAuth;

	@Value("${param.xml2json.url}")
	private String xml2jsonUrl;

	@Value("${param.db.test.url}")
	private String testDbUrl;

	@Value("${param.metacache.clean.url}")
	private String metacacheCleanUrl;

	@Value("${config.request.nifiapi.readtimeout}")
	private int readTimeout;

	@Autowired
	public SQLQueryFactory sqlQueryFactory;

	@Autowired
	private RedisUtil redisUtil;

	private static final Logger logger = LoggerFactory.getLogger(NiFiRequestUtil.class);

	private static volatile String restAuthHeaderName = "";

	private static volatile String wsAuthHeaderName = "";

	private static final String restTokenCacheKey = "integrated:restapi:token";

	private static final String wsTokenCacheKey = "integrated:wsapi:token";

	private static final Map<String, Object> loginMap = new HashMap<String, Object>();

	static {
		loginMap.put("uname", "esb/admin");
		loginMap.put("upwd", "iflytek@ESB-PLUS");
	}

	private ObjectMapper om = new ObjectMapper();

	/**
	 * ??????paramFormat???formatType??????schema
	 *
	 * @param paramFormat
	 * @param formatType
	 * @return
	 */
	public String generateSchemaToInterface(String paramFormat, String formatType) {
		if (StringUtils.isNotEmpty(paramFormat)) {
			// ??????????????????
			String type = Constant.ParamFormatType.getByType(formatType);
			if (StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)) {
				throw new RuntimeException("??????????????????");
			}
			// ????????????
			String schema = generateSchema(paramFormat, type);
			if (StringUtils.isEmpty(schema)) {
				throw new RuntimeException("schema????????????");
			}
			return schema;
		}
		return "";
	}

	public String xml2json(String xml) {
		if (StringUtils.isNotEmpty(xml)) {
			// ??????????????????
			HttpResult result;
			try {
				result = HttpClientUtil.doPost(xml2jsonUrl, xml);
				return result.getContent();
			} catch (Exception e) {
				throw new RuntimeException("xml???json????????????", e);
			}
		}
		return "";
	}

	/**
	 * ??????NiFi????????????????????????schema
	 *
	 * @param businessInterface
	 */
	public void generateSchemaToInterface(TBusinessInterface businessInterface) {
		if (StringUtils.isNotEmpty(businessInterface.getInParamFormat())) {
			// ??????????????????
			String type = Constant.ParamFormatType.getByType(businessInterface.getInParamFormatType());
			if (StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)) {
				throw new RuntimeException("????????????????????????");
			}
			// ????????????
			String schema = generateSchema(businessInterface.getInParamFormat(), type);
			if (StringUtils.isEmpty(schema)) {
				throw new RuntimeException("??????schema????????????");
			}
			businessInterface.setInParamSchema(schema);
		}
		if (StringUtils.isNotEmpty(businessInterface.getOutParamFormat())) {
			// ??????????????????
			String type = Constant.ParamFormatType.getByType(businessInterface.getOutParamFormatType());
			if (StringUtils.isBlank(type) || Constant.ParamFormatType.NONE.getType().equals(type)) {
				throw new RuntimeException("????????????????????????");
			}
			// ????????????
			String schema = generateSchema(businessInterface.getOutParamFormat(), type);
			if (StringUtils.isEmpty(schema)) {
				throw new RuntimeException("??????schema????????????");
			}
			businessInterface.setOutParamSchema(schema);
		}
	}

	/**
	 * ??????jolt????????????
	 *
	 * @param dto
	 * @return
	 */
	public Map joltDebugger(JoltDebuggerDto dto) {
		try {
			String type = PlatformUtil.strIsJsonOrXml(dto.getOriginObj());
			String debuggerUrl = dto.getJslt() != null ? jsltDebuggerUrl : joltDebuggerUrl;
			String url = debuggerUrl + "?contenttype=" + type;
			String param = JackSonUtils.transferToJson(dto);
			HttpResult result = HttpClientUtil.doPost(url, param);
			if (result != null && StringUtils.isNotBlank(result.getContent())) {
				return JackSonUtils.jsonToTransfer(result.getContent(), Map.class);
			}
		} catch (Exception e) {
			throw new RuntimeException("jolt/jslt????????????????????????");
		}
		return null;
	}

	/**
	 * ??????jolt????????????
	 *
	 * @param dto
	 * @return
	 */
	public Object joltDebugger2(JoltDebuggerDto dto) {
		try {
			String type = PlatformUtil.strIsJsonOrXml(dto.getOriginObj());
			String debuggerUrl = dto.getJslt() != null ? jsltDebuggerUrl : joltDebuggerUrl;
			String url = debuggerUrl + "?contenttype=" + type;
			String param = JackSonUtils.transferToJson(dto);
			HttpResult result = HttpClientUtil.doPost(url, param);
			if (result != null && StringUtils.isNotBlank(result.getContent())) {
				return JackSonUtils.jsonToTransfer(result.getContent(), Object.class);
			}
		} catch (Exception e) {
			throw new RuntimeException("jolt/jslt????????????????????????");
		}
		return null;
	}

	/**
	 * ??????format??????schema
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
	 * ??????format??????jolt
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
	 * ??????groovy????????????????????????
	 *
	 * @param content
	 * @return
	 */
	public GroovyValidateDto groovyUrl(String content) {
		try {
			HttpResult result = HttpClientUtil.doPost(groovyUrl, content);
			return JackSonUtils.jsonToTransfer(result.getContent(), GroovyValidateDto.class);
		} catch (Exception e) {
			throw new RuntimeException("????????????groovy????????????");
		}
	}

	/**
	 * ????????????
	 *
	 * @param format
	 * @return
	 */
	public String interfaceDebug(String format, Map<String, String> headerMap, boolean withAuth) {
		try {
			HttpResult result = HttpClientUtil.doPostWithHeaders(withAuth ? interfaceDebugWithAuth : interfaceDebug,
					format, headerMap, readTimeout);
			return result.getContent();
		} catch (Exception e) {
			throw new RuntimeException("??????????????????????????????", e);
		}
	}

	public Map<String, String> interfaceAuthLogin(String loginUrl, boolean isws) {
		Map<String, String> headerMap = new HashMap<>();
		try {
			Object cachetoken = redisUtil.get(isws ? wsTokenCacheKey : restTokenCacheKey);
			if (cachetoken == null
					|| (isws ? StringUtils.isBlank(wsAuthHeaderName) : StringUtils.isBlank(restAuthHeaderName))) {
				HttpResult httpResult = HttpClientUtil.doPost(loginUrl + "auth/login", HttpClientUtil.JSON, loginMap);
				String tokenResult = httpResult.getContent();
				String token = "";
				if (StringUtils.isNotBlank(tokenResult)) {
					Map<String, Object> tokenMap = om.readValue(tokenResult, new TypeReference<Map<String, Object>>() {
					});
					if ("200".equals(String.valueOf(tokenMap.get("result")))) {
						token = tokenMap.get("data").toString();
						String expiration = tokenMap.get("expiration").toString();
						if (isws) {
							wsAuthHeaderName = tokenMap.get("tokenHeaderName").toString();
						} else {
							restAuthHeaderName = tokenMap.get("tokenHeaderName").toString();
						}
						headerMap.put(isws ? wsAuthHeaderName : restAuthHeaderName, token);
						redisUtil.set(isws ? wsTokenCacheKey : restTokenCacheKey, token);
						redisUtil.expire(isws ? wsTokenCacheKey : restTokenCacheKey, Long.valueOf(expiration) - 10);
					}

				}
			} else {
				String token = String.valueOf(cachetoken);
				headerMap.put(isws ? wsAuthHeaderName : restAuthHeaderName, token);
			}
			return headerMap;
		} catch (Exception e) {
			throw new RuntimeException("??????????????????token????????????", e);
		}
	}

	/**
	 * ?????????????????????
	 * 
	 * @param dto
	 * @return
	 */
	public String testDbUrl(DbUrlTestDto dto) {
		try {
			String param = JackSonUtils.transferToJson(dto);
			HttpResult result = HttpClientUtil.doPost(testDbUrl, param);
			return result.getContent();
		} catch (Exception e) {
			throw new RuntimeException("?????????????????????????????????");
		}
	}

	public String getWsServiceUrl() {
		return wsdlServiceUrl;
	}

	public String getWsServiceUrlWithAuth() {
		return wsdlServiceUrlWithAuth;
	}

	public String getInterfaceDebugWithAuth() {
		return interfaceDebugWithAuth;
	}

	public void deleteNifiEtlFlow(TPlatform platform, String tEtlGroupId, String parentGroupId) throws Exception {
		if (platform != null) {
			String serverUrl = platform.getEtlServerUrl();
			if (StringUtils.isNotBlank(serverUrl) && serverUrl.endsWith("/")) {
				serverUrl = serverUrl.substring(0, serverUrl.lastIndexOf("/"));
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
				if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
					try {
						password = AesUtil.decrypt(password);
					} catch (IllegalBlockSizeException de) {
						logger.error("??????etl???????????????????????????????????????????????????" + password);
					}
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

				if (StringUtils.isNotBlank(parentGroupId)) {
					
					ProcessGroupFlowEntity parentGroupFlow = flowApi.getFlow(parentGroupId);
					
					if(parentGroupFlow != null && parentGroupFlow.getProcessGroupFlow().getFlow().getProcessGroups().size() == 1) {

						ProcessGroupEntity groupEntity = groupApi.getProcessGroup(parentGroupId);
						ControllerServicesEntity csEntity = flowApi.getControllerServicesFromGroup(parentGroupId, false,
								true);
						if (csEntity != null) {
							List<ControllerServiceEntity> css = csEntity.getControllerServices();
							if (css != null && css.size() > 0) {
								ControllerServicesApi csApi = new ControllerServicesApi(client);
								for (ControllerServiceEntity cs : css) {
									ControllerServiceRunStatusEntity csrunstate = new ControllerServiceRunStatusEntity();
									RevisionDTO rev = cs.getRevision();
									csrunstate.setDisconnectedNodeAcknowledged(false);
									csrunstate.setRevision(rev);
									csrunstate.setState(
											org.apache.nifi.api.toolkit.model.ControllerServiceRunStatusEntity.StateEnum.DISABLED);
									csApi.updateRunStatus(cs.getId(), csrunstate);
									ControllerServiceEntity csEnty = csApi.getControllerService(cs.getId());
									RevisionDTO revNew = csEnty.getRevision();
									csApi.removeControllerService(cs.getId(), revNew.getVersion() + "",
											revNew.getClientId(), false);
								}
							}
						}

						RevisionDTO revision = groupEntity.getRevision();
						groupApi.removeProcessGroup(parentGroupId, String.valueOf(revision.getVersion()),
								revision.getClientId(), groupEntity.getDisconnectedNodeAcknowledged());
					}else {
						ProcessGroupEntity groupEntity = groupApi.getProcessGroup(tEtlGroupId);
						RevisionDTO revision = groupEntity.getRevision();
						groupApi.removeProcessGroup(tEtlGroupId, String.valueOf(revision.getVersion()),
								revision.getClientId(), groupEntity.getDisconnectedNodeAcknowledged());
					}
					
				} else {
					ProcessGroupEntity groupEntity = groupApi.getProcessGroup(tEtlGroupId);
					RevisionDTO revision = groupEntity.getRevision();
					groupApi.removeProcessGroup(tEtlGroupId, String.valueOf(revision.getVersion()),
							revision.getClientId(), groupEntity.getDisconnectedNodeAcknowledged());
				}
			} catch (Exception e) {
				if (e instanceof ApiException) {
					e.printStackTrace();
					ApiException ae = (ApiException) e;
					if (ae.getCode() != 404) {
						if (ae.getCode() == 409) {
							logger.error("????????????[%s]???ETL??????[%s]??????!???????????????????????????????????????????????????????????????????????????", platform.getId(),
									tEtlGroupId);
							throw new Exception("????????????????????????????????????????????????????????????");
						}
						logger.error("????????????[%s]???ETL??????[%s]??????!???????????????" + e.getLocalizedMessage(), platform.getId(),
								tEtlGroupId);
						throw e;
					}
				} else {
					logger.error("????????????[%s]???ETL??????[%s]??????!???????????????" + e.getLocalizedMessage(), platform.getId(), tEtlGroupId);
					throw e;
				}
			}
		}
	}
	
	public void stopNifiEtlFlow(TPlatform platform, String tEtlGroupId) throws Exception {
		if(platform != null) {
			String serverUrl = platform.getEtlServerUrl();
			if (StringUtils.isNotBlank(serverUrl) && serverUrl.endsWith("/")) {
				serverUrl = serverUrl.substring(0, serverUrl.lastIndexOf("/"));
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
				if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
					try {
						password = AesUtil.decrypt(password);
					} catch (IllegalBlockSizeException de) {
						logger.error("??????etl???????????????????????????????????????????????????" + password);
					}
					String token = api.createAccessToken(userName, password);
					client.setAccessToken(token);
				}
				ProcessorsApi papi = new ProcessorsApi(client);
				FlowApi flowApi = new FlowApi(client);
				
				Map<String , Object> paramstop = new HashMap<String , Object>();
				paramstop.put("id", tEtlGroupId);
				paramstop.put("disconnectedNodeAcknowledged", false);
				paramstop.put("state", "STOPPED");
				
				com.squareup.okhttp.Call call = scheduleComponentsCall(tEtlGroupId, paramstop, client);
				client.execute(call);
				
				try {
					ProcessGroupFlowEntity groupflows = flowApi.getFlow(tEtlGroupId);
					if(groupflows != null) {
						List<ProcessGroupEntity> flowgroups = groupflows.getProcessGroupFlow().getFlow().getProcessGroups();
						for(ProcessGroupEntity group : flowgroups) {
							int activecount = group.getStatus().getAggregateSnapshot().getActiveThreadCount();
							if(activecount > 0) {
								String subGroupid = group.getId();
								ProcessGroupFlowEntity subflowentity = flowApi.getFlow(subGroupid);
								if(subflowentity != null) {
									List<ProcessorEntity> processors = subflowentity.getProcessGroupFlow().getFlow().getProcessors();
									if(processors != null && processors.size() >0) {
										for(ProcessorEntity processor : processors) {
											int pactivecount = processor.getStatus().getAggregateSnapshot().getActiveThreadCount();
											if(pactivecount > 0) {
												try {
													papi.terminateProcessor(processor.getId());
												}catch(Exception e) {
													if(e instanceof ApiException) {
														ApiException ae = (ApiException) e;
														if(ae.getCode() == 409) {
															Thread.sleep(5000);
															papi.terminateProcessor(processor.getId());
															continue;
														}
														throw e;
													}
													throw e;
												}
											}
										}
									}
								}
							}
						}
						Map<String , Object> params = new HashMap<String , Object>();
						params.put("tEtlGroupId", tEtlGroupId);
						params.put("etlServerUrl", serverUrl);
						params.put("etlUser", userName);
						params.put("etlPwd", password);
						emptyNifiCollections(params);
					}
				}catch (Exception e) {
					logger.error("?????????????????????????????????" , e);
				}
			} catch (Exception e) {
				throw e;
			}
			
		}
		
	}
	
//	public static void main(String[] args) {
//		NiFiRequestUtil nifiutil = new NiFiRequestUtil();
//		new AesUtil("w5xv7[Nmc0Z/3U^X");
//		TPlatform platform = new TPlatform();
//		platform.setEtlServerUrl("https://172.31.184.170:8080/nifi-api");
//		platform.setEtlUser("nifi/admin@IFLYTEK.COM");
//		platform.setEtlPwd("KD_99@sjdj");
//		try {
//			nifiutil.stopNifiEtlFlow(platform, "13c9882c-0181-1000-3823-041d8ab03be6");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public void emptyNifiCollections(Map<String, Object> params) throws Exception {
		if (params != null) {
			String serverUrl = params.containsKey("etlServerUrl") && params.get("etlServerUrl") != null
					? params.get("etlServerUrl").toString()
					: "";
			if (StringUtils.isNotBlank(serverUrl) && serverUrl.endsWith("/")) {
				serverUrl = serverUrl.substring(0, serverUrl.lastIndexOf("/"));
			}
			String userName = params.containsKey("etlUser") && params.get("etlUser") != null
					? params.get("etlUser").toString()
					: "";
			String password = params.containsKey("etlPwd") && params.get("etlPwd") != null
					? params.get("etlPwd").toString()
					: "";
			String tEtlGroupId = params.containsKey("tEtlGroupId") && params.get("tEtlGroupId") != null
					? params.get("tEtlGroupId").toString()
					: "";
			ApiClient client = new OAuthApiClient();
			client.setBasePath(serverUrl);
			client.addDefaultHeader("Content-Type", "application/json");
			client.addDefaultHeader("Accept", "application/json");
			client.addDefaultHeader("Connection", "close");
			AccessApi api = new AccessApi(client);

			try {
				client.setVerifyingSsl(false);
//				if (serverUrl.startsWith("https")) {
				if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
					try {
						password = AesUtil.decrypt(password);
					} catch (IllegalBlockSizeException de) {
						logger.error("??????etl???????????????????????????????????????????????????" + password);
					}
					String token = api.createAccessToken(userName, password);
					client.setAccessToken(token);
				}
//				}
				FlowApi flowApi = new FlowApi(client);
				FlowfileQueuesApi queueApi = new FlowfileQueuesApi(client);
				ProcessGroupFlowEntity flowEntity = flowApi.getFlow(tEtlGroupId);
				FlowDTO flowDto = flowEntity.getProcessGroupFlow().getFlow();
				List<ConnectionEntity> connections = new ArrayList<ConnectionEntity>();
				if (flowDto.getConnections() != null) {
					connections.addAll(flowDto.getConnections());
				}
				for (ProcessGroupEntity pgentity : flowDto.getProcessGroups()) {
					ProcessGroupFlowEntity innerFlowEntity = flowApi.getFlow(pgentity.getId());
					FlowDTO innerFlowDto = innerFlowEntity.getProcessGroupFlow().getFlow();
					if (innerFlowDto.getConnections() != null) {
						connections.addAll(innerFlowDto.getConnections());
					}
				}
				if (connections != null && connections.size() > 0) {
					for (ConnectionEntity conn : connections) {
						DropRequestEntity dropEntity = queueApi.createDropRequest(conn.getId());
						queueApi.removeDropRequest(conn.getId(), dropEntity.getDropRequest().getId());
					}
				}

			} catch (Exception e) {
				if (e instanceof ApiException) {
					ApiException ae = (ApiException) e;
					if (ae.getCode() == 0) {
						throw new RuntimeException("etl????????????????????????????????????????????????????????????????????????????????????");
					}
					if (ae.getCode() != 404) {
						if (ae.getCode() == 409) {
							logger.error("??????ETL??????[%s]???????????????????????????????????????", tEtlGroupId);
							throw new Exception("?????????????????????????????????????????????????????????");
						}
						logger.error("??????ETL??????[%s]????????????!???????????????" + e.getLocalizedMessage(), tEtlGroupId);
						throw e;
					}
				} else {
					logger.error("??????ETL??????[%s]????????????!???????????????" + e.getLocalizedMessage(), tEtlGroupId);
					throw e;
				}
			}
		}
	}

	public void metacacheClean(List<String> keys) {
		try {
			String keysContent = JackSonUtils.transferToJson(keys);
			if(metacacheCleanUrl.contains(",")) {
				String[] urls = metacacheCleanUrl.split(",");
				for(String url : urls) {
					HttpClientUtil.doPost(url, keysContent);
				}
				return;
			}
			HttpClientUtil.doPost(metacacheCleanUrl, keysContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private com.squareup.okhttp.Call scheduleComponentsCall(String id, Map<String , Object> params, ApiClient apiClient) throws ApiException, JsonProcessingException {
		String[] localVarAuthNames = new String[] {  };
        // create path and map variables
        String localVarPath = "/flow/process-groups/{id}".replaceAll("\\{format\\}","json")
        .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        localVarHeaderParams.put("Accept", "application/json");
        localVarHeaderParams.put("Content-Type", "application/json");
        
        apiClient.updateParamsForAuth(localVarAuthNames, localVarQueryParams, localVarHeaderParams);
        
        final String url = apiClient.buildUrl(localVarPath, localVarQueryParams);
        final Request.Builder reqBuilder = new Request.Builder().url(url);
        apiClient.processHeaderParams(localVarHeaderParams, reqBuilder);
        ObjectMapper om = new ObjectMapper();
        String content = om.writeValueAsString(params);
        RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), content);
        Request request = reqBuilder.method("PUT", reqBody).build();

        return apiClient.getHttpClient().newCall(request);
    }
}
