package com.iflytek.integrated.platform.service;

import com.google.gson.reflect.TypeToken;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.JackSonUtils;
import com.iflytek.integrated.common.utils.XmlJsonUtils;
import com.iflytek.integrated.common.utils.ase.AesUtil;
import com.iflytek.integrated.common.utils.OAuthApiClient;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.EtlTplDto;
import com.iflytek.integrated.platform.entity.TEtlTpl;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.squareup.okhttp.Call;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.api.toolkit.ApiClient;
import org.apache.nifi.api.toolkit.ApiException;
import org.apache.nifi.api.toolkit.ApiResponse;
import org.apache.nifi.api.toolkit.Pair;
import org.apache.nifi.api.toolkit.api.AccessApi;
import org.apache.nifi.api.toolkit.api.FlowApi;
import org.apache.nifi.api.toolkit.model.ProcessGroupEntity;
import org.apache.nifi.api.toolkit.model.ProcessGroupFlowEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.iflytek.integrated.platform.entity.QTEtlTpl.qTEtlTpl;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;

/**
 * @author lsn
 */
@Slf4j
@Api(tags = "etl服务")
@RestController
@RequestMapping("/{version}/pt/etltpl")
public class EtlTplService extends BaseService<TEtlTpl, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(EtlTplService.class);

	@Autowired
	private BatchUidService batchUidService;

	public EtlTplService() {
		super(qTEtlTpl, qTEtlTpl.id);
	}

	@ApiOperation(value = "分页获取模板")
	@GetMapping("/getEtlTpls/{tplType}/{tplFunType}")
	public ResultDto<TableData<TEtlTpl>> getEtlTpls(@PathVariable Integer tplType, @PathVariable Integer tplFunType,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
			@RequestParam(value = "tplName", required = false) String tplName) {
		BooleanExpression conditon = qTEtlTpl.tplType.eq(tplType);
		if (tplFunType != 0) {
			conditon = conditon.and(qTEtlTpl.tplFunType.eq(tplFunType));
		}
		if (StringUtils.isNotBlank(tplName)) {
			conditon = conditon.and(qTEtlTpl.tplName.like("%" + tplName + "%"));
		}
		QueryResults<TEtlTpl> queryResults = sqlQueryFactory
				.select(Projections.bean(TEtlTpl.class, qTEtlTpl.id, qTEtlTpl.tplType,
						qTEtlTpl.tplName.append(qTEtlTpl.suffixName).as(qTEtlTpl.tplName),
						qTEtlTpl.tplFunType, qTEtlTpl.tplDesp, qTEtlTpl.createdBy, qTEtlTpl.createdTime,
						qTEtlTpl.updatedBy, qTEtlTpl.updatedTime))
				.from(qTEtlTpl).where(conditon).limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTEtlTpl.createdTime.desc()).fetchResults();

		// 分页
		TableData<TEtlTpl> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", tableData);
	}

	@ApiOperation(value = "获取模板")
	@GetMapping("/listEtlTpls/{tplType}/{tplFunType}")
	public ResultDto<List<TEtlTpl>> listEtlTpls(@PathVariable Integer tplType, @PathVariable Integer tplFunType,
			@RequestParam(value = "tplName", required = false) String tplName) {
		BooleanExpression conditon = qTEtlTpl.tplType.eq(tplType);
		if (tplFunType != 0) {
			conditon = conditon.and(qTEtlTpl.tplFunType.eq(tplFunType));
		}
		if (StringUtils.isNotBlank(tplName)) {
			conditon = conditon.and(qTEtlTpl.tplName.like("%" + tplName + "%"));
		}
		List<TEtlTpl> queryResults = sqlQueryFactory.select(qTEtlTpl).from(qTEtlTpl).where(conditon)
				.orderBy(qTEtlTpl.createdTime.desc()).fetch();

		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "", queryResults);
	}

	@ApiOperation(value = "上传模板")
	@PostMapping(path = "/uploadEtlTpls/{tplType}/{tplFunType}")
	public ResultDto<String> uploadEtlTpls(@PathVariable Integer tplType, @PathVariable Integer tplFunType,
			@RequestParam("tplFiles") MultipartFile[] tplFiles, String tplDesp) {

		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}

		SQLInsertClause insertClause = sqlQueryFactory.insert(qTEtlTpl);
		// 流程模板
		try {
			if (tplFiles == null || tplFiles.length == 0) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到上传文件!", "没有获取到上传文件!");
			}
			String exsitsFileNames = "";
			int insetNum = 0;
			for (MultipartFile file : tplFiles) {
				String originalFilename = file.getOriginalFilename();
				String fileName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
				String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
				InputStream is = file.getInputStream();
				String tplContent = IOUtils.toString(is, "UTF-8");
				is.close();
				String tplId = sqlQueryFactory.select(qTEtlTpl.id).from(qTEtlTpl).where(qTEtlTpl.tplName.eq(fileName).and(qTEtlTpl.suffixName.eq(suffixName)))
						.fetchFirst();
				if (StringUtils.isNotBlank(tplId)) {
					exsitsFileNames += fileName + ",";
					continue;
				}
				String id = batchUidService.getUid(qTEtlTpl.getTableName()) + "";
				insertClause.set(qTEtlTpl.id, id).set(qTEtlTpl.tplName, fileName).set(qTEtlTpl.suffixName, suffixName)
						.set(qTEtlTpl.tplType, tplType).set(qTEtlTpl.tplFunType, tplFunType).set(qTEtlTpl.tplContent, tplContent)
						.set(qTEtlTpl.tplDesp, tplDesp)
						.set(qTEtlTpl.createdBy, loginUserName != null ? loginUserName : "")
						.set(qTEtlTpl.createdTime, new Date())
						.set(qTEtlTpl.updatedBy, loginUserName != null ? loginUserName : "")
						.set(qTEtlTpl.updatedTime, new Date()).addBatch();
				insetNum++;
			}
			if (StringUtils.isNotBlank(exsitsFileNames)) {
				if (exsitsFileNames.endsWith(",")) {
					exsitsFileNames = exsitsFileNames.substring(0, exsitsFileNames.lastIndexOf(","));
				}
				exsitsFileNames = "[" + exsitsFileNames + "]";
			}
			if (insetNum == tplFiles.length) {
				long insertCount = insertClause.execute();
				return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "所选模板文件上传成功", insertCount + "");
			} else if (insetNum > 0 && insetNum < tplFiles.length) {
				long insertCount = insertClause.execute();
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "所选模板文件部分上传成功,文件" + exsitsFileNames + "已存在",
						"所选模板文件部分上传成功,文件" + exsitsFileNames + "已存在！成功上传文件数：" + insertCount);
			} else {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "所选模板文件都已存在，请先删除后再上传！", "流程模板文件都已存在，请先删除后再上传！");
			}

		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "上传文件入库处理失败", e.getLocalizedMessage());
		}
	}

	@ApiOperation(value = "修改模板")
	@PostMapping(path = "/editEtlTpl/{id}")
	public ResultDto<String> editEtlTpl(@PathVariable String id, @RequestBody EtlTplDto tplDto) {

		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
		}

		SQLUpdateClause updateClause = sqlQueryFactory.update(qTEtlTpl);
		// 流程模板
		try {
			if (tplDto != null && tplDto.getTplName() != null) {
				String tplName = tplDto.getTplName();
				updateClause.set(qTEtlTpl.tplName, tplName.substring(0, tplName.lastIndexOf(".")));
			}
			if (tplDto != null && tplDto.getTplType() != null) {
				updateClause.set(qTEtlTpl.tplType, tplDto.getTplType());
			}
			if (tplDto != null && tplDto.getTplFunType() != null) {
				updateClause.set(qTEtlTpl.tplFunType, tplDto.getTplFunType());
			}
			if (tplDto != null && tplDto.getTplDesp() != null) {
				updateClause.set(qTEtlTpl.tplDesp, tplDto.getTplDesp());
			}
			updateClause.set(qTEtlTpl.updatedBy, loginUserName != null ? loginUserName : "");
			updateClause.set(qTEtlTpl.updatedTime, new Date());
			updateClause.where(qTEtlTpl.id.eq(id));
			long insertCount = updateClause.execute();
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "流程模板文件更新成功", insertCount + "");
		} catch (Exception e) {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "更新文件入库处理失败", e.getLocalizedMessage());
		}
	}

	@ApiOperation(value = "删除模板")
	@PostMapping(path = "/delEtlTpl/{id}")
	public ResultDto<String> delEtlTpl(@PathVariable String id) {

		// 校验是否获取到登录用户
//		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
//		if (StringUtils.isBlank(loginUserName)) {
//			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
//		}

		long result = sqlQueryFactory.delete(qTEtlTpl).where(qTEtlTpl.id.eq(id)).execute();
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "删除流程模板成功", result + "");
	}

	@ApiOperation(value = "下载模板")
	@GetMapping(path = "/downloadEtlTpl/{ids}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloadEtlTpl(@PathVariable String ids, HttpServletRequest request, HttpServletResponse response) {

		// 校验是否获取到登录用户
		String loginUserName = UserLoginIntercept.LOGIN_USER.UserName();
		if (StringUtils.isBlank(loginUserName)) {
			throw new RuntimeException("没有获取到登录用户!");
		}
		String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String tplsZipName = "etl_tpls_" + dateStr + ".zip";
		String[] idsArr = ids.split(",");

		List<TEtlTpl> result = sqlQueryFactory.select(qTEtlTpl).from(qTEtlTpl).where(qTEtlTpl.id.in(idsArr)).fetch();
		Map<String, byte[]> filesMap = new HashMap<String, byte[]>();
		if (result != null && result.size() > 0) {
			result.forEach(tpl -> {
				String tplName = tpl.getTplName();
				String suffixName = tpl.getSuffixName();
				String fileName = tplName + suffixName;
				String tplContent = tpl.getTplContent();
				filesMap.put(fileName, tplContent.getBytes());
			});
		}
		try {
			response.setContentType("application/x-msdownload");
			response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(tplsZipName, "utf-8"));

			ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
			BufferedOutputStream bos = new BufferedOutputStream(zos);

			for (Entry<String, byte[]> entry : filesMap.entrySet()) {
				String fileName = entry.getKey(); // 每个zip文件名
				byte[] file = entry.getValue(); // 这个zip文件的字节

				BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(file));
				zos.putNextEntry(new ZipEntry(fileName));

				int len = 0;
				byte[] buf = new byte[10 * 1024];
				while ((len = bis.read(buf, 0, buf.length)) != -1) {
					bos.write(buf, 0, len);
				}
				bis.close();
				bos.flush();
			}
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@ApiOperation(value = "同步模板文件到服务器")
	@GetMapping(path = "/syncEtlTpls/{platformId}/{ids}")
	public ResultDto<String> syncEtlTpls(@PathVariable String platformId, @PathVariable String ids,
			@ApiParam(value = "上传服务器组名，需要具体指定组上传时传入，正常忽略", example = "ETL") @RequestParam(value = "groupName", required = false) String groupName) {
		TPlatform platform = sqlQueryFactory.select(qTPlatform).from(qTPlatform).where(qTPlatform.id.eq(platformId))
				.fetchFirst();
		if (platform != null) {
			String[] idsArr = ids.split(",");

			List<TEtlTpl> result = sqlQueryFactory.select(qTEtlTpl).from(qTEtlTpl).where(qTEtlTpl.id.in(idsArr))
					.fetch();
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
			client.addDefaultHeader("responseType", "json");
			AccessApi api = new AccessApi(client);
			FlowApi flowApi = new FlowApi(client);
			String uploadGroupId = "";
			try {
				client.setVerifyingSsl(false);
//				if (serverUrl.startsWith("https")) {
					if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
						try {
							password = AesUtil.decrypt(password);
						}catch(IllegalBlockSizeException de) {
							logger.error("解密etl服务器密码失败，将使用原配置密码："+password);
						}
						String token = api.createAccessToken(userName, password);
						client.setAccessToken(token);
					}
//				}
				ProcessGroupFlowEntity groupFlowEntity = flowApi.getFlow("root");
				uploadGroupId = groupFlowEntity.getProcessGroupFlow().getId();
				if (StringUtils.isNotBlank(groupName)) {
					ProcessGroupEntity groupEntity = groupFlowEntity.getProcessGroupFlow().getFlow().getProcessGroups()
							.stream().filter(group -> {
								return group.getComponent().getName().equals(groupName);
							}).findFirst().orElse(null);
					if (groupEntity != null) {
						uploadGroupId = groupEntity.getId();
					}
				}
				if (StringUtils.isBlank(uploadGroupId)) {
					return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "调用NIFI API异常,未获取到ETL服务器组ID信息", "未获取到ETL服务器组ID信息");
				}
			} catch (Exception e) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "调用NIFI API异常,请检查项目分类下etl服务器配置信息", e.getLocalizedMessage());
			}
			if (result == null || result.size() == 0) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "上传模板文件失败", "未获取到模板文件数据");
			}
			List<String> exsitsFiles = new ArrayList<>();
			List<String> failUploadFiles = new ArrayList<>();
			
			
			for (TEtlTpl tpl : result) {
				String tplName = tpl.getTplName();
				tplName = tplName + ".xml";
				String tplContent = tpl.getTplContent();
				File file = new File(tplName);
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						logger.error("创建模板临时文件异常", e);
						return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "创建模板临时文件异常", "");
					}
				}
				try (final FileWriter writer = new FileWriter(file); BufferedWriter bw = new BufferedWriter(writer)) {
					bw.write(tplContent);
					bw.flush();
					writer.close();
					bw.close();
					Call call = buildUpdateTemplateCall(uploadGroupId , client , file);
					Type localVarReturnType = new TypeToken<String>(){}.getType();
					client.execute(call, localVarReturnType);
				} catch (Exception e) {
					if (e instanceof ApiException) {
						ApiException ae = (ApiException) e;
						if (ae.getCode() == 409) {
							exsitsFiles.add(tplName);
						} else if (String.valueOf(ae.getCode()).startsWith("2")) {
							String responseBody = ae.getResponseBody();
							if(StringUtils.isNotBlank(responseBody) && responseBody.contains("errorResponse")) {
								failUploadFiles.add(tplName);
							}else {
								continue;
							}
						} else {
							failUploadFiles.add(tplName);
						}
					} else {
						failUploadFiles.add(tplName);
					}
					logger.error("上传模板文件到服务器失败，文件名：" + tplName, e);
				} finally {
					file.delete();
				}
			}
