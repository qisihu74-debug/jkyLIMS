package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.PlanInfoDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.PlanInfoImportVo;
import com.lims.manage.erp.vo.PlanInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 计划信息业务层实现类
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class PlanInfoServiceImpl extends ServiceImpl<PlanInfoDao, PlanInfo> implements PlanInfoService {

    @Resource
    UserPlanInfoService userPlanInfoService;

    @Resource
    SysUserRoleService sysUserRoleService;

    @Resource
    PlanFileInfoService planFileInfoService;

    @Resource
    SysUserService sysUserService;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Override
    public IPage<PlanInfoVo> pageList(Page<PlanInfo> page, PlanInfo planInfo) {
        planInfo.setPartakeUserId(ShiroUtils.getUserInfo().getUserId().toString());
        return baseMapper.pageList(page, planInfo);
    }

    @Override
    public Result<?> addPlanInfo(PlanInfo planInfo, MultipartFile file) {
        //根据用户角色进行判断
        SysUserRoleEntity sysUserRoleEntity = sysUserRoleService.getOne(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, ShiroUtils.getUserInfo().getUserId()).eq(SysUserRoleEntity::getRoleId, CommonConstant.USER_ROLE_PLAN));
        if (sysUserRoleEntity == null) {
            return ResultUtil.error(500, "该用户没有新建权限");
        }
        //附件存在上传附件到服务器
        if (file != null) {
            String name = file.getOriginalFilename();
            String[] strings = name.split("\\.");
            String upload = MinIoUtil.upload(BucketsConst.file_syn, file, GenID.getID() + "." + strings[strings.length - 1]);
            if (!StringUtils.isEmpty(upload)) {
                planInfo.setEnclosureUrl(upload);
            } else {
                return ResultUtil.error("上传附件保存出错");
            }
        }
        //planInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        planInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
        planInfo.setCreateTime(new Date());
        this.save(planInfo);
        return ResultUtil.success("创建考试/培训计划成功");
    }

    @Override
    public PlanInfoVo getPlanInfoDetail(String planId) {
        //获取计划详情及报名信息
        PlanInfoVo planInfoVo=baseMapper.getPlanInfoDetail(planId);
        //如果计划详情存在 且计划结束时间在当前时间之前 则获取完成人员列表
        if(planInfoVo!=null && planInfoVo.getPlanEndTime().before(new Date())){
            //获取用户完成人员列表
            List<PlanInfoImportVo> planInfoImportList=baseMapper.getPlanCompleteInfo(planId);
            planInfoVo.setPlanInfoImportList(planInfoImportList);

            //获取文件列表
            LambdaQueryWrapper<PlanFileInfo> wrapper=Wrappers.lambdaQuery();
            wrapper.eq(PlanFileInfo::getPlanId,planId);
            wrapper.select(PlanFileInfo::getFileName,PlanFileInfo::getFileUrl);
            List<PlanFileInfo> planFileInfoList=planFileInfoService.list(wrapper);
            planInfoVo.setPlanFileInfoList(planFileInfoList);
        }
        return planInfoVo;
    }

    @Override
    public Result<?> enrollPlanInfo(String planId) {
        //根据计划id查询计划信息
        PlanInfo planInfo = this.getById(planId);
        if (planInfo != null) {
            //根据当前用户id和计划id查询用户是否已经报名过
            LambdaQueryWrapper<UserPlanInfo> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(UserPlanInfo::getUserId, ShiroUtils.getUserInfo().getUserId().toString());
            wrapper.eq(UserPlanInfo::getPlanId, planId);
            UserPlanInfo userPlanInfo = userPlanInfoService.getOne(wrapper);
            if (userPlanInfo != null) {
                return ResultUtil.error("该计划已报名");
            }
            //根据计划的开始时间判断当前是否可以报名
            if (planInfo.getPlanBeginTime().after(new Date())) {
                userPlanInfo = new UserPlanInfo();
                userPlanInfo.setPlanId(planId);
                userPlanInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
                userPlanInfo.setEnrollTime(new Date());
                userPlanInfo.setPartakeStatus(CommonConstant.PLAN_PARTAKE_STATUS_ENROLL);
                userPlanInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
                userPlanInfo.setCreateTime(new Date());
                userPlanInfoService.save(userPlanInfo);
            } else {
                return ResultUtil.error("当前计划暂无法报名");
            }
        } else {
            return ResultUtil.error("计划未找到");
        }
        return ResultUtil.success("报名成功");
    }

    @Override
    public Result<?> delPlanInfo(String planId) {
        //根据用户角色进行判断
        SysUserRoleEntity sysUserRoleEntity = sysUserRoleService.getOne(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, ShiroUtils.getUserInfo().getUserId()).eq(SysUserRoleEntity::getRoleId, CommonConstant.USER_ROLE_PROBLEM_CLASSIFICATION));
        if (sysUserRoleEntity == null) {
            return ResultUtil.error(500, "该用户没有计划删除权限");
        }
        //根据计划id获取计划信息
        PlanInfo planInfo = this.getById(planId);
        if (planInfo != null) {
            //如果当前计划不是当前用户创建的则无法删除
            // if(ShiroUtils.getUserInfo().getUserId().toString().equals(planInfo.getUserId())){
            //如果计划已经开始则无法删除
            if (planInfo.getPlanBeginTime().after(new Date())) {
                //如果有附件地址
                if (!StringUtils.isEmpty(planInfo.getEnclosureUrl())) {
                    String fileName = planInfo.getEnclosureUrl().substring(planInfo.getEnclosureUrl().lastIndexOf("/") + 1, planInfo.getEnclosureUrl().lastIndexOf("?"));
                    MinIoUtil.deleteFile(BucketsConst.file_syn, fileName);
                }
                this.removeById(planId);
                return ResultUtil.success("计划信息删除成功");
            } else {
                return ResultUtil.error(500, "计划开始无法删除");
            }
            /*}else{
                return ResultUtil.error(500,"没有删除权限");
            }*/

        } else {
            return ResultUtil.error(500, "没有找到对应计划信息");
        }
    }

    @Override
    public void planTemplateExport(String planId, HttpServletResponse response) {
        String fileName = "培训考试计划模板.xls";
        String sheetName = "培训考试计划模板";
        HSSFWorkbook workbook = ExcelUtil.createTemplate(sheetName,
                ExcelPlanStructure.PLAN_COLUMN_NAME, ExcelPlanStructure.PLAN_ISNULL_ABLE);
        //根据计划id获取培训人员信息
        List<SysUserEntity> userList = userPlanInfoService.getPlanUserInfo(planId);
        if (userList != null && userList.size() > 0) {
            for (int i = 0; i < userList.size(); i++) {
                // 创建事例数据
                HSSFRow exampleDataRow = workbook.getSheet(sheetName).createRow(i + 1);
                // 设置用户名称
                exampleDataRow.createCell(0).setCellValue(userList.get(i).getName());
                // 设置用户账号
                exampleDataRow.createCell(1).setCellValue(userList.get(i).getUsername());
            }
        }
        //输出文件流
        workbookWrite(response, fileName, workbook);
    }

    /**
     * 输出文件流
     *
     * @param response
     * @param fileName
     * @param workbook
     */
    private void workbookWrite(HttpServletResponse response, String fileName, HSSFWorkbook workbook) {
        // 响应到客户端
        try {
            fileName = new String(fileName.getBytes(), "ISO8859-1");
            response.setContentType("application/octet-stream;charset=ISO8859-1");
            response.setHeader("Content-Disposition", "attachment;filename=" + "report.xls");
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> savePlanResult(PlanInfo planInfo, MultipartFile[] file) {
        //根据用户角色进行判断
        SysUserRoleEntity sysUserRoleEntity = sysUserRoleService.getOne(Wrappers.<SysUserRoleEntity>lambdaQuery().eq(SysUserRoleEntity::getUserId, ShiroUtils.getUserInfo().getUserId()).eq(SysUserRoleEntity::getRoleId, CommonConstant.USER_ROLE_PROBLEM_CLASSIFICATION));
        if (sysUserRoleEntity == null) {
            return ResultUtil.error(500, "该用户没有保存计划结果权限");
        }
        //根据计划id获取计划信息
        PlanInfo planInfo1=this.getById(planInfo.getPlanId());
        if(planInfo1==null ){
            return ResultUtil.error(500, "计划信息不存在");
        } else if (!StringUtils.isEmpty(planInfo1.getUpdateBy())) {
            return ResultUtil.error(500, "计划结果已保存，不可再次上传");
        }
        //附件存在上传附件到服务器
        if (file.length > 0) {
            //便利文件列表
            for (MultipartFile planFile : file) {
                String name = planFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                //上传文件到minio服务器
                String upload = MinIoUtil.upload(BucketsConst.file_syn, planFile, GenID.getID() + "." + strings[strings.length - 1]);
                if (!StringUtils.isEmpty(upload)) {
                    String uploadUrl = upload.substring(0, upload.indexOf("?"));
                    String fileSuffix = strings[strings.length - 1].toUpperCase();
                    //添加到附件结果表
                    PlanFileInfo planFileInfo = new PlanFileInfo();
                    planFileInfo.setPlanId(planInfo.getPlanId());
                    planFileInfo.setFileUrl(uploadUrl);
                    planFileInfo.setFileName(name);
                    planFileInfo.setFileSuffix(fileSuffix);
                    planFileInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
                    planFileInfo.setCreateTime(new Date());
                    planFileInfoService.save(planFileInfo);
                }
            }
        }
        //对用户的成绩信息进行保存
        if (!planInfo.getPlanImportList().isEmpty()) {
            for (PlanInfoImportVo planInfoImport : planInfo.getPlanImportList()) {
                //根据用户账号获取用户id
                SysUserEntity user = sysUserService.getOne(Wrappers.<SysUserEntity>lambdaQuery().eq(SysUserEntity::getUsername, planInfoImport.getUsername()));
                if (user != null) {
                    //根据用户id和计划id获取用户计划信息
                    LambdaQueryWrapper<UserPlanInfo> wrapper = Wrappers.lambdaQuery();
                    wrapper.eq(UserPlanInfo::getPlanId, planInfo.getPlanId());
                    wrapper.eq(UserPlanInfo::getUserId, user.getUserId().toString());
                    UserPlanInfo userPlanInfo = userPlanInfoService.getOne(wrapper);
                    //根据用户id和计划id对用户的成绩进行保存
                    if (userPlanInfo == null) {
                        userPlanInfo = new UserPlanInfo();
                        userPlanInfo.setUserId(user.getUserId().toString());
                        userPlanInfo.setPlanId(planInfo.getPlanId());
                        userPlanInfo.setEnrollTime(new Date());
                        userPlanInfo.setPartakeStatus(CommonConstant.PLAN_PARTAKE_STATUS_NO_ENROLL_COMPLETE);
                        userPlanInfo.setCreateBy(user.getUsername());
                        userPlanInfo.setCreateTime(new Date());
                    }else {
                        userPlanInfo.setPartakeStatus(CommonConstant.PLAN_PARTAKE_STATUS_COMPLETE);
                    }
                    userPlanInfo.setExaminationRemarks(planInfoImport.getExpression());
                    userPlanInfo.setExaminationScores(planInfoImport.getScore());
                    userPlanInfo.setExaminationIntegral(planInfoImport.getIntegralNum());
                    userPlanInfoService.saveOrUpdate(userPlanInfo);

                    if (planInfoImport.getIntegralNum() != null && planInfoImport.getIntegralNum() != 0) {
                        //添加用户积分
                        userIntegralRecordService.addUserIntegralRecord(CommonConstant.INTEGRAL_RULE_PLAN_COMPLETE, null, user.getUserId().toString(), CommonConstant.USER_OPERATION_EVENT_TYPE_PLAN, planInfo.getPlanId(), planInfoImport.getIntegralNum());
                    }
                }
            }
        }

        //保存计划结果信息
        planInfo.setUpdateBy(ShiroUtils.getUserInfo().getUsername());
        planInfo.setUpdateTime(new Date());
        this.updateById(planInfo);
        return ResultUtil.success("计划结果保存完成");
    }
}
