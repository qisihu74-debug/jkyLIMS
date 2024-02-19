package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:57
 */
@Component
@Mapper
public interface ReportApprovalMapper {

    List<ReportApprovalVo> getReportApprovalList(@Param("search")String search,@Param("list") Set<Long> list,@Param("reportTypeStatus")Integer reportTypeStatus);

    ReportApprovalVo getReportApprovalDetail(Long id);

    @Select("SELECT\n" +
            "\tt2.id \n" +
            "FROM\n" +
            "\ttest_report_record AS t1\n" +
            "\tLEFT JOIN test_task AS t2 ON t1.task_code = t2.task_code \n" +
            "WHERE\n" +
            "\tt1.id = #{id}")
    Long getTaskId(Long id);


    int updateReportApprovalDetail(ReportApprovalVo reportApprovalVo);

    String getUserName(Long userId);


    List<ReportApprovalVo> getReportApprovalHistory(@Param("search")String search,@Param("list") Set<Long> list,@Param("reportTypeStatus")Integer reportTypeStatus);

    /**
     * 任务单详情
     * @param id
     * @return
     */
    TaskDetailInfoVo getTaskDetail(Long id);


    /**
     * 通过任务单id 获取检测项 主键以及状态
     * @param taskId
     * @return
     */
    EntrustAddVo getEntrustAddVoDetail(Long taskId);

    /**
     * 获取委托id 获取样品信息 以及检测信息
     */
    List<SampleDetailVo> getSampleDetailList(Long id);

    /**
     * 查询 报告签发信息
     * @param search
     * @return
     */
    List<ReportApprovalVo> getVerifyList(@Param("search")String search,@Param("list") Set<Long> list,@Param("reportTypeStatus")Integer reportTypeStatus);

    /**
     * 查询签发 历史
     * @param search
     * @return
     */
    List<ReportApprovalVo> getVerifyHistory(@Param("search")String search,@Param("list") Set<Long> list,@Param("reportTypeStatus")Integer reportTypeStatus);

    /**
     * 签发抢单
     */
    int updateVerifyMonad(ReportApprovalVo reportApprovalVo);

    /**
     * 被驳回后修改状态state = 0  （审批 签发被清除）
     * @param reportApprovalVo
     * @return
     */
    int updateExaminationAndApprovalMonad(ReportApprovalVo reportApprovalVo);

    /**
     * 被驳回后修改状态state = 0  （审批 签发被清除）
     * @param reportApprovalVo
     * @return
     */
    int updateentrustAndApprovalMonad(ReportApprovalVo reportApprovalVo);
    int updateentrustAndApprovalMonad2(ReportApprovalVo reportApprovalVo);
    int updateById(ReportApprovalVo reportApprovalVo);
    /**
     * 根据检测项 获取 检测项所属的 URL连接
     */
    @Select("SELECT origin_url  FROM `test_entrusted_sample_checkitem_rel` WHERE id =#{id}")
    String getCheckItemUrl(Long id);

    /**
     * 通过报告id 获取详细表中 检测项 check_item_id
     */
    List<CheckItemInfoVo> getCheckItemInfoVoList(Long id);

    /**
     * 任务单详情 ----中间报告
     * @param id
     * @return
     */
    TaskDetailInfoVo getTaskDetailInterimReport(Long id);

    /**
     * 查询报告url
     * @param reportId
     * @return
     */
    String getReportUrl(Long reportId);

    /**
     * 签发生成任务快照
     * @param entrustmentId
     * @return
     */
    int insertTaskStatistics(Long entrustmentId);

    /**
     * 签发生成检测项快照
     * @param entrustmentId
     * @return
     */
    int insertCheckItemStatistics(@Param("entrustmentId")Long entrustmentId,@Param("newEscIds")List<Integer> newEscIds);

    /**
     * 查询快照中的检测项信息
     * @param entrustmentId
     * @return
     */
    List<Integer> getStatisticsEscId(Long entrustmentId);

    /**
     * 查询签发时的检测项信息
     *
     * @param entrustmentId
     * @return
     */
    List<Integer> getNewEscId(Long entrustmentId);

    ReportRecordEntity getReportInfo(Long reportId);

    //    @Select("\tSELECT id as task_id FROM test_task WHERE order_time >= \"2023-11-1\" and state >= 4")
//    List<Long> getTaskList();
    @Select("SELECT\n" +
            "\ttt.task_id\n" +
            "FROM\n" +
            "\t(\n" +
            "SELECT DISTINCT\n" +
            "\tt1.id AS task_id,\n" +
            "\tt1.task_code,\n" +
            "\tt2.NAME \n" +
            "FROM\n" +
            "\ttest_task t1\n" +
            "\tLEFT JOIN sys_user t2 ON t1.receiver = t2.user_id\n" +
            "\tLEFT JOIN test_report_record AS t3 ON t3.entrustment_id = t1.entrustment_id \n" +
            "WHERE\n" +
            "\tt1.dept_id IN ( 229, 231, 232, 233, 234, 235, 236, 266, 267, 268 ) \n" +
            "\t\n" +
            "\tAND t3.issuer_time >= '2024-01-01' \n" +
            "\tAND t3.issuer_time <= '2024-01-31' \n" +
            "\t) tt \n" +
            "WHERE\n" +
            "\ttt.task_id not IN ( SELECT DISTINCT task_id FROM test_task_order_working_hours ) \n" +
            "ORDER BY\n" +
            "\ttt.NAME,\n" +
            "\ttt.task_code ASC")
    List<Long> getTaskList();

}
