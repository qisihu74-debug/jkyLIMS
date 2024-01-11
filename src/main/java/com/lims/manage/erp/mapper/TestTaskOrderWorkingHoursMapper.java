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
            "\tAND t3.issuer_time <= #{stopDate} \n" +
            "GROUP BY\n" +
            "\tt1.user_id")
    List<HourCount> exportHours(Date startDate, Date stopDate);
}
