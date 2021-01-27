package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.entity.TInterfaceParam;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTInterface.qTInterface;
import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;

/**
* 标准接口参数
* @author weihe9
* @date 2020/12/14 10:59
*/
@Service
public class InterfaceParamService extends BaseService<TInterfaceParam, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceParamService.class);

    public InterfaceParamService(){
        super(qTInterfaceParam, qTInterfaceParam.id);
    }


    /**
     * 根据接口id删除产品与接口关联信息
     * @param id
     */
    public void deleteProductInterfaceLinkById(String id) {
        sqlQueryFactory.delete(qTInterfaceParam).where(qTInterfaceParam.interfaceId.eq(id)).execute();
    }

    /**
     * 获取接口参数
     * @param interfaceId
     * @return
     */
    public List<TInterfaceParam> getParamsByInterfaceId(String interfaceId) {
        return sqlQueryFactory.select(
                Projections.bean(TInterfaceParam.class, qTInterfaceParam.id, qTInterfaceParam.paramName, qTInterfaceParam.paramInstruction,
                        qTInterfaceParam.interfaceId, qTInterfaceParam.paramType, qTInterfaceParam.paramLength, qTInterfaceParam.paramInOut,
                        qTInterfaceParam.createdBy, qTInterfaceParam.createdTime, qTInterfaceParam.updatedBy, qTInterfaceParam.updatedTime,
                        qTInterface.paramOutStatus.as("paramOutStatus"), qTInterface.paramOutStatusSuccess.as("paramOutStatusSuccess")))
                .from(qTInterfaceParam)
                .leftJoin(qTInterface).on(qTInterface.id.eq(qTInterfaceParam.interfaceId))
                .where(qTInterfaceParam.interfaceId.eq(interfaceId)).fetch();
    }


}
