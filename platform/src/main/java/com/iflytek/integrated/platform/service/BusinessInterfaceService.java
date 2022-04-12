package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.QTBusinessInterface;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLExpressions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTPlatform.qTPlatform;
import static com.iflytek.integrated.platform.entity.QTSysConfig.qTSysConfig;
import static com.iflytek.integrated.platform.entity.QTType.qTType;
import static com.querydsl.sql.SQLExpressions.groupConcat;

/**
 * 对接接口配置
 * 
 * @author weihe9
 * @date 2020/12/13 20:40
 */
@Service
public class BusinessInterfaceService extends BaseService<TBusinessInterface, String, StringPath> {

	private static final Logger logger = LoggerFactory.getLogger(BusinessInterfaceService.class);

    @Autowired
    private RedisService redisService;

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
        //redis缓存信息获取
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.id.in(idList));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.mockStatus, mockStatus)
				.set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
				.where(qTBusinessInterface.id.in(idList)).execute();
		// 判断编辑是否成功
		if (idList.size() != size) {
			throw new RuntimeException("更改mock状态失败!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "更改mock状态成功!", new RedisDto(redisKeyDtoList).toString());
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
        //redis缓存信息获取
        ArrayList<Predicate> arr = new ArrayList<>();
        arr.add(qTBusinessInterface.id.in(idList));
        List<RedisKeyDto> redisKeyDtoList = redisService.getRedisKeyDtoList(arr);

		long size = sqlQueryFactory.update(qTBusinessInterface).set(qTBusinessInterface.status, status)
				.set(qTBusinessInterface.updatedTime, new Date()).set(qTBusinessInterface.updatedBy, loginUserName)
				.where(qTBusinessInterface.id.in(idList)).execute();
		// 判断编辑是否成功
		if (idList.size() != size) {
			throw new RuntimeException("启停用状态编辑失败!");
		}
		return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "更改接口配置状态成功!", new RedisDto(redisKeyDtoList).toString());
	}

	/**
	 * 根据相同条件删除接口配置数据
	 * 
	 * @param interfaceId
	 * @param requestSysconfigId
	 */
	public long delObjByCondition(String interfaceId, String requestSysconfigId) {
		long count = sqlQueryFactory.delete(qTBusinessInterface).where(qTBusinessInterface.requestInterfaceId
				.eq(interfaceId).and(qTBusinessInterface.requestSysconfigId.eq(requestSysconfigId))).execute();
		return count;
	}

	/**
	 * 根据相同条件查询所有接口配置数据
	 * 
	 * @param interfaceId
	 * @param requestSysconfigId
	 */
	public List<TBusinessInterface> getListByCondition(String interfaceId, String requestSysconfigId) {
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(interfaceId)) {
			list.add(qTBusinessInterface.requestInterfaceId.eq(interfaceId));
		}
		if (StringUtils.isNotBlank(requestSysconfigId)) {
			list.add(qTBusinessInterface.requestSysconfigId.eq(requestSysconfigId));
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
	 * 根据请求方系统接口ID获取接口配置信息
	 *
	 * @param interfaceId
	 */
	public TBusinessInterface getOneByInterfaceId(String interfaceId) {
		return sqlQueryFactory
				.select(Projections.bean(TBusinessInterface.class,qTBusinessInterface.requestInterfaceId))
				.from(qTBusinessInterface)
				.where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)).fetchFirst();
	}

	/**
	 * 获取接口配置信息列表
	 * 
	 * @param list
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public QueryResults<TBusinessInterface> getInterfaceConfigureList(ArrayList<Predicate> list, String requestedInterfaceName, Integer pageNo, Integer pageSize) {

//		QueryResults<TBusinessInterface> queryResults = sqlQueryFactory
//				.select(Projections.bean(TBusinessInterface.class, qTBusinessInterface.id,
//						qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId,
//						groupConcat(qTBusinessInterface.requestedSysconfigId.append("/")
//								.append(qTBusinessInterface.businessInterfaceName)).as("businessInterfaceName"),
//						qTBusinessInterface.mockStatus, qTBusinessInterface.status, qTBusinessInterface.createdBy,
//						qTBusinessInterface.createdTime, qTBusinessInterface.updatedBy, qTBusinessInterface.updatedTime,
//						qTSysConfig.versionId.as("versionId")))
//				.from(qTBusinessInterface)
//				.leftJoin(qTSysConfig).on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId))
//				.leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
//				.where(list.toArray(new Predicate[list.size()]))
//				.groupBy(qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId)
//				.orderBy(qTBusinessInterface.createdTime.desc()).limit(pageSize).offset((pageNo - 1) * pageSize)
//				.fetchResults();

		String q = "queryBusinessInterface";
		StringPath queryLabel = Expressions.stringPath(q);
		QTBusinessInterface businessInterface = new QTBusinessInterface(q);
		ArrayList<Predicate> interfaceNameList = new ArrayList<>();
		if (StringUtils.isNotEmpty(requestedInterfaceName)) {
			interfaceNameList.add(businessInterface.businessInterfaceName.like("%" + requestedInterfaceName + "%"));
		}
		SubQueryExpression query = SQLExpressions
				.select(qTBusinessInterface.id.min().as("id"), qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId,
						groupConcat(qTBusinessInterface.requestedSysconfigId.append("/")
								.append(qTBusinessInterface.businessInterfaceName)).as("BUSINESS_INTERFACE_NAME"),
						qTBusinessInterface.mockStatus.min().as("MOCK_STATUS"), qTBusinessInterface.status.min().as("status"), qTBusinessInterface.createdBy.min().as("CREATED_BY"),
						qTBusinessInterface.createdTime.max().as("CREATED_TIME"), qTBusinessInterface.updatedBy.min().as("UPDATED_BY"),
						qTBusinessInterface.updatedTime.max().as("UPDATED_TIME"))
				.from(qTBusinessInterface)
				.leftJoin(qTSysConfig).on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId))
				.leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
				.where(list.toArray(new Predicate[list.size()]))
				.groupBy(qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId);

		QueryResults<TBusinessInterface> queryResults = sqlQueryFactory
				.select(Projections.bean(TBusinessInterface.class, businessInterface.id, businessInterface.requestInterfaceId,
						businessInterface.requestSysconfigId, businessInterface.businessInterfaceName, businessInterface.mockStatus,
						businessInterface.status, businessInterface.createdBy, businessInterface.createdTime,
						businessInterface.updatedBy, businessInterface.updatedTime))
				.from(query, queryLabel)
				.where(interfaceNameList.toArray(new Predicate[interfaceNameList.size()]))
				.limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(businessInterface.createdTime.desc()).fetchResults();
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
		list.add(qTSysConfig.platformId.eq(platformId));
		List<TBusinessInterface> queryResults = sqlQueryFactory
				.select(Projections.bean(TBusinessInterface.class,
						// qTBusinessInterface.id,
						qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId))
				.from(qTBusinessInterface).leftJoin(qTSysConfig)
				.on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId))
				.where(list.toArray(new Predicate[list.size()]))
				.groupBy(qTBusinessInterface.requestInterfaceId, qTBusinessInterface.requestSysconfigId).fetch();
		return queryResults;
	}

	/**
	 * 根据三条件获取
	 * 
	 * @param interfaceId
	 * @param requestSysconfigId
	 * @return
	 */
	public List<TBusinessInterface> getTBusinessInterfaceList(String interfaceId, String requestSysconfigId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.requestInterfaceId.eq(interfaceId)
						.and(qTBusinessInterface.requestSysconfigId.eq(requestSysconfigId)))
				.orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
		return list;
	}

	/**
	 * 新增接口配置时根据条件判断是否存在该数据
	 * 
	 * @param projectId
	 * @param sysId
	 * @param interfaceId
	 * @return
	 */
	public List<TBusinessInterface> getBusinessInterfaceIsExist(String projectId, String sysId, String interfaceId) {
		ArrayList<Predicate> list = new ArrayList<>();

		if (StringUtils.isNotEmpty(interfaceId)) {
			list.add(qTBusinessInterface.requestInterfaceId.eq(interfaceId));
		}
		if (StringUtils.isNotEmpty(sysId)) {
			list.add(qTSysConfig.sysId.eq(sysId));
		}
		if (StringUtils.isNotEmpty(projectId)) {
			list.add(qTPlatform.projectId.in(projectId));
		}

		List<TBusinessInterface> rtnList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.leftJoin(qTSysConfig).on(qTSysConfig.id.eq(qTBusinessInterface.requestSysconfigId)).join(qTPlatform)
				.on(qTPlatform.id.eq(qTSysConfig.platformId)).where(list.toArray(new Predicate[list.size()])).fetch();
		return rtnList;
	}

	/**
	 * 根据系统id获取对接接口配置数据
	 * 
	 * @param sysId
	 * @return
	 */
	public List<TBusinessInterface> getListBySysId(String sysId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.join(qTSysConfig).on(qTBusinessInterface.requestSysconfigId.eq(qTSysConfig.id))
				.where(qTSysConfig.sysId.eq(sysId)).fetch();
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
	 * 根据请求方系统配置id获取对接接口配置数据
	 * 
	 * @param requestSysconfigId
	 * @return
	 */
	public List<TBusinessInterface> getListBySysConfigId(String requestSysconfigId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.requestSysconfigId.eq(requestSysconfigId)).fetch();
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
	
	public List<TBusinessInterface> getListByPlatforms(List<String> platformIds) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.leftJoin(qTSysConfig)
				.on((qTBusinessInterface.requestSysconfigId.eq(qTSysConfig.id)
						.or(qTBusinessInterface.requestedSysconfigId.eq(qTSysConfig.id))))
				.where(qTSysConfig.platformId.in(platformIds)).fetch();
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
		if (StringUtils.isNotBlank(businessInterface.getRequestInterfaceId())) {
			list.add(qTBusinessInterface.requestInterfaceId.eq(businessInterface.getRequestInterfaceId()));
		}
		if (StringUtils.isNotBlank(businessInterface.getRequestSysconfigId())) {
			list.add(qTBusinessInterface.requestSysconfigId.eq(businessInterface.getRequestSysconfigId()));
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

	public long selectByQIId(String QIId){
		return sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface).where(qTBusinessInterface.QIId.eq(QIId)).fetchCount();
	}

}
