package com.iflytek.integrated.platform.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.integrated.common.Constant;
import com.iflytek.integrated.common.ResultDto;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.StringUtil;
import com.iflytek.integrated.common.utils.Utils;
import com.iflytek.integrated.platform.entity.TProductFunctionLink;
import com.iflytek.integrated.platform.entity.TProject;
import com.iflytek.integrated.platform.entity.TProjectProductLink;
import com.iflytek.medicalboot.core.dto.PageRequest;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.iflytek.medicalboot.core.querydsl.QuerydslService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.iflytek.integrated.platform.entity.QTProductFunctionLink.qTProductFunctionLink;
import static com.iflytek.integrated.platform.entity.QTProject.qTProject;
import static com.iflytek.integrated.platform.entity.QTProjectProductLink.qTProjectProductLink;

/**
* 项目管理
* @author weihe9
* @date 2020/12/12 17:19
*/
@Slf4j
@Api(tags = "项目管理")
@RestController
@RequestMapping("/{version}/pb/projectManage")
public class ProjectService extends QuerydslService<TProject, String, TProject, StringPath, PageRequest<TProject>> {

    @Autowired
    private ProjectProductLinkService projectProductLinkService;
    @Autowired
    private ProductFunctionLinkService productFunctionLinkService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private StringUtil stringUtil;

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    public ProjectService(){
        super(qTProject, qTProject.id);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "获取项目信息", notes = "获取项目信息")
    @GetMapping("/getProject")
    public ResultDto getProject(
            @ApiParam(value = "项目类型 1区域 2医院") @RequestParam(required = false) String projectType,
            @ApiParam(value = "项目状态 1启用 2停用") @RequestParam(required = false) String projectStatus,
            @ApiParam(value = "项目名称") @RequestParam(required = false) String projectName) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotBlank(projectType))
            list.add(qTProject.projectType.eq(projectType));
        if (StringUtils.isNotBlank(projectStatus))
            list.add(qTProject.projectStatus.eq(projectStatus));
        if (StringUtils.isNotBlank(projectName))
            list.add(qTProject.projectName.like(Utils.createFuzzyText(projectName)));
        List<TProject> rtnList = sqlQueryFactory.select(qTProject).from(qTProject)
                .where(list.toArray(new Predicate[list.size()])).fetch();
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取项目信息成功!", rtnList);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增or修改项目", notes = "新增or修改项目")
    @GetMapping("/saveAndUpdateProject")
    public ResultDto saveAndUpdateProject(
            @ApiParam(value = "保存项目-产品-功能信息") @RequestParam(required = true) String param){
        JSONObject jsonObj = JSON.parseObject(param);
        String projectName = jsonObj.getString("projectName");
        if (StringUtils.isBlank(projectName)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目名称为空!", null);
        }
        String projectId = jsonObj.getString("id");
        if (isExistence(projectName, projectId)) { //检验项目名称是否存在
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目名称已经存在!", null);
        }
        if (StringUtils.isBlank(projectId)) { //项目新增
            return saveProject(jsonObj);
        }else { //项目修改
            return updateProject(jsonObj);
        }
    }

    /** 新增项目 */
    private ResultDto saveProject(JSONObject jsonObj) {
        try {
            String projectId = batchUidService.getUid(qTProject.getTableName())+"";
            sqlQueryFactory.insert(qTProject).set(qTProject.id, projectId)
                         .set(qTProject.projectName, jsonObj.getString("projectName"))
                         .set(qTProject.projectCode, stringUtil.recountNew(Constant.AppCode.PROJECT, 4))
                         .set(qTProject.projectStatus, Constant.Status.START)
                         .set(qTProject.projectType, jsonObj.getString("projectType"))
//                                         .set(qTProject.createdBy, )
                         .set(qTProject.createdTime, new Date()).execute();
            JSONArray productList = jsonObj.getJSONArray("productList");
            for (int i = 0; i < productList.size(); i++) {
                JSONObject pObj = productList.getJSONObject(i);
                String productId = pObj.getString("productId");
                JSONArray jsonArr = pObj.getJSONArray("functionList");
                for (int j = 0; j < jsonArr.size(); j++) {
                    /**产品与功能关联*/
                    String productFunLinkId = batchUidService.getUid(qTProductFunctionLink.getTableName()) + "";
                    TProductFunctionLink tpfl = new TProductFunctionLink();
                    tpfl.setId(productFunLinkId);
                    tpfl.setProductId(productId);
                    tpfl.setFunctionId(jsonArr.getJSONObject(j).getString("functionId"));
                    tpfl.setCreatedTime(new Date());
                    productFunctionLinkService.post(tpfl);
                    /**项目与产品关联*/
                    TProjectProductLink tppl = new TProjectProductLink();
                    tppl.setId(batchUidService.getUid(qTProjectProductLink.getTableName())+"");
                    tppl.setProjectId(projectId);
                    tppl.setProductFunctionLinkId(productFunLinkId);
                    tppl.setCreatedTime(new Date());
                    projectProductLinkService.post(tppl);
                }
            }
        } catch (Exception e) {
            logger.error("新增项目失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "新增项目失败!", ExceptionUtil.dealException(e));
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "新增项目成功!", null);
    }

    /** 修改项目 */
    private ResultDto updateProject(JSONObject jsonObj) {
        try {
            String projectId = jsonObj.getString("id");
            this.deleteProjectById(projectId);
            sqlQueryFactory.update(qTProject).set(qTProject.projectName, jsonObj.getString("projectName"))
                    .set(qTProject.projectType, jsonObj.getString("projectType"))
                    .set(qTProject.updatedTime, new Date())
                    .where(qTProject.id.eq(projectId)).execute();
            JSONArray productList = jsonObj.getJSONArray("productList");
            for (int i = 0; i < productList.size(); i++) {
                JSONObject pObj = productList.getJSONObject(i);
                String productId = pObj.getString("productId");
                JSONArray jsonArr = pObj.getJSONArray("functionList");
                for (int j = 0; j < jsonArr.size(); j++) {
                    /**产品与功能关联*/
                    String productFunLinkId = batchUidService.getUid(qTProductFunctionLink.getTableName()) + "";
                    TProductFunctionLink tpfl = new TProductFunctionLink();
                    tpfl.setId(productFunLinkId);
                    tpfl.setProductId(productId);
                    tpfl.setFunctionId(jsonArr.getJSONObject(j).getString("functionId"));
                    tpfl.setCreatedTime(new Date());
                    productFunctionLinkService.post(tpfl);
                    /**项目与产品关联*/
                    TProjectProductLink tppl = new TProjectProductLink();
                    tppl.setId(batchUidService.getUid(qTProjectProductLink.getTableName())+"");
                    tppl.setProjectId(projectId);
                    tppl.setProductFunctionLinkId(productFunLinkId);
                    tppl.setCreatedTime(new Date());
                    projectProductLinkService.post(tppl);
                }
            }
        } catch (Exception e) {
            logger.error("项目修改失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目修改失败!", ExceptionUtil.dealException(e));
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "项目修改成功!", null);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "删除项目", notes = "删除项目")
    @GetMapping("/deleteProject")
    public ResultDto deleteProject(@ApiParam(value = "项目id") @RequestParam(value = "id", required = true) String id) {
        try {
            this.deleteProjectById(id);
            sqlQueryFactory.delete(qTProject).where(qTProject.id.eq(id)).execute();
        } catch (Exception e) {
            logger.error("项目删除失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目删除失败!", ExceptionUtil.dealException(e));

        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "项目删除成功!", null);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "更改项目启用状态", notes = "更改项目启用状态")
    @GetMapping("/updateProjectStatus")
    public ResultDto updateProjectStatus(@ApiParam(value = "项目id") @RequestParam(value = "id", required = true) String id,
                                         @ApiParam(value = "项目状态 1启用 2停用") @RequestParam(value = "projectStatus", required = true) String projectStatus) {
        try {
            sqlQueryFactory.update(qTProject).set(qTProject.projectStatus, projectStatus)
                    .where(qTProject.id.eq(id)).execute();
        } catch (Exception e) {
            logger.error("项目状态修改失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目状态修改失败!", ExceptionUtil.dealException(e));
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "项目状态修改成功!", null);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "获取某项目下的产品及功能信息", notes = "获取某项目下的产品及功能信息")
    @GetMapping("/getInfoByProjectId")
    public ResultDto getInfoByProjectId(@ApiParam(value = "项目id") @RequestParam(value = "id", required = true) String id) {
        try {
            List<TProjectProductLink> list = projectProductLinkService.findProjectProductLinkByProjectId(id);
            Map<String, String> map = new HashMap<>();
            for (TProjectProductLink obj : list) {
                String productFunctionLinkId = obj.getProductFunctionLinkId();
                TProductFunctionLink tpfl = productFunctionLinkService.getOne(productFunctionLinkId);
                String pId = tpfl.getProductId();
                String fId = tpfl.getFunctionId();
                boolean isExist = false;
                for(String key : map.keySet()){
                    if(key.equals(pId)) {
                        map.put(pId, map.get(pId)+","+fId);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    map.put(pId, fId);
                }
            }
            //返回数据
            JSONArray rtnArr = new JSONArray();
            if (map.size() > 0) {
                for(String key : map.keySet()) {
                    String[] fIdArr = map.get(key).split(",");
                    JSONArray arr = new JSONArray();
                    for(int i = 0; i<fIdArr.length; i++) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("id", fIdArr[i]);
                        arr.add(jsonObj);
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("id", key);
                    obj.put("function", arr);
                    rtnArr.add(obj);
                }
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取项目下的产品及功能信息成功!", rtnArr);
        } catch (Exception e) {
            logger.error("获取项目下的产品及功能信息失败!", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "获取项目下的产品及功能信息失败!", ExceptionUtil.dealException(e));
        }
    }


    /**
     * 校验是否存在相同的项目名
     * @param projectName
     * @param projectId
     * @return
     */
    private boolean isExistence(String projectName, String projectId){
        ArrayList<Predicate> list = new ArrayList<>();
        list.add(qTProject.projectName.eq(projectName));
        if (StringUtils.isNotBlank(projectId)) {
            list.add(qTProject.id.notEqualsIgnoreCase(projectId));
        }
        List<String> projects = sqlQueryFactory.select(qTProject.id).from(qTProject)
                .where(list.toArray(new Predicate[list.size()])).fetch();

        if (!CollectionUtils.isEmpty(projects)) {
            return true;
        }
        return false;
    }


    /**
     * 根据项目id删除其关联信息
     * @param projectId
     */
    private void deleteProjectById(String projectId) {
        List<TProjectProductLink> list = projectProductLinkService.findProjectProductLinkByProjectId(projectId);
        for (TProjectProductLink obj : list) {
            String productFunctionLinkId = obj.getProductFunctionLinkId();
            productFunctionLinkService.deleteProductFunctionLinkById(productFunctionLinkId);
            projectProductLinkService.deleteProjectProductLinkById(obj.getId());
        }
    }


}