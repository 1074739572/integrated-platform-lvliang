package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.entity.TProjectProductLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.dsl.StringPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;
/**
* 项目与产品关联
* @author weihe9
* @date 2020/12/12 14:16
*/
@Service
public class ProjectProductLinkService extends QuerydslService<TProjectProductLink, String, TProjectProductLink, StringPath, PageRequest<TProjectProductLink>> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectProductLinkService.class);

    public ProjectProductLinkService(){
        super(qTProjectProductLink, qTProjectProductLink.id);
    }

    /**
     * 根据项目id获取项目与产品关联信息
     * @param projectId
     * @return
     */
    public List<TProjectProductLink> findProjectProductLinkByProjectId(String projectId) {
        return sqlQueryFactory.select(qTProjectProductLink).from(qTProjectProductLink)
                .where(qTProjectProductLink.projectId.eq(projectId)).fetch();
    }

}
