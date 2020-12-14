package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TVendorConfig;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.iflytek.integrated.platform.entity.QTVendorConfig.qTVendorConfig;


@Service
public class VendorConfigService extends QuerydslService<TVendorConfig, String, TVendorConfig, StringPath, PageRequest<TVendorConfig>> {

    private static final Logger logger = LoggerFactory.getLogger(VendorConfigService.class);

    public VendorConfigService(){
        super(qTVendorConfig, qTVendorConfig.id);
    }


}
