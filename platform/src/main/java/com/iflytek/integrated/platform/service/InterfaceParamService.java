package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TInterfaceParam;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTInterfaceParam.qTInterfaceParam;
/**
* 标准接口参数
* @author weihe9
* @date 2020/12/14 10:59
*/
@Service
public class InterfaceParamService extends QuerydslService<TInterfaceParam, String, TInterfaceParam, StringPath, PageRequest<TInterfaceParam>> {

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
        return sqlQueryFactory.select(qTInterfaceParam).from(qTInterfaceParam)
                .where(qTInterfaceParam.interfaceId.eq(interfaceId)).fetch();
    }


}
