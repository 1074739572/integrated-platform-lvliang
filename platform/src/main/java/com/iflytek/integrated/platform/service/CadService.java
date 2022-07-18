package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.platform.entity.TCadFile;
import com.iflytek.integrated.platform.utils.PlatformUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.iflytek.integrated.platform.entity.QTCadFile.qtCadFile;
import static com.iflytek.integrated.platform.entity.QTDrive.qTDrive;

/**
 * 分类
 *
 * @author weihe9
 * @date 2020/12/20 17:02
 */
@Slf4j
@Api(tags = "CAD文档管理")
@RestController
@RequestMapping("/{version}/pt/cadFileManage")
public class CadService extends BaseService<TCadFile, String, StringPath> {

    private static final Logger logger = LoggerFactory.getLogger(CadService.class);

    public CadService() {
        super(qtCadFile, qtCadFile.id);
    }

    String uploadPath;

    @PostConstruct
    public void init() {
        ApplicationHome h = new ApplicationHome(getClass());
        String root = h.getSource()
                .getParentFile().getParentFile().toString();
        uploadPath = root + File.separator + "upload" + File.separator;
        logger.info("==>图片存储目录为：{}",uploadPath);
    }

    @ApiOperation(value = "获取服务分类列表")
    @GetMapping("/getFileList")
    public ResultDto<TableData<TCadFile>> getInterfaceType(
            @ApiParam(value = "文件主题") @RequestParam(value = "docTheme", required = false) String docTheme,
            @ApiParam(value = "页码", example = "1") @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
            @ApiParam(value = "每页大小", example = "10") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        ArrayList<Predicate> list = new ArrayList<>();
        // 判断条件是否为空
        if (StringUtils.isNotEmpty(docTheme)) {
            list.add(qtCadFile.docTheme.like(PlatformUtil.createFuzzyText(docTheme)));
        }
        QueryResults<TCadFile> queryResults = sqlQueryFactory
                .select(Projections.bean(TCadFile.class, qtCadFile.id, qtCadFile.docNo, qtCadFile.docTheme, qtCadFile.docStandardNo, qtCadFile.docStandardDesc, qtCadFile.filePath))
                .from(qtCadFile).where(list.toArray(new Predicate[list.size()]))
                .limit(pageSize)
                .offset((pageNo - 1) * pageSize).orderBy(qtCadFile.docNo.desc()).fetchResults();
        // 分页
        TableData<TCadFile> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto<>(Constant.ResultCode.SUCCESS_CODE, "数据获取成功!", tableData);
    }

    @ApiOperation(value = "导出")
    @GetMapping("/export")
    public void downloadResources(@ApiParam(value = "id") @RequestParam String id, HttpServletResponse response) {
        //根据id查询文件路径
        TCadFile cadFile = this.getOne(id);
        if (cadFile == null || StringUtils.isEmpty(cadFile.getFilePath())) {
            throw new RuntimeException("该文档未维护文件目录!");
        }

        String filePath=uploadPath+cadFile.getFilePath();

        try {
            String fileName = cadFile.getDocTheme()+".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName + ";filename*=utf-8''" + URLEncoder.encode(fileName, "utf-8"));
            response.setHeader("Access-Control-Expose-Headers", "content-disposition");
            File sourceFile = new File(filePath);
            if (!sourceFile.exists()) {
                System.out.println(">>>>>> 待压缩的文件目录：" + filePath + " 不存在. <<<<<<");
            } else {
                ZipOutputStream zos = null;
                BufferedInputStream bis = null;
                try {

                    File[] files = sourceFile.listFiles();
                    zos = new ZipOutputStream(response.getOutputStream());
                    byte[] buf = new byte[8192];
                    int len;
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if (!file.isFile()) continue;
                        ZipEntry ze = new ZipEntry(file.getName());
                        zos.putNextEntry(ze);
                        bis = new BufferedInputStream(new FileInputStream(file));
                        while ((len = bis.read(buf)) > 0) {
                            zos.write(buf, 0, len);
                        }
                        zos.closeEntry();
                    }
                    zos.closeEntry();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (zos != null) {
                        try {
                            zos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("下载文件失败");
            response.setStatus(500);
        }
    }
}
