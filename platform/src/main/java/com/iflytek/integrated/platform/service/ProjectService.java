package com.iflytek.integrated.platform.service;

import com.iflytek.integrated.platform.common.BaseService;
import com.iflytek.integrated.platform.common.Constant;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.integrated.common.dto.TableData;
import com.iflytek.integrated.common.intercept.UserLoginIntercept;
import com.iflytek.integrated.common.utils.ExceptionUtil;
import com.iflytek.integrated.common.utils.RedisUtil;
import com.iflytek.integrated.platform.dto.FunctionDto;
import com.iflytek.integrated.platform.dto.ProductDto;
import com.iflytek.integrated.platform.dto.ProjectDto;
import com.iflytek.integrated.platform.entity.*;
import com.iflytek.integrated.platform.utils.Utils;
import com.iflytek.medicalboot.core.id.BatchUidService;
import com.querydsl.core.QueryResults;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
@RequestMapping("/{version}/pt/projectManage")
public class ProjectService extends BaseService<TProject, String, StringPath> {

    @Autowired
    private ProjectProductLinkService projectProductLinkService;
    @Autowired
    private ProductFunctionLinkService productFunctionLinkService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private PlatformService platformService;
    @Autowired
    private HospitalVendorLinkService hospitalVendorLinkService;
    @Autowired
    private VendorConfigService vendorConfigService;
    @Autowired
    private BusinessInterfaceService businessInterfaceService;
    @Autowired
    private BatchUidService batchUidService;
    @Autowired
    private Utils utils;
    @Autowired
    private RedisUtil redisUtil;

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    public ProjectService(){
        super(qTProject, qTProject.id);
    }


    @ApiOperation(value = "获取项目信息", notes = "获取项目信息")
    @GetMapping("/getProject")
    public ResultDto<TableData<TProject>> getProject(
            @ApiParam(value = "项目类型 1区域 2医院") @RequestParam(required = false) String projectType,
            @ApiParam(value = "项目状态 1启用 2停用") @RequestParam(required = false) String projectStatus,
            @ApiParam(value = "项目名称") @RequestParam(required = false) String projectName,
            @ApiParam(value = "页码",example = "1") @RequestParam(defaultValue = "1", required = false)Integer pageNo,
            @ApiParam(value = "每页大小",example = "10") @RequestParam(defaultValue = "10", required = false)Integer pageSize) {
        ArrayList<Predicate> list = new ArrayList<>();
        if (StringUtils.isNotBlank(projectType)) {
            list.add(qTProject.projectType.eq(projectType));
        }
        if (StringUtils.isNotBlank(projectStatus)) {
            list.add(qTProject.projectStatus.eq(projectStatus));
        }
        if (StringUtils.isNotBlank(projectName)) {
            list.add(qTProject.projectName.like(Utils.createFuzzyText(projectName)));
        }
        QueryResults<TProject> queryResults = sqlQueryFactory.select(qTProject).from(qTProject)
                .where(list.toArray(new Predicate[list.size()]))
                .limit(pageSize)
                .offset((pageNo - 1) * pageSize)
                .orderBy(qTProject.createdTime.desc()).fetchResults();
        TableData<TProject> tableData = new TableData<>(queryResults.getTotal(), queryResults.getResults());
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取项目信息成功!", tableData);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "新增or修改项目", notes = "新增or修改项目")
    @PostMapping("/saveAndUpdateProject")
    public ResultDto<String> saveAndUpdateProject(@ApiParam(value = "保存项目-产品-功能信息") @RequestBody ProjectDto dto) {
        String projectName = dto.getProjectName();
        if (StringUtils.isBlank(projectName)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目名称为空!", null);
        }
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.getLoginUserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        String projectId = dto.getId();
        //检验项目名称是否存在
        if (isExistence(projectName, projectId)) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目名称已经存在!", null);
        }
        //项目新增
        if (StringUtils.isBlank(projectId)) {
            return saveProject(dto, loginUserName);
        }else { //项目修改
            return updateProject(dto, loginUserName);
        }
    }

