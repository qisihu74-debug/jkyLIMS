package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.HourCount;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    List<TeamOutputValueVo> teamParentTaskStatistics(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("deptIds") List<Long> deptIds);

    /**
     * @param beginDate
     * @param endDate
     * @param deptIds
     * @return
     */
    List<TeamOutputValueVo> teamStatistics0715(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("deptIds") List<Long> deptIds);

    List<TeamOutputValueVo> teamStatistics230419(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("deptIds") List<Long> deptIds);

    List<TeamOutputValueVo> teamStatistics231219(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("deptIds") List<Long> deptIds);

    List<TeamOutputValueVo> teamStatisticsNode0715(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("deptIds") List<Long> deptIds);

    /**
     * 部门产值统计--子级
     *
     * @param paramVo
     * @return
     */
    List<TeamOutputValueVo> teamStatisticsNode(StatisticsParamVo paramVo);

    List<TeamOutputValueVo> teamStatisticsNode0419(StatisticsParamVo paramVo);

    List<TeamOutputValueVo> teamStatisticsNode1219(StatisticsParamVo paramVo);

    /**
     * 废弃
     *
     * @param taskDetailInfoVo
     * @return
     */
    List<TaskStatsVo> getTaskList(TaskStatsVo taskDetailInfoVo);

    /**
     * 视图
     *
     * @param taskDetailInfoVo
     * @return
     */
    List<TaskStatsVo> getTaskListShow(TaskStatsVo taskDetailInfoVo);

    /**
     * entrustIds集合 查询 视图
     *
     * @param longs
     * @return
     */
    List<TaskStatsVo> getTaskInListShow(@Param("longs") List<Long> longs);

    /**
     * 查询区域信息
     *
     * @return
     */
    List<LabelValueVo> getAreas();

    /**
     * 通过委托单id 获取折扣率
     */
    String getDiscount(Long entrustmentId);

    /**
     * 查询任务单 获取发布人
     */
    List<TaskTestEntity> selectOrderTaskTest(PersonalStatsVo personalStats);

    @Select("SELECT\n" +
            "\tsum( task_price ) AS teamPrice \n" +
            "FROM\n" +
            "\ttest_task_statistics \n" +
            "WHERE\n" +
            "\tissuer_time >= #{startDate} \n" +
            "\tAND issuer_time < #{stopDate}")
    Double countDeptPriceByTime(@Param("startDate") Date startDate, @Param("stopDate") Date stopDate);

    /**
     * 查询委托单中 委托人次数
     *
     * @return
     */
    @MapKey("businessAcceptor")
    Map<String, Map<String, Object>> selectEntrustBusinessAcceptorMap(PersonalStatsVo personalStats);

    /**
     * 查询发布任务
     *
     * @return
     */
    @MapKey("orderer")
    Map<String, Map<String, Object>> selectReleaseTaskMap(PersonalStatsVo personalStats);

    /**
     * 查询试验检测
     *
     * @return
     */
    @MapKey("inspector")
    Map<String, Map<String, Object>> selectInspectorMap(PersonalStatsVo personalStats);

    /**
     * 查询复核人员
     *
     * @return
     */
    @MapKey("reviewer")
    Map<String, Map<String, Object>> selectReviewerMap(PersonalStatsVo personalStats);

    /**
     * 查询报告审核人员
     *
     * @return
     */
    @MapKey("verifyerId")
    Map<Long, Map<Long, Object>> selectreportApprovalMap(PersonalStatsVo personalStats);

    /**
     * 查询报告签发人员
     *
     * @return
     */
    @MapKey("issuerId")
    Map<Long, Map<Long, Object>> selectReportIssueMap(PersonalStatsVo personalStats);

    /**
     * 查询报告签发人员
     *
     * @return
     */
    @MapKey("sealer")
    Map<String, Map<String, Object>> selectReportSealerMap(PersonalStatsVo personalStats);

    /**
     * 查询报告制作人员
     *
     * @return
     */
    @MapKey("reportProducer")
    Map<String, Map<String, Object>> selectReportProducerMap(PersonalStatsVo personalStats);

    /**
     * 根据部门id 及 日期 统计具体报告清单
     *
     * @param teamId
     * @param beginDate
     * @param endDate
     * @return
     */
    List<StatisticsNodeDetailVo> getTeamStatisticsNode(@Param("teamId") String teamId, @Param("beginDate") String beginDate, @Param("endDate") String endDate);



    /**
     * 合格率分析 — 按检测大类统计判定结果
     */
    @Select("<script>" +
            "SELECT pt.product_type_id AS typeId, pt.product_type_name AS typeName, " +
            "  CAST(SUM(d.judge_result = '合格') AS SIGNED) AS qualified, " +
            "  CAST(SUM(d.judge_result = '不合格') AS SIGNED) AS unqualified, " +
            "  COUNT(*) AS total " +
            "FROM test_report_record_detail d " +
            "JOIN test_product_item pi ON d.check_item_id = pi.check_item_id " +
            "JOIN test_product p ON pi.product_id = p.product_id " +
            "JOIN test_product_type pt ON p.product_type_id = pt.product_type_id " +
            "LEFT JOIN test_task t ON d.task_id = t.id " +
            "WHERE d.judge_result IN ('合格','不合格') " +
            "<if test='startDate != null and startDate != \"\"'> AND t.create_time &gt;= #{startDate} </if>" +
            "<if test='stopDate != null and stopDate != \"\"'> AND t.create_time &lt; DATE_ADD(#{stopDate}, INTERVAL 1 DAY) </if>" +
            "GROUP BY pt.product_type_id, pt.product_type_name ORDER BY total DESC" +
            "</script>")
    List<QualificationRateVo> qualificationRate(@Param("startDate") String startDate, @Param("stopDate") String stopDate);


    /**
     * 设备利用率 — 各仪器使用次数与累计时长
     */
    @Select("<script>" +
            "SELECT i.name AS name, i.code AS code, i.laboratory_name AS lab, " +
            "  CAST(COUNT(*) AS SIGNED) AS useCount, " +
            "  ROUND(SUM(CASE WHEN r.end_time >= r.start_time THEN TIMESTAMPDIFF(MINUTE, r.start_time, r.end_time) ELSE 0 END)/60, 1) AS useHours " +
            "FROM test_instrument_use_record r " +
            "JOIN test_instrument i ON r.instrument_id = i.id " +
            "WHERE r.start_time IS NOT NULL " +
            "<if test='startDate != null and startDate != \"\"'> AND r.start_time &gt;= #{startDate} </if>" +
            "<if test='stopDate != null and stopDate != \"\"'> AND r.start_time &lt; DATE_ADD(#{stopDate}, INTERVAL 1 DAY) </if>" +
            "GROUP BY i.id, i.name, i.code, i.laboratory_name ORDER BY useCount DESC" +
            "</script>")
    List<InstrumentUsageVo> instrumentUsage(@Param("startDate") String startDate, @Param("stopDate") String stopDate);

}