//			dir.delete();
			String errorMsg = "";
			if (exsitsFiles.size() > 0) {
				errorMsg += String.format("模板文件[%s]在服务器已存在，请先删除后再上传！", StringUtils.join(exsitsFiles.toArray(), ","));
			}
			if (failUploadFiles.size() > 0) {
				errorMsg += String.format("模板文件[%s]同步上传服务器失败，请检查模板文件格式是否正确，或联系系统管理员！",
						StringUtils.join(failUploadFiles.toArray(), ","));
			}
			if (StringUtils.isNotBlank(errorMsg)) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, errorMsg, errorMsg);
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "同步模板文件到服务器成功", "success");
		} else {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "同步模板文件到服务器失败,平台分类ID入参异常，无法查询平台分类信息", "同步模板文件到服务器失败,平台分类ID入参异常，无法查询平台分类信息");
		}
	}

	@ApiOperation(value = "上传模板文件到NIFI服务器")
	@PostMapping(path = "/uploadEtlTplsToNIFI/{platformId}")
	public ResultDto<String> uploadEtlTplsToNIFI(@PathVariable String platformId,
										         @RequestParam("tplFiles") MultipartFile[] tplFiles,
												 @ApiParam(value = "上传服务器组名，需要具体指定组上传时传入，正常忽略", example = "ETL")
													 @RequestParam(value = "groupName", required = false) String groupName) {
		TPlatform platform = sqlQueryFactory.select(qTPlatform).from(qTPlatform).where(qTPlatform.id.eq(platformId))
				.fetchFirst();
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
			FlowApi flowApi = new FlowApi(client);
			String uploadGroupId = "";
			try {
				client.setVerifyingSsl(false);
//				if (serverUrl.startsWith("https")) {
					if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
						try {
							password = AesUtil.decrypt(password);
						}catch(IllegalBlockSizeException de) {
							logger.error("解密etl服务器密码失败，将使用原配置密码："+password);
						}
						String token = api.createAccessToken(userName, password);
						client.setAccessToken(token);
					}
//				}
				ProcessGroupFlowEntity groupFlowEntity = flowApi.getFlow("root");
				uploadGroupId = groupFlowEntity.getProcessGroupFlow().getId();
				if (StringUtils.isNotBlank(groupName)) {
					ProcessGroupEntity groupEntity = groupFlowEntity.getProcessGroupFlow().getFlow().getProcessGroups()
							.stream().filter(group -> {
								return group.getComponent().getName().equals(groupName);
							}).findFirst().orElse(null);
					if (groupEntity != null) {
						uploadGroupId = groupEntity.getId();
					}
				}
				if (StringUtils.isBlank(uploadGroupId)) {
					return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "调用NIFI API异常,未获取到ETL服务器组ID信息", "未获取到ETL服务器组ID信息");
				}
			} catch (Exception e) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "调用NIFI API异常,请检查项目分类下etl服务器配置信息", e.getLocalizedMessage());
			}
			if (tplFiles == null || tplFiles.length == 0) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "没有获取到上传文件!", "没有获取到上传文件!");
			}
			List<String> exsitsFiles = new ArrayList<>();
			List<String> failUploadFiles = new ArrayList<>();
			String filename ="";
			File file = null;
			List<Map<String , String>> uploadedTpls = new ArrayList<>();
			try{
				client.addDefaultHeader("Content-Type", "multipart/form-data");
				client.addDefaultHeader("Accept", "application/xml");
				for (MultipartFile multipartFile : tplFiles) {
					filename = multipartFile.getOriginalFilename();
					InputStream is = multipartFile.getInputStream();
					String tplContent = IOUtils.toString(is, "UTF-8");
					is.close();

					file = new File(filename);
					if (!file.exists()) {
						file.createNewFile();
					}
					final FileWriter writer = new FileWriter(file);
					BufferedWriter bw = new BufferedWriter(writer);
					bw.write(tplContent);
					bw.flush();
					writer.close();
					bw.close();
					Call call = buildUpdateTemplateCall(uploadGroupId , client , file);
					Type localVarReturnType = new TypeToken<String>(){}.getType();
					
					ApiResponse<String> resp = client.execute(call, localVarReturnType);
					Map<String,String> tpl = parseResponse(resp.getData());
					uploadedTpls.add(tpl);
				}
			}catch (Exception e) {
				if (e instanceof ApiException) {
					ApiException ae = (ApiException) e;
					if (ae.getCode() == 409) {
						exsitsFiles.add(filename);
					} else if (String.valueOf(ae.getCode()).startsWith("2")) {
						String responseBody = ae.getResponseBody();
						if(StringUtils.isNotBlank(responseBody) && responseBody.contains("errorResponse")) {
							failUploadFiles.add(filename);
						}
					} else {
						failUploadFiles.add(filename);
					}
				} else {
					failUploadFiles.add(filename);
				}
				logger.error("上传模板文件到服务器失败，文件名：" + filename, e);
			} finally {
				file.delete();
			}
			String errorMsg = "";
			if (exsitsFiles.size() > 0) {
				errorMsg += String.format("模板文件[%s]在服务器已存在，请先删除后再上传！", StringUtils.join(exsitsFiles.toArray(), ","));
			}
			if (failUploadFiles.size() > 0) {
				errorMsg += String.format("模板文件[%s]上传服务器失败，请检查模板文件格式是否正确，或联系系统管理员！",
						StringUtils.join(failUploadFiles.toArray(), ","));
			}
			String resultStr = JackSonUtils.transferToJson(uploadedTpls);
			if (StringUtils.isNotBlank(errorMsg)) {
				return new ResultDto<>(Constant.ResultCode.ERROR_CODE, errorMsg, resultStr);
			}
			return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "上传模板文件到服务器成功", resultStr);
		} else {
			return new ResultDto<>(Constant.ResultCode.ERROR_CODE, "上传模板文件到服务器失败,平台分类ID入参异常，无法查询平台分类信息", "上传模板文件到服务器失败,平台分类ID入参异常，无法查询平台分类信息");
		}
	}
	
	private Call buildUpdateTemplateCall(String groupId , ApiClient client , File file) throws ApiException {
		String localVarPath = "/process-groups/{id}/templates/upload".replaceAll("\\{format\\}","json")
		        .replaceAll("\\{" + "id" + "\\}", client.escapeString(groupId.toString()));
		 Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		 localVarHeaderParams.put("Content-Type", "multipart/form-data");
		 localVarHeaderParams.put("Accept", "application/xml");
		 Map<String, Object> localVarFormParams = new HashMap<String, Object>();
		 localVarFormParams.put("template", file);
		 String[] localVarAuthNames = new String[] {  };
		 List<Pair> localVarQueryParams = new ArrayList<Pair>();
		 Call call = client.buildCall(localVarPath, "POST", localVarQueryParams, false, localVarHeaderParams, localVarFormParams, localVarAuthNames, null);
		 return call;
	}
	
	private Map<String, String> parseResponse(String xml) throws DocumentException {
		Map<String, String> result = new HashMap<>();
		Document doc = null;
        doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();// 指向根节点
        Element tpl = root.element("template");
        String tplId = tpl.element("id").getText();
        String tplName = tpl.element("name").getText();
        String desp = tpl.element("description").getText();
        if(StringUtils.isNotBlank(tplId)) {
        	result.put("flowTplName", tplName);
        	result.put("flowTplId", tplId);
        	result.put("description", desp);
        }
        return result;
	}
	
