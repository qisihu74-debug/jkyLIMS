package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.PlanFileInfo;
import com.lims.manage.erp.entity.PlanInfo;
import com.lims.manage.erp.vo.PlanInfoVo;
import org.apache.ibatis.annotations.Param;

/**
 * 培训考试计划结果附件dao层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface PlanFileInfoDao extends BaseMapper<PlanFileInfo> {
}
