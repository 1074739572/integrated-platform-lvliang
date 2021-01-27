package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.entity.TInterfaceMonitor;
import com.querydsl.core.types.dsl.NumberPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iflytek.integrated.platform.entity.QTInterfaceMonitor.qTInterfaceMonitor;

/**
* 接口监控
* @author weihe9
* @date 2020/12/15 16:51
*/
public class InterfaceMonitorService extends BaseService<TInterfaceMonitor, Long, NumberPath<Long>> {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceMonitorService.class);

    public InterfaceMonitorService(){
        super(qTInterfaceMonitor, qTInterfaceMonitor.id);
    }
    
}
