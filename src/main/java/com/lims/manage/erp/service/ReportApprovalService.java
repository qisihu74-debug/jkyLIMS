package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.ReportApprovalVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;

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

    /**
     * 进行审批
     * @param id
     * @param peroration
     * @param reason
     * @return
     */
    Boolean approval_data(ReportApprovalVo reportApprovalVo1);

    /**
     * 报告审批历史查询
     * @param search
     * @return
     */
    List<ReportApprovalVo> applyfor_history(String search);

    /**
     * 查看详情
     * @param id
     * @return
     */
    TaskDetailInfoVo getDetails(Long id);

    /**
     * 报告签发列表
     * @param search
     * @param state
     * @return
     */
    List<ReportApprovalVo> getVerify_list(String search, Integer state);

    /**
     * 签发抢单
     * @param reportApprovalVo
     * @return
     */
    Boolean verify_monad(ReportApprovalVo reportApprovalVo);

    /**
     * 签发进行审批
     * @param reportApprovalVo
     * @return
     */
    Boolean verify_data(ReportApprovalVo reportApprovalVo);



}
