package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.PlanFileInfo;
import com.lims.manage.erp.entity.PlanInfo;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.PlanInfoVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 计划信息业务层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface PlanInfoService extends IService<PlanInfo> {

    /**
     * 考试/培训计划列表
     * @param page 分页参数
     * @param planInfo 查询条件
     * @return IPage
     */
    IPage<PlanInfoVo> pageList(Page<PlanInfo> page, PlanInfo planInfo);

    /**
     * 添加考试/培训计划
     * @param planInfo 考试/培训计划信息
     * @param file 附件
     * @return Result
     */
    Result<?> addPlanInfo(PlanInfo planInfo, MultipartFile file);

    /**
     * 获取计划详情(包含报名信息)
     * @param planId 计划id
     * @return PlanInfoVo
     */
    PlanInfoVo getPlanInfoDetail(String planId);

    /**
     * 报名参加计划
     * @param planId 计划id
     * @return Result
     */
    Result<?> enrollPlanInfo(String planId);

    /**
     * 根据计划id删除计划信息
     * @param planId 计划id
     * @return Result
     */
    Result<?> delPlanInfo(String planId);

    /**
     * 获取计划报名的用户信息
     * @param planId 计划id
     * @param response 返回
     */
    void planTemplateExport(String planId, HttpServletResponse response);

    /**
     * 保存培训考试结果
     * @param planInfo 培训考试计划结果
     * @param file 附件
     * @return Result
     */
    Result<?> savePlanResult(PlanInfo planInfo, MultipartFile[] file);
}
