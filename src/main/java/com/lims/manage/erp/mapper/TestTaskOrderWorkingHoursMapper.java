package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.HourCount;
import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface TestTaskOrderWorkingHoursMapper  extends BaseMapper<TestTaskOrderWorkingHours> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestTaskOrderWorkingHours record);

    int insertSelective(TestTaskOrderWorkingHours record);

    TestTaskOrderWorkingHours selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestTaskOrderWorkingHours record);

    int updateByPrimaryKey(TestTaskOrderWorkingHours record);

    /**
     * 根据人员 查询 工时列表
     *
     * @param taskStatisticsVo
     * @return
     */
    List<TestTaskOrderWorkingHours> selectTaskOrderWorkingHours(TaskStatisticsVo taskStatisticsVo);

    /**
     * 根据人员 查询 统计总供时
     *
     * @param taskStatisticsVo
     * @return
     */
    String selectTaskOrderCountWorkingHours(TaskStatisticsVo taskStatisticsVo);

    /**
     * 根据授权人员 查询 任务单工时列表-总工时
     *
     * @param taskStatisticsVo
     * @return
     */
    List<TestTaskOrderWorkingHours> selectTaskOrderTotalWorkingHours(TaskStatisticsVo taskStatisticsVo);

    /**
     * 根据授权人员 查询 任务单工时列表-总工时
     *
     * @param taskStatisticsVo
     * @return
     */
    String selectAuthorizedSignatureHours(TaskStatisticsVo taskStatisticsVo);

    /**
     * 根据授权签字人 查询信息
     *
     * @param taskStatisticsVo
     * @return
     */
    List<TestTaskOrderWorkingHours> selectTaskOrderList(TaskStatisticsVo taskStatisticsVo);

    /**
     * 通过委托单id 获取任务单id列表
     *
     * @param entrustId
     * @return
     */
    @Select("SELECT\n" +
            "\tid \n" +
            "FROM\n" +
            "\ttest_task \n" +
            "WHERE\n" +
            "\tentrustment_id = #{entrustId} \n" +
            "\tAND state != 144 and task_list_status is not null")
    List<Long> getTaskList(@Param("entrustId") Long entrustId);

    /**
     * 根据taskIds 查询返回数据
     *
     * @param taskIds
     * @return
     */
    List<TestTaskOrderWorkingHours> getTestTaskOrderWorkingHoursList(@Param("taskIds") List<Long> taskIds);

    /**
     * 工时信息
     * @param taskStatisticsVo
     * @return
     */
    List<TestTaskOrderWorkingHours> selectTaskOrderWorkingHoursList(TaskStatisticsVo taskStatisticsVo);

    @Select("SELECT\n" +
            "\tt1.user_id As userId,\n" +
            "\tt1.user_name As userName,\n" +
            "\tsum( working_hours ) As hours,\n" +
            "\tt5.id As teamId,\n" +
            "\tt5.pid,\n" +
            "\tt5.`name` As teamName \n" +
            "FROM\n" +
            "\ttest_task_order_working_hours t1\n" +
            "\tLEFT JOIN test_task t2 ON t1.task_id = t2.id\n" +
            "\tLEFT JOIN test_report_record t3 ON t2.entrustment_id = t3.entrustment_id\n" +
            "\tLEFT JOIN test_technicist t4 ON t1.user_id = t4.user_id\n" +
            "\tLEFT JOIN test_team t5 ON t4.team_id = t5.id \n" +
            "WHERE\n" +
            "\tt3.issuer_time >= #{startDate} \n" +
            "\tAND t3.issuer_time < #{stopDate} \n" +
            "GROUP BY\n" +
            "\tt1.user_id")
    List<HourCount> exportHours(Date startDate, Date stopDate);


    /**
     * 通过委托单id 获取旧任务单id列表
     *
     * @param entrustId
     * @return
     */
    @Select("SELECT\n" +
            "\tid \n" +
            "FROM\n" +
            "\ttest_task \n" +
            "WHERE\n" +
            "\tentrustment_id = #{entrustId} \n" +
            "\tAND state != 144 and task_list_status is null")
    List<Long> getTaskOldList(@Param("entrustId") Long entrustId);

    /**
     * 查询 taskId 工时是否整除：!null 整除
     *
     * @param taskId
     * @return
     */
    @Select("SELECT\n" +
            "\ttt.count \n" +
            "FROM\n" +
            "\t( SELECT *, COUNT( * ) AS count FROM test_task_order_working_hours WHERE task_id = #{taskId} GROUP BY task_id HAVING ROUND( sum( working_hours ), 4 ) != total_working_hours " +
            "or ROUND( sum( proportion ), 2 ) != 100) tt")
    String selectTaskOrderWorkingCount(@Param("taskId") Long taskId);

    /**
     * 查询任务单 获取总工时
     *
     * @param taskId
     * @return
     */
    @Select("SELECT sum(working_hours) FROM test_task_order_working_hours WHERE task_id = #{taskId} ")
    String selectTaskOrderWorkingSum(@Param("taskId") Long taskId);

    /**
     * 获取团队下人员比例
     *
     * @param teamId
     * @return
     */
    @Select("SELECT\n" +
            "\tid,\n" +
            "\tuser_name AS userName,\n" +
            "\tuser_id AS userId,\n" +
            "\tproportion AS proportion \n" +
            "FROM\n" +
            "\ttest_task_order_working_hours_scale \n" +
            "WHERE\n" +
            "\tteam_id = #{teamId} and add_operator = #{addOperator}")
    List<TestTaskOrderWorkingHours> selectManHourRatio(@Param("teamId") Long teamId, @Param("addOperator") String addOperator);
}
