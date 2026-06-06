package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.vo.ReportApprovalVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:55
 */

public interface ReportApprovalService {

    /**
     * 进行抢单
     * @param reportApprovalVo
     * @return
     */
    Boolean applyfor_monad(ReportApprovalVo reportApprovalVo);

    /**
     * 进行审批
     * @return
     */
    Boolean approval_data(ReportApprovalVo reportApprovalVo1);

    /**
     * 进行审批——二次
     * @return
     */
    Boolean approval_data_two(ReportApprovalVo reportApprovalVo1);

    /**
     * 通过报告id 和 登录人id和姓名 比对审批人数据
     * state =1 审批
     * state =2 签发
     * @param taskId
     * @param userId
     * @param name
     * @return
     */
    Boolean efficacyApprovalData(Long taskId,Long userId,String name,Integer state);

    /**
     * 报告审批历史查询
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo applyfor_history(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus);

    /**
     * 查看报告详情
     * @param id
     * @return
     */
    TaskDetailInfoVo getDetails(Long id);

    /**
     * 查询报告URL
     * @param reportId
     * @return
     */
    String getReportUrl(Long reportId);


    /**
     * 报告签发列表
     * @param search
     * @param state
     * @return
     */
    //List<ReportApprovalVo> getVerify_list(String search, Integer state);

    /**
     * 报告签发列表
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo getVerify_list(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus);

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

    /**
     * 签发进行审批二次开发
     * @param reportApprovalVo
     * @return
     */
    Boolean verify_data_two(ReportApprovalVo reportApprovalVo);

    /**
     * 报告签发历史记录
     * @param search
     * @return
     */
    //List<ReportApprovalVo> verifyHistory(String search);

    /**
     * 报告审批列表
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo getApplyforList(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus);

    /**
     * 报告签发历史记录
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo verifyHistory(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus);

    /**
     * 报告退回到下级环节（带意见回退）
     * @param id            报告记录主键
     * @param targetState   目标状态（"0"=回制作 / "3"=回校核）
     * @param reason        退回意见
     * @param clearVerifyer 是否清空校核人（退回到制作时为 true，退回到校核时为 false）
     * @param entrustmentId 委托单id（退回到制作时用于复位检测任务/委托单状态）
     */
    Boolean sendBack(Long id, String targetState, String reason, boolean clearVerifyer, Long entrustmentId);
}
