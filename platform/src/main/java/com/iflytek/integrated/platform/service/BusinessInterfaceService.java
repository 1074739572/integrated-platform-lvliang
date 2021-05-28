package com.iflytek.integrated.platform.service;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTHospitalVendorLink.qTHospitalVendorLink;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.querydsl.sql.SQLExpressions.groupConcat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.iflytek.integrated.platform.entity.THospitalVendorLink;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;

/**
 * 对接接口配置
 * 
 * @author weihe9
 * @date 2020/12/13 20:40
 */
@Service
public class BusinessInterfaceService extends BaseService<TBusinessInterface, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(BusinessInterfaceService.class);

	public BusinessInterfaceService() {
		super(qTBusinessInterface, qTBusinessInterface.id);
	}

	/**
	 * 根据相同条件更改接口配置mock状态
	 * 
	 * @param id
	 * @param mockStatus
	 * @param loginUserName
	 */
	public ResultDto<String> updateMockStatus(String id, String mockStatus, String loginUserName) {
		// 获取多接口，多个接口的id集合
		List<String> idList = busInterfaceIds(id);
		long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockStatus, mockStatus)
				.set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
				.where(qTBusinessInterface.id.in(idList)).execute();
		// 判断编辑是否成功
		if (idList.size() != size) {
			throw new RuntimeException("更改mock状态失败!");
		}
		String rtnStr = "";
		for (String idStr : idList) {
			rtnStr += idStr + ",";
		}
		rtnStr = StringUtils.isBlank(rtnStr) ? "" : rtnStr.substring(0, rtnStr.length() - 1);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "更改mock状态成功!", new RedisDto(rtnStr).toString());
	}

	/**
	 * 根据相同条件更改接口配置启停用状态
	 * 
	 * @param id
	 * @param status
	 * @param loginUserName
	 */
	public ResultDto updateStatus(String id, String status, String loginUserName) {
		// 获取多接口，多个接口的id集合
		List<String> idList = busInterfaceIds(id);
		long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.status, status)
				.set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
				.where(qTBusinessInterface.id.in(idList)).execute();
		// 判断编辑是否成功
		if (idList.size() != size) {
			throw new RuntimeException("启停用状态编辑失败!");
		}
		String rtnStr = "";
		for (String idStr : idList) {
			rtnStr += idStr + ",";
		}
		rtnStr = StringUtils.isBlank(rtnStr) ? "" : rtnStr.substring(0, rtnStr.length() - 1);
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "更改接口配置状态成功!", new RedisDto(rtnStr).toString());
	}

	/**
	 * 根据相同条件删除接口配置数据
	 * 
	 * @param productFunctionLinkId
	 * @param interfaceId
	 * @param vendorConfigId
	 */
	public long delObjByCondition(String productFunctionLinkId, String interfaceId, String vendorConfigId) {
		long count = sqlQueryFactory.delete(qTBusinessInterface)
				.where(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId)
						.and(qTBusinessInterface.interfaceId.eq(interfaceId)
								.and(qTBusinessInterface.vendorConfigId.eq(vendorConfigId))))
				.execute();
		return count;
	}

	/**
	 * 根据相同条件查询所有接口配置数据
	 * 
	 * @param productFunctionLinkId
	 * @param interfaceId
	 * @param vendorConfigId
	 */
	public List<TBusinessInterface> getListByCondition(String productFunctionLinkId, String interfaceId,
			String vendorConfigId) {
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(productFunctionLinkId)) {
			list.add(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId));
		}
		if (StringUtils.isNotBlank(interfaceId)) {
			list.add(qTBusinessInterface.interfaceId.eq(interfaceId));
		}
		if (StringUtils.isNotBlank(vendorConfigId)) {
			list.add(qTBusinessInterface.vendorConfigId.eq(vendorConfigId));
		}
		List<TBusinessInterface> tbiList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(list.toArray(new Predicate[list.size()])).fetch();
		return tbiList;
	}

	/**
	 * 保存mock模板
	 * 
	 * @param id
	 * @param mockTemplate
	 * @param mockIsUse
	 */
	public Long saveMockTemplate(String id, String mockTemplate, Integer mockIsUse, String loginUserName) {
		return sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockTemplate, mockTemplate)
				.set(qTBusinessInterface.mockIsUse, mockIsUse).set(qTBusinessInterface.updatedTime, new Date())
				.set(qTBusinessInterface.updatedBy, loginUserName).where(qTBusinessInterface.id.eq(id)).execute();
	}

	/**
	 * 根据标准接口id获取接口配置id(获取标准接口详情使用)
	 * 
	 * @param interfaceId
	 */
	public List<TBusinessInterface> getListByInterfaceId(String interfaceId) {
		return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetch();
	}

	/**
	 * 获取接口配置信息列表
	 * 
	 * @param platformId
	 * @param status
	 * @param mockStatus
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public QueryResults<TBusinessInterface> getInterfaceConfigureList(String platformId, String status,
			String mockStatus, Integer pageNo, Integer pageSize) {
		ArrayList<Predicate> list = new ArrayList<>();
		list.add(qTSysConfig.platformId.eq(platformId));
		if (StringUtils.isNotEmpty(status)) {
			list.add(qTBusinessInterface.status.eq(status));
		}
		if (StringUtils.isNotEmpty(mockStatus)) {
			list.add(qTBusinessInterface.mockStatus.eq(mockStatus));
		}
		QueryResults<TBusinessInterface> queryResults = sqlQueryFactory
				.select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id,
						qTBusinessInterface.requestInterfaceId,qTBusinessInterface.requestSysconfigId,
						groupConcat(qTBusinessInterface.requestedSysconfigId.append("/").append(qTBusinessInterface.businessInterfaceName)).as("businessInterfaceName"),
						qTBusinessInterface.requestType, qTBusinessInterface.requestConstant,
						qTBusinessInterface.interfaceType, qTBusinessInterface.pluginId,
						qTBusinessInterface.inParamFormat, qTBusinessInterface.inParamSchema,
						qTBusinessInterface.inParamTemplate, qTBusinessInterface.inParamFormatType,
						qTBusinessInterface.outParamFormat, qTBusinessInterface.outParamSchema,
						qTBusinessInterface.outParamTemplate, qTBusinessInterface.outParamFormatType,
						qTBusinessInterface.mockStatus, qTBusinessInterface.status, qTBusinessInterface.createdBy,
						qTBusinessInterface.createdTime, qTBusinessInterface.updatedBy, qTBusinessInterface.updatedTime,
						qTSysConfig.versionId.as("versionId")))
				.from(qTBusinessInterface).leftJoin(qTSysConfig)
				.on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId))
				.where(list.toArray(new Predicate[list.size()]))
				.groupBy(qTBusinessInterface.requestInterfaceId,
						qTBusinessInterface.requestSysconfigId)
				.orderBy(qTBusinessInterface.createdTime.desc()).limit(pageSize).offset((pageNo - 1) * pageSize)
				.fetchResults();
		return queryResults;
	}

	/**
	 * 根据平台id获取接口配置信息
	 * 
	 * @param platformId
	 * @return
	 */
	public List<TBusinessInterface> getInterfaceConfigureList(String platformId) {
		ArrayList<Predicate> list = new ArrayList<>();
		list.add(qTVendorConfig.platformId.eq(platformId));
		List<TBusinessInterface> queryResults = sqlQueryFactory
				.select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id,
						qTBusinessInterface.productFunctionLinkId, qTBusinessInterface.interfaceId,
						qTBusinessInterface.vendorConfigId))
				.from(qTBusinessInterface).leftJoin(qTVendorConfig)
				.on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId))
				.where(list.toArray(new Predicate[list.size()])).groupBy(qTBusinessInterface.productFunctionLinkId,
						qTBusinessInterface.interfaceId, qTBusinessInterface.vendorConfigId)
				.fetch();
		return queryResults;
	}

	/**
	 * 根据三条件获取
	 * 
	 * @param productFunctionLinkId
	 * @param interfaceId
	 * @param vendorConfigId
	 * @return
	 */
	public List<TBusinessInterface> getTBusinessInterfaceList(String productFunctionLinkId, String interfaceId,
			String vendorConfigId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId)
						.and(qTBusinessInterface.interfaceId.eq(interfaceId)
								.and(qTBusinessInterface.vendorConfigId.eq(vendorConfigId))))
				.orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
		return list;
	}

	/**
	 * 新增接口配置时根据条件判断是否存在该数据
	 * 
	 * @param thvlList
	 * @param projectId
	 * @param productId
	 * @param interfaceId
	 * @return
	 */
	public List<TBusinessInterface> getBusinessInterfaceIsExist(List<THospitalVendorLink> thvlList, String projectId,
			String productId, String interfaceId) {
		ArrayList<Predicate> list = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(thvlList)) {
			List<String> hospitalIds = new ArrayList<>();
			for (THospitalVendorLink obj : thvlList) {
				hospitalIds.add(obj.getHospitalId());
			}
			list.add(qTHospitalVendorLink.hospitalId.in(hospitalIds));
		}
		if (StringUtils.isNotEmpty(interfaceId)) {
			list.add(qTBusinessInterface.interfaceId.eq(interfaceId));
		}
		if (StringUtils.isNotEmpty(productId)) {
			list.add(qTProductFunctionLink.productId.eq(productId));
		}
		if (StringUtils.isNotEmpty(projectId)) {
			list.add(qTPlatform.projectId.in(projectId));
		}

		List<TBusinessInterface> rtnList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.join(qTHospitalVendorLink)
				.on(qTHospitalVendorLink.vendorConfigId.eq(qTBusinessInterface.vendorConfigId))
				.join(qTProductFunctionLink).on(qTProductFunctionLink.id.eq(qTBusinessInterface.productFunctionLinkId))
				.join(qTVendorConfig).on(qTVendorConfig.id.eq(qTBusinessInterface.vendorConfigId)).join(qTPlatform)
				.on(qTPlatform.id.eq(qTVendorConfig.platformId)).where(list.toArray(new Predicate[list.size()]))
				.fetch();
		return rtnList;
	}

	/**
	 * 根据产品功能关联表id获取对接接口配置数据
	 * 
	 * @param productFunctionLinkId
	 * @return
	 */
	public List<TBusinessInterface> getListBySysId(String sysId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.productFunctionLinkId.eq(productFunctionLinkId)).fetch();
		return list;
	}

	/**
	 * 根据插件表id获取对接接口配置数据
	 * 
	 * @param pluginId
	 * @return
	 */
	public List<TBusinessInterface> getListByPluginId(String pluginId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.pluginId.eq(pluginId)).fetch();
		return list;
	}

	/**
	 * 根据厂商配置id获取对接接口配置数据
	 * 
	 * @param vendorConfigId
	 * @return
	 */
	public List<TBusinessInterface> getListByVendorConfigId(String vendorConfigId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.vendorConfigId.eq(vendorConfigId)).fetch();
		return list;
	}

	/**
	 * 获取平台下的所有接口配置信息
	 * 
	 * @param platformId
	 */
	public List<TBusinessInterface> getListByPlatform(String platformId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.leftJoin(qTSysConfig)
				.on((qTBusinessInterface.requestSysconfigId.eq(qTSysConfig.id)
						.or(qTBusinessInterface.requestedSysconfigId.eq(qTSysConfig.id))))
				.where(qTSysConfig.platformId.eq(platformId)).fetch();
		return list;
	}

	/**
	 * 根据id获取多接口配置下全部接口
	 * 
	 * @param id
	 * @return
	 */
	public List<TBusinessInterface> busInterfaces(String id) {
		TBusinessInterface businessInterface = getOne(id);
		if (businessInterface == null) {
			throw new RuntimeException("没有找到接口配置");
		}
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(businessInterface.getInterfaceId())) {
			list.add(qTBusinessInterface.interfaceId.eq(businessInterface.getInterfaceId()));
		}
		if (StringUtils.isNotBlank(businessInterface.getProductFunctionLinkId())) {
			list.add(qTBusinessInterface.productFunctionLinkId.eq(businessInterface.getProductFunctionLinkId()));
		}
		if (StringUtils.isNotBlank(businessInterface.getVendorConfigId())) {
			list.add(qTBusinessInterface.vendorConfigId.eq(businessInterface.getVendorConfigId()));
		}
		List<TBusinessInterface> interfaces = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(list.toArray(new Predicate[list.size()])).orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
		if (CollectionUtils.isEmpty(interfaces)) {
			throw new RuntimeException("没有找到多接口配置集合");
		}
		return interfaces;
	}

	/**
	 * 根据id，获取id的list
	 * 
	 * @return
	 */
	private List<String> busInterfaceIds(String id) {
		// 获取id集合
		List<TBusinessInterface> interfaces = busInterfaces(id);
		return interfaces.stream().map(TBusinessInterface::getId).collect(Collectors.toList());
	}

}
