package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.PlanFileInfo;
import com.lims.manage.erp.entity.PlanInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.PlanInfoVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 培训考试计划结果附件业务层
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface PlanFileInfoService extends IService<PlanFileInfo> {
}
