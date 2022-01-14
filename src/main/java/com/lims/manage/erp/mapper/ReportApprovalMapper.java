package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.ReportApprovalVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:57
 */
@Component
@Mapper
public interface ReportApprovalMapper {

    List<ReportApprovalVo> getReportApprovalList(@Param("search")String search);

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


    List<ReportApprovalVo> getReportApprovalHistory(@Param("search")String search);

    /**
     * 任务单详情
     * @param id
     * @return
     */
    TaskDetailInfoVo getTaskDetail(Long id);

    /**
     * 获取委托id 获取样品信息 以及检测信息
     */
    List<SampleDetailVo> getSampleDetailLis(Long id);





}