//	public static void main(String[] args) {
//		List<Map<String ,String>> tpls = new ArrayList<>();
//		Map<String , String> tpl = new HashMap<>();
//		tpl.put("flowTplName", "tplName");
//		tpl.put("flowTplId", "tplId");
//		tpls.add(tpl);
//		System.out.println(JackSonUtils.transferToJson(tpls));
//		String strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><templateEntity><template encoding-version=\"1.3\"><description></description><groupId>238bd1cf-0176-1000-bd56-cfd6c9f3662e</groupId><id>46b0e0d2-85a3-4730-9327-b5d88cdee60d</id><name>testuploadtpl</name><timestamp>07/01/2021 11:19:11 CST</timestamp><uri>http://172.31.184.170:8080/nifi-api/templates/46b0e0d2-85a3-4730-9327-b5d88cdee60d</uri></template></templateEntity>";
//		Document doc = null;
//        try {
//            doc = DocumentHelper.parseText(strXML);
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
//        Element root = doc.getRootElement(); 指向根节点
//        Element tpl = root.element("template");
//        String tplId = tpl.element("id").getText();
//        System.out.println(tplId);
//	}
	
//	public static void main(String[] args) throws IOException {
//		EtlTplService service = new EtlTplService();
//		String serverUrl = "http://172.31.184.170/nifi-api";
//		String tplName = "testtesttest";
//		tplName = tplName + ".xml";
//		String tplContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
//				+ "<template encoding-version=\"1.3\">\n"
//				+ "    <description></description>\n"
//				+ "    <groupId>238bd1cf-0176-1000-bd56-cfd6c9f3662e</groupId>\n"
//				+ "    <name>testuploadtpl</name>\n"
//				+ "    <snippet>\n"
//				+ "        <processGroups>\n"
//				+ "            <id>e26352b7-19f7-3cb5-0000-000000000000</id>\n"
//				+ "            <parentGroupId>1ed4658e-5fd3-3815-0000-000000000000</parentGroupId>\n"
//				+ "            <position>\n"
//				+ "                <x>0.0</x>\n"
//				+ "                <y>0.0</y>\n"
//				+ "            </position>\n"
//				+ "            <comments></comments>\n"
//				+ "            <contents/>\n"
//				+ "            <name>testuploadtpl</name>\n"
//				+ "            <variables/>\n"
//				+ "        </processGroups>\n"
//				+ "    </snippet>\n"
//				+ "    <timestamp>07/01/2021 10:42:53 CST</timestamp>\n"
//				+ "</template>";
//		File file = new File(tplName);
//		if (!file.exists()) {
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				logger.error("创建模板临时文件异常", e);
//				throw e;
//			}
//		}
//		try (final FileWriter writer = new FileWriter(file); BufferedWriter bw = new BufferedWriter(writer)) {
//			bw.write(tplContent);
//			bw.flush();
//			writer.close();
//			bw.close();
//			
//			ApiClient client = new OAuthApiClient();
//			client.setBasePath(serverUrl);
//			client.addDefaultHeader("Content-Type", "multipart/form-data");
//			client.addDefaultHeader("Accept", "application/xml");
//			
//			AccessApi api = new AccessApi(client);
//			
//			client.setVerifyingSsl(false);
//			if (serverUrl.startsWith("https")) {
////				String token = api.createAccessToken(userName, password);
////				client.setAccessToken(token);
//			}
//			String groupId = "238bd1cf-0176-1000-bd56-cfd6c9f3662e";
//			
//			Call call = service.buildUpdateTemplateCall(groupId , client , file);
//			Type localVarReturnType = new TypeToken<String>(){}.getType();
//			
//			ApiResponse<String> resp = client.execute(call, localVarReturnType);
//			String tplId = service.parseResponse(resp.getData());
//			System.out.println(tplId);
//			
//		} catch (Exception e) {
//
//			if (e instanceof ApiException) {
//				ApiException ae = (ApiException) e;
//				if (ae.getCode() == 409) {
//					System.out.println("409");
//				} else if (String.valueOf(ae.getCode()).startsWith("2")) {
//					String responseBody = ae.getResponseBody();
//					if(StringUtils.isNotBlank(responseBody) && responseBody.contains("errorResponse")) {
//						System.out.println("fail start 2 contains errorResponse");
//					}
//				} else {
//					System.out.println("fail start 2");
//				}
//			} else {
//				System.out.println("fail");
//			}
//		} finally {
//			file.delete();
//		}
//	
//	}
}
