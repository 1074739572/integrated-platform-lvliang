package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.platform.common.Constant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.SimpleDateFormat;

@Slf4j
@Api(tags = "上传接口")
@RestController
@RequestMapping("/{version}/pt/file")
public class UploadService {
    private String uploadPath;

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    @PostConstruct
    public void init() {
        ApplicationHome h = new ApplicationHome(getClass());
        String root = h.getSource()
                .getParentFile().getParentFile().toString();
        uploadPath = root + File.separator + "upload" + File.separator;
        logger.info("==>图片存储目录为：{}", uploadPath);
    }

    @ApiOperation(value = "文件上传", notes = "文件上传")
    @PostMapping("/upload")
    public ResultDto upload(@RequestParam("file") MultipartFile file) throws Exception {
        File upDir = new File(uploadPath);
        if (!upDir.exists()) {
            upDir.mkdir();
        }
        String fileName = file.getOriginalFilename();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        fileName = String.format("%s-%s", sdf.format(System.currentTimeMillis()), fileName);
        String filePath = uploadPath + fileName;
        File newFile = new File(filePath);
        file.transferTo(newFile);
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "文件上传成功!", "/file/"+fileName);
    }
}
