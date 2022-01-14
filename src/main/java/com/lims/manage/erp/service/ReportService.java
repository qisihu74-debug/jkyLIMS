package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;

import java.util.List;

public interface ReportService {
    /**
     * 查询可制作报告列表
     *
     * @return
     */
    List<ReportListVo> getReportList();

    /**
     * 查询委托单--报告制作详情
     *
     * @param id
     * @return
     */
    ReportDetailVo getReportDetail(Long id);

    /**
     * 保存信息
     *
     * @param vo
     * @return
     */
    Boolean preserve(ReportPreserveVo vo);

    /**
     * 待盖章和历史盖章列表查询
     *
     * @param type
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize);
}
