package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.ReportApprovalVo;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:55
 */

public interface ReportApprovalService {

    /**
     * 报告审批列表
     * @param search
     * @param state
     * @return
     */
    List<ReportApprovalVo> getApplyforList(String search,Integer state);

    /**
     * 进行抢单
     * @param reportApprovalVo
     * @return
     */
    Boolean applyfor_monad(ReportApprovalVo reportApprovalVo);

}
