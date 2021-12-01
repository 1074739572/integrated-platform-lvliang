package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;
import static com.iflytek.integrated.platform.entity.QTEtlFlow.qTEtlFlow;
import static com.iflytek.integrated.platform.entity.QTEtlGroup.qTEtlGroup;
import static com.iflytek.integrated.platform.entity.QTHospital.qTHospital;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTPlugin.qTPlugin;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTSysDriveLink.qTSysDriveLink;
import static com.iflytek.integrated.platform.entity.QTSysHospitalConfig.qTSysHospitalConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.ResourceDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.integrated.platform.entity.THospital;
import com.iflytek.integrated.platform.entity.TInterface;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import com.iflytek.integrated.platform.entity.TPlatform;
import com.iflytek.integrated.platform.entity.TPlugin;
import com.iflytek.integrated.platform.entity.TResource;
import com.iflytek.integrated.platform.entity.TSys;
import com.iflytek.integrated.platform.entity.TSysConfig;
import com.iflytek.integrated.platform.entity.TSysDriveLink;
import com.iflytek.integrated.platform.entity.TSysHospitalConfig;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.SimpleTemplate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "资源中心接口类")
@RequestMapping("/{version}/pt/resourceCenter")
public class ResourceCenterService {
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceCenterService.class);

	@Autowired
    protected SQLQueryFactory sqlQueryFactory;
	
	@Autowired
	protected BusinessInterfaceService biService;
	
    @GetMapping("/getAllResources")
    @ApiOperation(value = "获取全部资源")
	public ResultDto<TableData<TResource>> getAllResources(@RequestParam(value = "resourceName", required = false) String resourceName,@RequestParam(value = "type", required = false) String type,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
    	
    	Path[] fields = new Path[10];
    	StringPath idPath = Expressions.stringPath("id");
        StringPath typePath = Expressions.stringPath("type");
        StringPath typeNamePath = Expressions.stringPath("typeName");
        StringPath resourceNamePath = Expressions.stringPath("resourceName");
        NumberPath<Integer> intfTransCountPath = Expressions.numberPath(Integer.class, "intfTransCount");
        NumberPath<Integer> etlCountPath = Expressions.numberPath(Integer.class, "etlCount");
        NumberPath<Integer> sysDriverCountPath = Expressions.numberPath(Integer.class, "sysDriverCount");
        NumberPath<Integer> sysInftCountPath = Expressions.numberPath(Integer.class, "sysInftCount");
        NumberPath<Integer> pluginCountPath = Expressions.numberPath(Integer.class, "pluginCount");
        NumberPath<Integer> driverCountPath = Expressions.numberPath(Integer.class, "driverCount");
        fields[0]=idPath;
        fields[1]=typePath;
        fields[2]=typeNamePath;
        fields[3]=resourceNamePath;
        fields[4]=intfTransCountPath;
        fields[5]=etlCountPath;
        fields[6]=sysDriverCountPath;
        fields[7]=sysInftCountPath;
        fields[8]=pluginCountPath;
        fields[9]=driverCountPath;
        
        ArrayList<Predicate> list = new ArrayList<>();
    	if(StringUtils.isNotBlank(resourceName)) {
    		list.add(resourceNamePath.like(PlatformUtil.createFuzzyText(resourceName)));
    	}
    	
        
        StringPath intf = Expressions.stringPath("intf");
        StringPath etl = Expressions.stringPath("etl");
        
        StringPath sys = Expressions.stringPath("sys");
        StringPath plugin = Expressions.stringPath("plugin");
        StringPath drive = Expressions.stringPath("drive");
        StringPath proj = Expressions.stringPath("proj");
        
        StringPath all = Expressions.stringPath("all");
        
		SimpleExpression<String> sysType = Expressions.constantAs("1", typePath);
		SimpleExpression<String> sysTypeName = Expressions.constantAs("系统", typeNamePath);
		
		SubQueryExpression<Tuple> sysQuery = SQLExpressions.select(qTSys.id.as("id") , qTSys.sysName.as("resourceName") , sysType , sysTypeName,
				qTDrive.id.countDistinct().as("sysDriverCount") , qTInterface.id.countDistinct().as("sysInftCount") , 
				Expressions.constantAs(0, intfTransCountPath),Expressions.constantAs(0, etlCountPath),Expressions.constantAs(0, pluginCountPath),Expressions.constantAs(0, driverCountPath)).from(qTSys)
				.leftJoin(qTSysDriveLink).on(qTSysDriveLink.sysId.eq(qTSys.id)).leftJoin(qTDrive).on(qTDrive.id.eq(qTSysDriveLink.driveId))
				.leftJoin(qTInterface).on(qTInterface.sysId.eq(qTSys.id)).groupBy(qTSys.id , qTSys.sysName);
		
		SimpleExpression<String> pluginType = Expressions.constantAs("2", typePath);
		SimpleExpression<String> pluginTypeName = Expressions.constantAs("插件", typeNamePath);
		SubQueryExpression<Tuple> pluginQuery = SQLExpressions.select(qTPlugin.id.as("id") , qTPlugin.pluginName.as("resourceName") ,pluginType , pluginTypeName,
				qTPlugin.id.count().as("pluginCount") ,Expressions.constantAs(0, sysInftCountPath) , Expressions.constantAs(0, sysDriverCountPath) , 
				Expressions.constantAs(0, intfTransCountPath),Expressions.constantAs(0, etlCountPath),Expressions.constantAs(0, driverCountPath)).from(qTPlugin).groupBy(qTPlugin.id , qTPlugin.pluginName);
		
		SimpleExpression<String> driveType = Expressions.constantAs("3", typePath);
		SimpleExpression<String> driveTypeName = Expressions.constantAs("驱动", typeNamePath);
		SubQueryExpression<Tuple> driveQuery = SQLExpressions.select(qTDrive.id.as("id") , qTDrive.driveName.as("resourceName") ,driveType , driveTypeName,
				qTDrive.id.count().as("driverCount") ,Expressions.constantAs(0, sysInftCountPath) , Expressions.constantAs(0, sysDriverCountPath) , 
				Expressions.constantAs(0, intfTransCountPath),Expressions.constantAs(0, etlCountPath),Expressions.constantAs(0, pluginCountPath)).from(qTDrive).groupBy(qTDrive.id , qTDrive.driveName);
		
		NumberTemplate<Integer> intfTransCount = Expressions.numberTemplate(Integer.class, "ifnull(intf.intfTransCount, 0)");
        NumberTemplate<Integer> etlCount = Expressions.numberTemplate(Integer.class, "ifnull(etl.etlCount, 0)");
        SimpleTemplate<String> etlprojId = Expressions.template(String.class, "etl.PROJECT_ID");
		SimpleTemplate<String> intfprojId = Expressions.template(String.class, "intf.ID");
		SimpleTemplate<String> projName = Expressions.template(String.class, "intf.PROJECT_NAME");
		SimpleExpression<String> projType = Expressions.constantAs("4", typePath);
		SimpleExpression<String> projTypeName = Expressions.constantAs("项目", typeNamePath);
		
    	SubQueryExpression<Tuple> intfQuery = 
				SQLExpressions.select(qTProject.id , qTProject.projectName , qTBusinessInterface.requestInterfaceId.as("intfTransCount")).from(qTProject)
				.leftJoin(qTSysConfig).on(qTProject.id.eq(qTSysConfig.projectId))
				.leftJoin(qTBusinessInterface).on(qTBusinessInterface.requestSysconfigId.eq(qTSysConfig.id))
				.where(qTBusinessInterface.requestInterfaceId.isNotNull()).groupBy(qTProject.id , qTBusinessInterface.requestInterfaceId);
		SubQueryExpression<Tuple> etlQuery = SQLExpressions.select(qTEtlGroup.projectId , qTEtlFlow.id.countDistinct().as("etlCount"))
				.from(qTEtlGroup).join(qTEtlFlow).on(qTEtlFlow.groupId.eq(qTEtlGroup.id)).groupBy(qTEtlGroup.projectId);
		SubQueryExpression<Tuple> projQuery = SQLExpressions.select(intfprojId.as("id") , projName.as("resourceName"), intfTransCount.countDistinct().as("intfTransCount") , etlCount.as("etlCount") , projType, projTypeName,
				Expressions.constantAs(0, sysDriverCountPath) , Expressions.constantAs(0, sysInftCountPath),Expressions.constantAs(0, pluginCountPath),Expressions.constantAs(0, driverCountPath)).from(intfQuery , intf)
				.leftJoin(etlQuery , etl).on(etlprojId.eq(intfprojId)).groupBy(intfprojId);
		
		
		SubQueryExpression<TResource> unionQuery = null;
		switch (type) {
		case "1":
			unionQuery = SQLExpressions.select(Projections.bean(TResource.class, fields)).from(sysQuery , sys);
			break;
		case "2":
			unionQuery = SQLExpressions.select(Projections.bean(TResource.class, fields)).from(pluginQuery , plugin);
			break;
		case "3":
			unionQuery = SQLExpressions.select(Projections.bean(TResource.class, fields)).from(driveQuery , drive);
			break;
		case "4":
			unionQuery = SQLExpressions.select(Projections.bean(TResource.class, fields)).from(projQuery , proj);
			break;
		default:
			unionQuery = SQLExpressions.select(Projections.bean(TResource.class, fields)).unionAll(SQLExpressions.select(Projections.bean(TResource.class, fields)).from(sysQuery , sys),
					SQLExpressions.select(Projections.bean(TResource.class, fields)).from(pluginQuery , plugin) , 
					SQLExpressions.select(Projections.bean(TResource.class, fields)).from(driveQuery , drive) , 
					SQLExpressions.select(Projections.bean(TResource.class, fields)).from(projQuery , proj));
			break;
		}
		QueryResults<TResource> queryResults = sqlQueryFactory.select(Projections.bean(TResource.class, fields)).from(unionQuery , all)
				.where(list.toArray(new Predicate[list.size()]))
				.limit(pageSize).offset((pageNo - 1) * pageSize).fetchResults();
		
		TableData<TResource> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "获取资源成功!", tableData);
	}
    
    private void getResourcesByBizInterfaceIds(List<String> ids , StringBuilder sqlStringBuffer) {
		List<String> interfaceIds = new ArrayList<>();
		List<String> sysConfigIds = new ArrayList<>();
		List<String> pluginIds = new ArrayList<>();
		List<String> sysIds = new ArrayList<>();
		List<String> driverIds = new ArrayList<>();
		List<String> hospitalIds = new ArrayList<>();
		List<String> platformIds = new ArrayList<>();

		List<Path<?>> lists = new ArrayList<>();
		lists.addAll(Arrays.asList(qTBusinessInterface.all()));
		lists.add(qTSysConfig.platformId);
		for (String businessInterfaceId : ids) {
			String requestInterfaceId = sqlQueryFactory.select(qTBusinessInterface.requestInterfaceId).from(qTBusinessInterface)
					.where(qTBusinessInterface.id.eq(businessInterfaceId)).fetchFirst();
			if(StringUtils.isNotBlank(requestInterfaceId)) {
				List<TBusinessInterface> tBusinessInterfaces = sqlQueryFactory.select(Projections.bean(TBusinessInterface.class , lists.toArray(new Path[0]))).from(qTBusinessInterface)
						.join(qTSysConfig).on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId))
						.where(qTBusinessInterface.requestInterfaceId.eq(requestInterfaceId)).fetch();
				for (TBusinessInterface tBusinessInterface : tBusinessInterfaces) {
					interfaceIds.add(tBusinessInterface.getRequestInterfaceId());
					sysConfigIds.add(tBusinessInterface.getRequestSysconfigId());
					sysConfigIds.add(tBusinessInterface.getRequestedSysconfigId());
					pluginIds.add(tBusinessInterface.getPluginId());
					platformIds.add(tBusinessInterface.getPlatformId());
					sqlStringBuffer.append("REPLACE INTO `t_business_interface` (`ID`, `REQUEST_SYSCONFIG_ID`, " +
							"`REQUEST_INTERFACE_ID`, `REQUESTED_SYSCONFIG_ID`, `BUSINESS_INTERFACE_NAME`, `REQUEST_TYPE`, " +
							"`REQUEST_CONSTANT`, `INTERFACE_TYPE`, `PLUGIN_ID`, `IN_PARAM_FORMAT`, `IN_PARAM_SCHEMA`, `IN_PARAM_TEMPLATE_TYPE`, " +
							"`IN_PARAM_TEMPLATE`, `IN_PARAM_FORMAT_TYPE`, `OUT_PARAM_FORMAT`, `OUT_PARAM_SCHEMA`, `OUT_PARAM_TEMPLATE_TYPE`, " +
							"`OUT_PARAM_TEMPLATE`, `OUT_PARAM_FORMAT_TYPE`, `MOCK_TEMPLATE`, `MOCK_STATUS`, `STATUS`, `EXC_ERR_STATUS`, " +
							"`EXC_ERR_ORDER`, `MOCK_IS_USE`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `ASYNC_FLAG`, " +
							"`INTERFACE_SLOW_FLAG`) VALUES ('" + tBusinessInterface.getId() + "', '" + tBusinessInterface.getRequestSysconfigId() + "', '" + tBusinessInterface.getRequestInterfaceId() + "', " +
							"'" + tBusinessInterface.getRequestedSysconfigId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getBusinessInterfaceName())  + "', '" + tBusinessInterface.getRequestType() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getRequestConstant()) + "', " +
							"'" + tBusinessInterface.getInterfaceType() + "', '" + tBusinessInterface.getPluginId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getInParamFormat()) + "', '" + tBusinessInterface.getInParamSchema() + "', " +
							"" + tBusinessInterface.getInParamTemplateType() + ", '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getInParamTemplate()) + "', '" + tBusinessInterface.getInParamFormatType() + "', '" + tBusinessInterface.getOutParamFormat() + "', " +
							"'" + tBusinessInterface.getOutParamSchema() + "', " + tBusinessInterface.getOutParamTemplateType() + ", '" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getOutParamTemplate()) + "', '" + tBusinessInterface.getOutParamFormatType() + "', " +
							"'" + PlatformUtil.escapeSqlSingleQuotes(tBusinessInterface.getMockTemplate()) + "', '" + tBusinessInterface.getMockStatus() + "', '" + tBusinessInterface.getStatus() + "', '" + tBusinessInterface.getExcErrStatus() + "', " +
							"" + tBusinessInterface.getExcErrOrder() + ", " + tBusinessInterface.getMockIsUse() + ", 'admin', now() , 'admin', now() , " + tBusinessInterface.getAsyncFlag() + ", " + tBusinessInterface.getInterfaceSlowFlag() + "); \n");
					sqlStringBuffer.append("END_OF_SQL\n");
				}
			}
		}
		List<TPlatform> tPlatforms = sqlQueryFactory.select(qTPlatform).from(qTPlatform).where(qTPlatform.id.in(platformIds)).fetch();
		for(TPlatform tp : tPlatforms) {
			sqlStringBuffer.append("REPLACE INTO `t_platform` (`ID`, `PROJECT_ID`, `PLATFORM_NAME`, `PLATFORM_CODE`, " +
					"`PLATFORM_STATUS`, `PLATFORM_TYPE`, `ETL_SERVER_URL`, `ETL_USER`, `ETL_PWD`," +
					" `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES ('" + tp.getId() + "', 'newProjectId', '" + tp.getPlatformName() + "', '" + tp.getPlatformCode() + "', " +
					"'" + tp.getPlatformStatus() + "', '" + tp.getPlatformType() + "', '" + tp.getEtlServerUrl() + "', '" + tp.getEtlUser() + "', '" + tp.getEtlPwd() +
					"', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		
		List<TInterface> tInterfaces = sqlQueryFactory.select(qTInterface).from(qTInterface).where(qTInterface.id.in(interfaceIds)).fetch();
		for (TInterface tInterface : tInterfaces) {
			sqlStringBuffer.append("REPLACE INTO `t_interface` (`ID`, `SYS_ID`, `INTERFACE_NAME`, `TYPE_ID`, " +
					"`INTERFACE_URL`, `IN_PARAM_FORMAT`, `OUT_PARAM_FORMAT`, `PARAM_OUT_STATUS`, `PARAM_OUT_STATUS_SUCCESS`," +
					" `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `IN_PARAM_SCHEMA`, `IN_PARAM_FORMAT_TYPE`, " +
					"`OUT_PARAM_SCHEMA`, `OUT_PARAM_FORMAT_TYPE`) VALUES ('" + tInterface.getId() + "', '" + tInterface.getSysId() + "', '" + tInterface.getInterfaceName() + "', '" + tInterface.getTypeId() + "', " +
					"'" + tInterface.getInterfaceUrl() + "', '" + tInterface.getInParamFormat() + "', '" + tInterface.getOutParamFormat() + "', '" + tInterface.getParamOutStatus() + "', '" + tInterface.getParamOutStatusSuccess() +
					"', 'admin', now() , 'admin', now(), '" + tInterface.getInParamSchema() + "', '" + tInterface.getInParamFormatType() + "', '" + tInterface.getOutParamSchema() + "', '" + tInterface.getOutParamFormatType() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TInterfaceParam> tInterfaceParams = sqlQueryFactory.select(qTInterfaceParam).from(qTInterfaceParam).where(qTInterfaceParam.interfaceId.in(interfaceIds)).fetch();
		for (TInterfaceParam tInterfaceParam : tInterfaceParams) {
			sqlStringBuffer.append("REPLACE INTO `t_interface_param` (`ID`, `PARAM_NAME`, `PARAM_INSTRUCTION`, `INTERFACE_ID`, `PARAM_TYPE`, `PARAM_LENGTH`, `PARAM_IN_OUT`, `CREATED_BY`, `CREATED_TIME`, " +
					"`UPDATED_BY`, `UPDATED_TIME`) VALUES ('" + tInterfaceParam.getId() + "', '" + tInterfaceParam.getParamName() + "', '" + tInterfaceParam.getParamInstruction() + "', '" + tInterfaceParam.getInterfaceId() + "'," +
					" '" + tInterfaceParam.getParamType() + "', " + tInterfaceParam.getParamLength() + ", '" + tInterfaceParam.getParamInOut() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}

		List<TSysConfig> tSysConfigs = sqlQueryFactory.select(qTSysConfig).from(qTSysConfig).where(qTSysConfig.id.in(sysConfigIds)).fetch();
		for (TSysConfig sysConfig : tSysConfigs) {
			sysIds.add(sysConfig.getSysId());
			sqlStringBuffer.append("REPLACE INTO `t_sys_config` (`ID`, `PROJECT_ID`, `PLATFORM_ID`, `SYS_ID`, `SYS_CONFIG_TYPE`, `HOSPITAL_CONFIGS`, `VERSION_ID`, `CONNECTION_TYPE`, `ADDRESS_URL`, `ENDPOINT_URL`," +
					" `NAMESPACE_URL`, `DATABASE_NAME`, `DATABASE_URL`, `DATABASE_TYPE`, `DATABASE_DRIVER`, `DRIVER_URL`, `JSON_PARAMS`, `USER_NAME`, `USER_PASSWORD`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, " +
					"`INNER_IDX`) VALUES ('" + sysConfig.getId() + "', 'newProjectId', '"+ sysConfig.getPlatformId() +"', '" + sysConfig.getSysId() + "', " + sysConfig.getSysConfigType() + ", " +
					sysConfig.getHospitalConfigs() + ", '" + sysConfig.getVersionId() + "', '" + sysConfig.getConnectionType() + "', '" + sysConfig.getAddressUrl() + "', '" + sysConfig.getEndpointUrl() + "', " +
					"'" + sysConfig.getNamespaceUrl() + "', '" + sysConfig.getDatabaseName() + "', '" + sysConfig.getDatabaseUrl() + "', '" + sysConfig.getDatabaseType() + "', '" + sysConfig.getDatabaseDriver() + "', " +
					"'" + sysConfig.getDriverUrl() + "', '" + sysConfig.getJsonParams() + "', '" + sysConfig.getUserName() + "', '" + sysConfig.getUserPassword() + "','admin', now() , 'admin', now(), '" + sysConfig.getInnerIdx() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TPlugin> tPlugins = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.id.in(pluginIds)).fetch();
		for (TPlugin tPlugin : tPlugins) {
			sqlStringBuffer.append("REPLACE INTO `t_plugin` (`ID`, `PLUGIN_NAME`, `PLUGIN_CODE`, `TYPE_ID`, `PLUGIN_INSTRUCTION`, `PLUGIN_CONTENT`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `DEPENDENT_PATH`) " +
					"VALUES ('" + tPlugin.getId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tPlugin.getPluginName()) + "', '" + tPlugin.getPluginCode() + "', '" + tPlugin.getTypeId() + "', '" + tPlugin.getPluginInstruction() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tPlugin.getPluginContent()) + "', 'admin', now() , 'admin', now(), '" + tPlugin.getDependentPath() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TSys> tSyss = sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.id.in(sysIds)).fetch();
		for (TSys tSys : tSyss) {
			sqlStringBuffer.append("REPLACE INTO `t_sys` (`ID`, `SYS_NAME`, `SYS_CODE`, `IS_VALID`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSys.getId() + "', '" + tSys.getSysName() + "', '" + tSys.getSysCode() + "', '" + tSys.getIsValid() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TSysDriveLink> tSysDriveLinks = sqlQueryFactory.select(qTSysDriveLink).from(qTSysDriveLink).where(qTSysDriveLink.sysId.in(sysIds)).fetch();
		for (TSysDriveLink tSysDriveLink : tSysDriveLinks) {
			driverIds.add(tSysDriveLink.getDriveId());
			sqlStringBuffer.append("REPLACE INTO `t_sys_drive_link` (`ID`, `SYS_ID`, `DRIVE_ID`, `DRIVE_ORDER`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSysDriveLink.getId() + "', '" + tSysDriveLink.getSysId() + "', '" + tSysDriveLink.getDriveId() + "', " + tSysDriveLink.getDriveOrder() + ", 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TDrive> tDrives = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.in(driverIds)).fetch();
		for (TDrive tDrive : tDrives) {
			sqlStringBuffer.append("REPLACE INTO `t_drive` (`ID`, `DRIVE_NAME`, `DRIVE_CODE`, `TYPE_ID`, `DRIVE_INSTRUCTION`, `DRIVE_CONTENT`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `DRIVE_CALL_TYPE`, `DEPENDENT_PATH`) VALUES " +
					"('" + tDrive.getId() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tDrive.getDriveName()) + "', '" + tDrive.getDriveCode() + "', '" + tDrive.getTypeId() + "', '" + tDrive.getDriveInstruction() + "', '" + PlatformUtil.escapeSqlSingleQuotes(tDrive.getDriveContent()) + "', 'admin', now() , 'admin', now(), '" + tDrive.getDriveCallType() + "', '" + tDrive.getDependentPath() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<TSysHospitalConfig> tSysHospitalConfigs = sqlQueryFactory.select(qTSysHospitalConfig).from(qTSysHospitalConfig).where(qTSysHospitalConfig.sysConfigId.in(sysConfigIds)).fetch();
		for (TSysHospitalConfig tSysHospitalConfig : tSysHospitalConfigs) {
			hospitalIds.add(tSysHospitalConfig.getHospitalId());
			sqlStringBuffer.append("REPLACE INTO `t_sys_hospital_config` (`id`, `sys_config_id`, `hospital_id`, `hospital_code`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSysHospitalConfig.getId() + "', '" + tSysHospitalConfig.getSysConfigId() + "', '" + tSysHospitalConfig.getHospitalId() + "', '" + tSysHospitalConfig.getHospitalCode() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<THospital> tHospitals = sqlQueryFactory.select(qTHospital).from(qTHospital).where(qTHospital.id.in(hospitalIds)).fetch();
		for (THospital tHospital : tHospitals) {
			sqlStringBuffer.append("REPLACE INTO `t_hospital` (`ID`, `HOSPITAL_NAME`, `STATUS`, `AREA_ID`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tHospital.getId() + "', '" + tHospital.getHospitalName() + "', '" + tHospital.getStatus() + "', '" + tHospital.getAreaId() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
	}
	
	private void getResourcesBySys(String sysId , List<String> driverIds , List<String> interfaceIds , StringBuilder sqlStringBuffer) {
		TSys tSys = sqlQueryFactory.select(qTSys).from(qTSys).where(qTSys.id.eq(sysId)).fetchOne();
		if (tSys != null) {
			sqlStringBuffer.append("REPLACE INTO `t_sys` (`ID`, `SYS_NAME`, `SYS_CODE`, `IS_VALID`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSys.getId() + "', '" + tSys.getSysName() + "', '" + tSys.getSysCode() + "', '" + tSys.getIsValid() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		List<Predicate> driveLinkFilters = new ArrayList<>();
		driveLinkFilters.add(qTSysDriveLink.sysId.eq(sysId));
		if(driverIds != null && driverIds.size() > 0) {
			driveLinkFilters.add(qTSysDriveLink.driveId.in(driverIds));
		}
		List<TSysDriveLink> tSysDriveLinks = sqlQueryFactory.select(qTSysDriveLink).from(qTSysDriveLink).where(driveLinkFilters.toArray(new Predicate[driveLinkFilters.size()])).fetch();
		List<String> driveIds = new ArrayList<>();
		for (TSysDriveLink tSysDriveLink : tSysDriveLinks) {
//			driveIds.add(tSysDriveLink.getDriveId());
			sqlStringBuffer.append("REPLACE INTO `t_sys_drive_link` (`ID`, `SYS_ID`, `DRIVE_ID`, `DRIVE_ORDER`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`) VALUES " +
					"('" + tSysDriveLink.getId() + "', '" + tSysDriveLink.getSysId() + "', '" + tSysDriveLink.getDriveId() + "', " + tSysDriveLink.getDriveOrder() + ", 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
			driveIds.add(tSysDriveLink.getDriveId());
		}
		List<TDrive> tDrives = sqlQueryFactory.select(qTDrive).from(qTDrive)
				.where(qTDrive.id.in(driveIds)).fetch();
		for (TDrive tDrive : tDrives) {
			sqlStringBuffer.append("REPLACE INTO `t_drive` (`ID`, `DRIVE_NAME`, `DRIVE_CODE`, `TYPE_ID`, `DRIVE_INSTRUCTION`, `DRIVE_CONTENT`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `DRIVE_CALL_TYPE`, `DEPENDENT_PATH`) VALUES " +
					"('" + tDrive.getId() + "', '" + tDrive.getDriveName() + "', '" + tDrive.getDriveCode() + "', '" + tDrive.getTypeId() + "', '" + tDrive.getDriveInstruction() + "', '" + tDrive.getDriveContent() + "', 'admin', now() , 'admin', now(), '" + tDrive.getDriveCallType() + "', '" + tDrive.getDependentPath() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
		
		List<Predicate> sysInftFilters = new ArrayList<>();
		sysInftFilters.add(qTInterface.sysId.eq(sysId));
		if(interfaceIds != null && interfaceIds.size()> 0) {
			sysInftFilters.add(qTInterface.id.in(interfaceIds));
		}
		List<TInterface> tInterfaces = sqlQueryFactory.select(qTInterface).from(qTInterface).where(sysInftFilters.toArray(new Predicate[sysInftFilters.size()])).fetch();
		List<String> intfIds = new ArrayList<>();
		for (TInterface tInterface : tInterfaces) {
			sqlStringBuffer.append("REPLACE INTO `t_interface` (`ID`, `SYS_ID`, `INTERFACE_NAME`, `TYPE_ID`, " +
					"`INTERFACE_URL`, `IN_PARAM_FORMAT`, `OUT_PARAM_FORMAT`, `PARAM_OUT_STATUS`, `PARAM_OUT_STATUS_SUCCESS`," +
					" `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `IN_PARAM_SCHEMA`, `IN_PARAM_FORMAT_TYPE`, " +
					"`OUT_PARAM_SCHEMA`, `OUT_PARAM_FORMAT_TYPE`) VALUES ('" + tInterface.getId() + "', '" + tInterface.getSysId() + "', '" + tInterface.getInterfaceName() + "', '" + tInterface.getTypeId() + "', " +
					"'" + tInterface.getInterfaceUrl() + "', '" + tInterface.getInParamFormat() + "', '" + tInterface.getOutParamFormat() + "', '" + tInterface.getParamOutStatus() + "', '" + tInterface.getParamOutStatusSuccess() +
					"', 'admin', now() , 'admin', now(), '" + tInterface.getInParamSchema() + "', '" + tInterface.getInParamFormatType() + "', '" + tInterface.getOutParamSchema() + "', '" + tInterface.getOutParamFormatType() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
			intfIds.add(tInterface.getId());
		}
		List<TInterfaceParam> tInterfaceParams = sqlQueryFactory.select(qTInterfaceParam).from(qTInterfaceParam).where(qTInterfaceParam.interfaceId.in(intfIds)).fetch();
		for (TInterfaceParam tInterfaceParam : tInterfaceParams) {
			sqlStringBuffer.append("REPLACE INTO `t_interface_param` (`ID`, `PARAM_NAME`, `PARAM_INSTRUCTION`, `INTERFACE_ID`, `PARAM_TYPE`, `PARAM_LENGTH`, `PARAM_IN_OUT`, `CREATED_BY`, `CREATED_TIME`, " +
					"`UPDATED_BY`, `UPDATED_TIME`) VALUES ('" + tInterfaceParam.getId() + "', '" + tInterfaceParam.getParamName() + "', '" + tInterfaceParam.getParamInstruction() + "', '" + tInterfaceParam.getInterfaceId() + "'," +
					" '" + tInterfaceParam.getParamType() + "', " + tInterfaceParam.getParamLength() + ", '" + tInterfaceParam.getParamInOut() + "', 'admin', now() , 'admin', now());\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
	}
	
	private void getResourcesByDriver(List<String> driverIds , StringBuilder sqlStringBuffer) {
		SQLQuery<TDrive> sqlQuery = null;
		if(driverIds != null && driverIds.size() > 0) {
			sqlQuery = sqlQueryFactory.select(qTDrive).from(qTDrive).where(qTDrive.id.in(driverIds));
		}else {
			sqlQuery = sqlQueryFactory.select(qTDrive).from(qTDrive);
		}
		List<TDrive> tDrives = sqlQuery.fetch();
		for (TDrive tDrive : tDrives) {
			sqlStringBuffer.append("REPLACE INTO `t_drive` (`ID`, `DRIVE_NAME`, `DRIVE_CODE`, `TYPE_ID`, `DRIVE_INSTRUCTION`, `DRIVE_CONTENT`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `DRIVE_CALL_TYPE`, `DEPENDENT_PATH`) VALUES " +
					"('" + tDrive.getId() + "', '" + tDrive.getDriveName() + "', '" + tDrive.getDriveCode() + "', '" + tDrive.getTypeId() + "', '" + tDrive.getDriveInstruction() + "', '" + tDrive.getDriveContent() + "', 'admin', now() , 'admin', now(), '" + tDrive.getDriveCallType() + "', '" + tDrive.getDependentPath() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
	}
	
	private void getResourcesByPlugin(List<String> pluginIds , StringBuilder sqlStringBuffer) {
		SQLQuery<TPlugin> sqlQuery = null;
		if(pluginIds != null && pluginIds.size() > 0) {
			sqlQuery = sqlQueryFactory.select(qTPlugin).from(qTPlugin).where(qTPlugin.id.in(pluginIds));
		}else {
			sqlQuery = sqlQueryFactory.select(qTPlugin).from(qTPlugin);
		}
		List<TPlugin> tPlugins = sqlQuery.fetch();
		for (TPlugin tPlugin : tPlugins) {
			sqlStringBuffer.append("REPLACE INTO `t_plugin` (`ID`, `PLUGIN_NAME`, `PLUGIN_CODE`, `TYPE_ID`, `PLUGIN_INSTRUCTION`, `PLUGIN_CONTENT`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`, `DEPENDENT_PATH`) " +
					"VALUES ('" + tPlugin.getId() + "', '" + tPlugin.getPluginName() + "', '" + tPlugin.getPluginCode() + "', '" + tPlugin.getTypeId() + "', '" + tPlugin.getPluginInstruction() + "', '" + tPlugin.getPluginContent() + "', 'admin', now() , 'admin', now(), '" + tPlugin.getDependentPath() + "');\n");
			sqlStringBuffer.append("END_OF_SQL\n");
		}
	}

	
	@ApiOperation(value = "导出资源中心模块配置")
	@PostMapping("/downloadResources")
	public void downloadResources(@RequestBody ResourceDto resourceDto,	HttpServletResponse response) {
		StringBuilder sqlStringBuffer = new StringBuilder();
		String type = resourceDto.getType();
		List<String> ids = resourceDto.getIds();
		String sqlFileNamePrifix = "sql_";
		switch (type) {
		case "1":
			String sysId = resourceDto.getSysId();
			List<String> sysDriveIds = resourceDto.getSysDriveIds();
			List<String> sysIntfIds = resourceDto.getSysIntfIds();
			this.getResourcesBySys(sysId, sysDriveIds, sysIntfIds, sqlStringBuffer);
			sqlFileNamePrifix = "system_";
			break;
		case "2":
			this.getResourcesByDriver(ids, sqlStringBuffer);
			sqlFileNamePrifix = "driver_";
			break;
		case "3":
			this.getResourcesByPlugin(ids, sqlStringBuffer);
			sqlFileNamePrifix = "plugin_";
			break;
		case "4":
			String platformId = resourceDto.getPlatformId();
			List<TBusinessInterface> bis = biService.getListByPlatform(platformId);
			List<String> biids = new ArrayList<>();
			bis.forEach(bi->{
				biids.add(bi.getId());
			});
			this.getResourcesByBizInterfaceIds(biids, sqlStringBuffer);
			sqlFileNamePrifix = "project_";
			break;
		default:
			break;
		}

		String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String sqlName = sqlFileNamePrifix + dateStr + ".sql";
		try {
			response.setContentType("application/x-msdownload");
			response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(sqlFileNamePrifix + dateStr + ".zip", "utf-8"));

			ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
			BufferedOutputStream bos = new BufferedOutputStream(zos);

			String fileName = sqlName; // 每个zip文件名
			byte[] file = sqlStringBuffer.toString().getBytes(StandardCharsets.UTF_8); // 这个zip文件的字节

			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(file));
			zos.putNextEntry(new ZipEntry(fileName));

			int len = 0;
			byte[] buf = new byte[10 * 1024];
			while ((len = bis.read(buf, 0, buf.length)) != -1) {
				bos.write(buf, 0, len);
			}
			bis.close();
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@ApiOperation(value = "根据系统ID获取驱动")
	@GetMapping("/getPageDriveBySysId")
	public ResultDto<TableData<TDrive>> getPageDriveBySysId(
			@ApiParam(value = "系统id") @RequestParam(value = "sysId", required = true) String sysId,
			@ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		QueryResults<TDrive> queryResults = sqlQueryFactory.select(qTDrive).from(qTSysDriveLink).leftJoin(qTDrive)
				.on(qTDrive.id.eq(qTSysDriveLink.driveId)).where(qTSysDriveLink.sysId.eq(sysId))
				.limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTDrive.createdTime.desc()).fetchResults();
		// 分页
		TableData<TDrive> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "根据系统获取驱动成功", tableData);
	}
}