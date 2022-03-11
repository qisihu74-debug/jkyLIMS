package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ReportApprovalVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
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

    List<ReportApprovalVo> getReportApprovalList(@Param("search")String search,@Param("ids") Set<Long> list);

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


    List<ReportApprovalVo> getReportApprovalHistory(@Param("search")String search,@Param("ids") Set<Long> ids);

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
    List<ReportApprovalVo> getVerifyList(@Param("search")String search,@Param("ids") Set<Long> list);

    /**
     * 查询签发 历史
     * @param search
     * @return
     */
    List<ReportApprovalVo> getVerifyHistory(@Param("search")String search,@Param("ids") Set<Long> list);

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
     * 根据检测项 获取 检测项所属的 URL连接
     */
    @Select("SELECT origin_url  FROM `test_entrusted_sample_checkitem_rel` WHERE id =#{id}")
    String getCheckItemUrl(Long id);





}
