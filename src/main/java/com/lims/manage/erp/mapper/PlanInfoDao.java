package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.PlanInfo;
import com.lims.manage.erp.vo.PlanInfoImportVo;
import com.lims.manage.erp.vo.PlanInfoVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 计划信息dao层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface PlanInfoDao extends BaseMapper<PlanInfo> {

    /**
     * 考试/培训计划列表
     * @param page 分页参数
     * @param planInfo 查询条件
     * @return IPage
     */
    IPage<PlanInfoVo> pageList(Page<PlanInfo> page, @Param("planInfo") PlanInfo planInfo);

    /**
     * 获取计划详情(包含报名信息)
     * @param planId 计划id
     * @return PlanInfoVo
     */
    PlanInfoVo getPlanInfoDetail(@Param("planId") String planId);

    /**
     * 根据计划id获取计划完成人员列表
     * @param planId 计划id
     * @return PlanInfoImportVo
     */
    List<PlanInfoImportVo> getPlanCompleteInfo(@Param("planId")String planId);
}
