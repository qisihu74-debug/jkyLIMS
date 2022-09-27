package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface StatisticsMapper {

    /**
     * 任务查询
     */
    List<TaskStatsVo> selectTaskQuery(TaskStatsVo taskStatsVo);

    /**
     * 通过任务单id 和 委托信息
     *  获取样品名 和 所属的检测项价格及检测项状态
     */
    List<SampleEntity> selectSampleEntityList(Long taskId,Long entrustmentId);

    /**
     * 根据部门集合 获取人员信息
     */
    List<PersonalStatsVo> selectAllPerson(@Param("deptIds") List<Long> deptIds);

    /**
     * 历史委托查询 委托单所有信息
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryListRelease(PersonalStatsVo personalStats);

    /**
     * 查询任务单 获取试验检测人(邓喜旺&1647657004269101)
     */
    List<TaskTestEntity> selectTaskTest(PersonalStatsVo personalStats);

    /**
     * 查询任务单 获取复核人员(邓喜旺&1647657004269101)
     */
    List<TaskTestEntity> selectTaskReview(PersonalStatsVo personalStats);

    /**
     * 查询报告 获取 报告审批人(id)，报告签发人(id) 报告盖章(盖章)
     */
    List<ReportRecordEntity> selectReportSeal(PersonalStatsVo personalStats);

    /**
     * 查询报告 获取 报告审批人(id)
     */
    List<ReportRecordEntity> selectReportVerifyer(PersonalStatsVo personalStats);

    /**
     * 查询报告 获取 报告签发人(id)
     */
    List<ReportRecordEntity> selectReportIssuer(PersonalStatsVo personalStats);

    /**
     * 团队返回全部信息。
     */
    List<TestTeamVo> selectAllTeamVo();

    /**
     * 区域产值统计
     * @param paramVo
     * @return
     */
    List<AreaStatisticsResultVo> areaStatistics(StatisticsParamVo paramVo);
    int areaStatisticsEntrust(StatisticsParamVo paramVo);
    int areaStatisticsReport(StatisticsParamVo paramVo);

    /**
     * 部门产值统计--父级
     * @param paramVo
     * @return
     */
    List<TeamOutputValueVo> teamStatistics(StatisticsParamVo paramVo);

    /**
     * 父级报告产值
     * @param beginDate
     * @param endDate
     * @param deptIds
     * @return
     */
    List<TeamOutputValueVo> teamParentReportStatistics(@Param("beginDate") String beginDate,@Param("endDate") String endDate,@Param("deptIds") List<Long> deptIds);

    /**
     * 父级任务产值
     * @param beginDate
     * @param endDate
     * @param deptIds
     * @return
     */
    List<TeamOutputValueVo> teamParentTaskStatistics(@Param("beginDate") String beginDate,@Param("endDate") String endDate,@Param("deptIds") List<Long> deptIds);

    /**
     *
     * @param beginDate
     * @param endDate
     * @param deptIds
     * @return
     */
    List<TeamOutputValueVo> teamStatistics0715(@Param("beginDate") String beginDate,@Param("endDate") String endDate,@Param("deptIds") List<Long> deptIds);
    List<TeamOutputValueVo> teamStatisticsNode0715(@Param("beginDate") String beginDate,@Param("endDate") String endDate,@Param("deptIds") List<Long> deptIds);

    /**
     * 部门产值统计--子级
     * @param paramVo
     * @return
     */
    List<TeamOutputValueVo> teamStatisticsNode(StatisticsParamVo paramVo);

    List<TaskStatsVo> getTaskList(TaskStatsVo taskDetailInfoVo);

    /**
     * 查询区域信息
     * @return
     */
    List<LabelValueVo> getAreas();

    /**
     * 通过委托单id 获取折扣率
     */
    String getDiscount(Long entrustmentId);
}
