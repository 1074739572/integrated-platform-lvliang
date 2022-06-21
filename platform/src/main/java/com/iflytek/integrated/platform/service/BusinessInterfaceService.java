package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.common.RedisService;
import com.iflytek.integrated.platform.dto.RedisDto;
import com.iflytek.integrated.platform.dto.RedisKeyDto;
import com.iflytek.integrated.platform.entity.TBusinessInterface;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.iflytek.integrated.platform.entity.QTBusinessInterface.qTBusinessInterface;
import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTSys.qTSys;
import static com.iflytek.integrated.platform.entity.QTSysRegistry.qTSysRegistry;
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

	@Value("${server.db}")
	private String dbType;

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
	 */
	public long delObjByCondition(String interfaceId) {
		long count = sqlQueryFactory.delete(qTBusinessInterface).where(qTBusinessInterface.requestInterfaceId
				.eq(interfaceId)).execute();
		return count;
	}

	/**
	 * 根据相同条件查询所有接口配置数据
	 * 
	 * @param interfaceId
	 */
	public List<TBusinessInterface> getListByCondition(String interfaceId) {
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(interfaceId)) {
			list.add(qTBusinessInterface.requestInterfaceId.eq(interfaceId));
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
	 * 获取集成配置信息列表
	 * 
	 * @param list
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public QueryResults<TBusinessInterface> getInterfaceConfigureList(ArrayList<Predicate> list, Integer pageNo, Integer pageSize) {
		StringTemplate template = Expressions.stringTemplate("concat(string_agg ( concat(concat({0},'/' ::TEXT),{1}), ',' :: TEXT ))",qTSys.sysName,qTBusinessInterface.businessInterfaceName);
		QueryResults<TBusinessInterface> queryResults = sqlQueryFactory
				.select(Projections.bean(TBusinessInterface.class,qTBusinessInterface.id.min().as("id"),qTType.typeName.min().as(qTType.typeName),
						qTBusinessInterface.requestInterfaceId,
						qTInterface.interfaceName.min().as(qTInterface.interfaceName),
						template.as(qTBusinessInterface.businessInterfaceName),
						qTBusinessInterface.mockStatus.min().as(qTBusinessInterface.mockStatus),
						qTBusinessInterface.status.min().as("status"),
						qTBusinessInterface.createdBy.min().as( qTBusinessInterface.createdBy),
						qTBusinessInterface.createdTime.max().as(qTBusinessInterface.createdTime),
						qTBusinessInterface.updatedBy.min().as(qTBusinessInterface.updatedBy),
						qTBusinessInterface.updatedTime.max().as(qTBusinessInterface.updatedTime)))
				.from(qTBusinessInterface)
				.leftJoin(qTSysRegistry).on(qTSysRegistry.id.eq(qTBusinessInterface.sysRegistryId))
				.leftJoin(qTType).on(qTType.id.eq(qTBusinessInterface.interfaceType))
				.leftJoin(qTSys).on(qTSys.id.eq(qTSysRegistry.sysId))
				.leftJoin(qTInterface).on(qTInterface.id.eq(qTBusinessInterface.requestInterfaceId))
				.where(list.toArray(new Predicate[list.size()]))
				.groupBy(qTBusinessInterface.requestInterfaceId)
				.limit(pageSize).offset((pageNo - 1) * pageSize)
				.orderBy(qTBusinessInterface.createdTime.as("createdTime").desc()).fetchResults();;
		return queryResults;
	}


	/**
	 * 根据三条件获取
	 * 
	 * @param interfaceId
	 * @return
	 */
	public List<TBusinessInterface> getTBusinessInterfaceList(String interfaceId) {
		List<TBusinessInterface> list = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(qTBusinessInterface.requestInterfaceId.eq(interfaceId))
				.orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
		return list;
	}

	/**
	 * 新增接口配置时根据条件判断是否存在该数据
	 * 
	 * @param interfaceId
	 * @return
	 */
	public List<TBusinessInterface> getBusinessInterfaceIsExist(String interfaceId) {
		ArrayList<Predicate> list = new ArrayList<>();

		if (StringUtils.isNotEmpty(interfaceId)) {
			list.add(qTBusinessInterface.requestInterfaceId.eq(interfaceId));
		}

		List<TBusinessInterface> rtnList = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(list.toArray(new Predicate[list.size()])).fetch();
		return rtnList;
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
	 * 获取平台下的所有接口配置信息
	 * 
	 * @param platformId
	 */
	public List<TBusinessInterface> getListByPlatform(String platformId) {
		List<TBusinessInterface> list = null;
		return list;
	}
	
	public List<TBusinessInterface> getListByPlatforms(List<String> platformIds) {
		List<TBusinessInterface> list =null;
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
			throw new RuntimeException("没有找到集成配置");
		}
		ArrayList<Predicate> list = new ArrayList<>();
		if (StringUtils.isNotBlank(businessInterface.getRequestInterfaceId())) {
			list.add(qTBusinessInterface.requestInterfaceId.eq(businessInterface.getRequestInterfaceId()));
		}
		if (StringUtils.isNotBlank(businessInterface.getSysRegistryId())) {
			list.add(qTBusinessInterface.sysRegistryId.eq(businessInterface.getSysRegistryId()));
		}
		List<TBusinessInterface> interfaces = sqlQueryFactory.select(qTBusinessInterface).from(qTBusinessInterface)
				.where(list.toArray(new Predicate[list.size()])).orderBy(qTBusinessInterface.excErrOrder.asc()).fetch();
		if (CollectionUtils.isEmpty(interfaces)) {
			throw new RuntimeException("没有找到多服务配置集合");
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
