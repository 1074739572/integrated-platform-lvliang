package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TInterfaceMonitor;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.NumberPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iflytek.integrated.platform.entity.QTInterfaceMonitor.qTInterfaceMonitor;
/**
* 接口监控
* @author weihe9
* @date 2020/12/15 16:51
*/
public class InterfaceMonitorService extends QuerydslService<TInterfaceMonitor, Long, TInterfaceMonitor, NumberPath<Long>, PageRequest<TInterfaceMonitor>> {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceMonitorService.class);

    public InterfaceMonitorService(){
        super(qTInterfaceMonitor, qTInterfaceMonitor.id);
    }
    
}
