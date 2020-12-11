package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TDrive;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;

/**
 * 驱动管理
 * @author czzhan
 */
@Slf4j
@RestController
public class DriveService extends QuerydslService<TDrive, String, TDrive, StringPath, PageRequest<TDrive>> {
    public DriveService(){
        super(qTDrive,qTDrive.id);
    }

    private static final Logger logger = LoggerFactory.getLogger(DriveService.class);

}