    /** 新增项目 */
    private ResultDto saveProject(ProjectDto dto, String loginUserName) {
        String projectId = batchUidService.getUid(qTProject.getTableName())+"";
        String projectName = dto.getProjectName();

        TProject project = new TProject();
        project.setId(projectId);
        project.setProjectName(projectName);
        project.setProjectCode(utils.generateCode(qTProject, qTProject.projectCode, projectName));
        project.setProjectStatus(Constant.Status.START);
        project.setProjectType(dto.getProjectType());
        project.setCreatedTime(new Date());
        project.setCreatedBy(loginUserName);
        projectService.post(project);

        List<ProductDto> productList = dto.getProductList();
        for (int i = 0; i < productList.size(); i++) {
            ProductDto pObj = productList.get(i);
            String productId = pObj.getProductId();
            List<FunctionDto> jsonArr = pObj.getFunctionList();
            for (int j = 0; j < jsonArr.size(); j++) {
                //产品与功能关联
                String functionId = jsonArr.get(j).getFunctionId();
                TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(productId, functionId);
                //项目与产品关联
                TProjectProductLink tppl = new TProjectProductLink();
                tppl.setId(batchUidService.getUid(qTProjectProductLink.getTableName())+"");
                tppl.setProjectId(projectId);
                tppl.setProductFunctionLinkId(tpfl.getId());
                tppl.setCreatedTime(new Date());
                tppl.setCreatedBy(loginUserName);
                projectProductLinkService.post(tppl);
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "新增项目成功!", null);
    }

    /** 修改项目 */
    private ResultDto updateProject(ProjectDto dto, String loginUserName) {
        String projectId = dto.getId();
        this.deleteProjectById(projectId);

        TProject project = new TProject();
        project.setProjectName(dto.getProjectName());
        project.setProjectType(dto.getProjectType());
        project.setUpdatedTime(new Date());
        project.setUpdatedBy(loginUserName);
        projectService.put(projectId, project);

        List<ProductDto> productList = dto.getProductList();
        for (int i = 0; i < productList.size(); i++) {
            ProductDto pObj = productList.get(i);
            String productId = pObj.getProductId();
            List<FunctionDto> jsonArr = pObj.getFunctionList();
            for (int j = 0; j < jsonArr.size(); j++) {
                //产品与功能关联
                String functionId = jsonArr.get(j).getFunctionId();
                TProductFunctionLink tpfl = productFunctionLinkService.getObjByProductAndFunction(productId, functionId);
                //项目与产品关联
                TProjectProductLink tppl = new TProjectProductLink();
                tppl.setId(batchUidService.getUid(qTProjectProductLink.getTableName())+"");
                tppl.setProjectId(projectId);
                if (tpfl != null) {
                    tppl.setProductFunctionLinkId(tpfl.getId());
                }
                tppl.setCreatedTime(new Date());
                tpfl.setCreatedBy(loginUserName);
                projectProductLinkService.post(tppl);
            }
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "项目修改成功!", projectId);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "删除项目", notes = "删除项目")
    @PostMapping("/deleteProject")
    public ResultDto<String> deleteProject(@ApiParam(value = "项目id") @RequestParam(value = "id", required = true) String id) {
        TProject tp = this.getOne(id);
        if (tp == null) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "该项目不存在!", "该项目不存在!");
        }
        //删除项目
        long count = this.delete(id);
        if (count <= 0) {
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目删除失败!", "项目删除失败!");
        }
        //删除项目下所有平台及其相关信息
        List<TPlatform> tpList = platformService.getListByProjectId(id);
        for (TPlatform obj : tpList) {
            String platformId = obj.getId();
            //获取平台下所有厂商配置
            List<TVendorConfig> list = vendorConfigService.getObjByPlatformId(platformId);
            for (TVendorConfig tvc : list) {
                //删除医院与厂商配置关联信息
                hospitalVendorLinkService.deleteByVendorConfigId(tvc.getId());
            }
            //删除平台下所有关联的接口配置
            List<TBusinessInterface> tbiList = businessInterfaceService.getListByPlatform(platformId);
            for (TBusinessInterface tbi : tbiList) {
                businessInterfaceService.delete(tbi.getId());
            }
            //删除平台下的所有厂商配置信息
            vendorConfigService.delVendorConfigAll(platformId);
        }
        //删除项目与产品功能关联
        this.deleteProjectById(id);
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "项目删除成功!", id);
    }


    @ApiOperation(value = "更改项目启用状态", notes = "更改项目启用状态")
    @PostMapping("/updateProjectStatus")
    public ResultDto<String> updateProjectStatus(@ApiParam(value = "项目id") @RequestParam(value = "id", required = true) String id,
                                 @ApiParam(value = "项目状态 1启用 2停用") @RequestParam(value = "projectStatus", required = true) String projectStatus) {
        //校验是否获取到登录用户
        String loginUserName = UserLoginIntercept.LOGIN_USER.getLoginUserName();
        if(StringUtils.isBlank(loginUserName)){
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "没有获取到登录用户!", "没有获取到登录用户!");
        }
        try {
            sqlQueryFactory.update(qTProject)
                    .set(qTProject.projectStatus, projectStatus)
                    .set(qTProject.updatedBy, loginUserName)
                    .where(qTProject.id.eq(id)).execute();
        } catch (Exception e) {
            logger.error("项目状态修改失败! MSG:{}", ExceptionUtil.dealException(e));
            e.printStackTrace();
            return new ResultDto(Constant.ResultCode.ERROR_CODE, "项目状态修改失败!", ExceptionUtil.dealException(e));
        }
        return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "项目状态修改成功!", id);
    }


    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "获取某项目下的产品及功能信息", notes = "获取某项目下的产品及功能信息")
    @GetMapping("/getInfoByProjectId")
    public ResultDto<List<Map<String, Object>>> getInfoByProjectId(@ApiParam(value = "项目id") @RequestParam(value = "id", required = true) String id) {
        try {
            List<TProjectProductLink> list = projectProductLinkService.findProjectProductLinkByProjectId(id);
            Map<String, String> map = new HashMap<>();
            for (TProjectProductLink obj : list) {
                String productFunctionLinkId = obj.getProductFunctionLinkId();
                TProductFunctionLink tpfl = productFunctionLinkService.getOne(productFunctionLinkId);
                if (tpfl != null) {
                    String pId = tpfl.getProductId();
                    String fId = tpfl.getFunctionId();
                    boolean isExist = false;
                    for(String key : map.keySet()) {
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
            }
            //返回数据
            List<Map<String, Object>> rtnArr = new ArrayList<>();
            if (map.size() > 0) {
                for(String key : map.keySet()) {
                    String[] fIdArr = map.get(key).split(",");
                    List<String> arr = new ArrayList<>();
                    for(int i = 0; i<fIdArr.length; i++) {
                        arr.add(fIdArr[i]);
                    }
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("id", key);
                    obj.put("function", arr);
                    rtnArr.add(obj);
                }
            }
            return new ResultDto(Constant.ResultCode.SUCCESS_CODE, "获取项目下的产品及功能信息成功!", rtnArr);
        } catch (Exception e) {
            logger.error("获取项目下的产品及功能信息失败! MSG:{}", ExceptionUtil.dealException(e));
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
            projectProductLinkService.delete(obj.getId());
        }
    }


}
