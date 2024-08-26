package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Component
@Mapper
public interface TestCheckItemsTaskRelMapper extends BaseMapper<TestCheckItemsTaskRel> {


    /**
     * 根据条件获取工时统计信息
     *
     * @param paramMap 查询条件
     * @return 工时统计信息列表
     */
    IPage<WorkHourStatisticVo> getWorkHoursList(Page<WorkHourStatisticVo> page, @Param("map") Map<String, Object> paramMap);

    /**
     * 查询 - 工时统计-我的工时-授权签字人能够看到，属于分配给自己工时也能看到。
     *
     * @param taskStatisticsVo
     * @return
     */
    List<TaskStatisticsVo> getMyHoursStatistics(TaskStatisticsVo taskStatisticsVo);

    /**
     * 通过任务单id 获取任务单下对应的检测项总工时
     *
     * @param taskId
     * @return
     */
    @Select("SELECT\n" +
            "\tIFNULL( sum( t1.times * t2.working_hours ), 0 )  \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel AS t1\n" +
            "\tLEFT JOIN test_product_item AS t2 ON t1.check_item_id = t2.check_item_id \n" +
            "WHERE\n" +
            "\tt1.task_id = #{taskId}")
    String getWorkingHours(@Param("taskId") Long taskId);

    /**
     * 查询 - 工时统计-我的工时-总计工时
     *
     * @param taskStatisticsVo
     * @return
     */
    String getMyHoursStatisticsSum(TaskStatisticsVo taskStatisticsVo);

    /**
     * 任务单id 获取 工时
     *
     * @param taskId
     * @return
     */
    @Select("SELECT DISTINCT task_id FROM test_task_order_working_hours WHERE task_id = #{taskId}")
    String getTaskIdWorkingHours(@Param("taskId") Long taskId);

    /**
     * 根据任务单id 查询所有任务大厅 已领取检测信息
     *
     * @param taskId
     * @return
     */
    List<TestCheckItemsTaskRel> selectAllDataBitValue(@Param("taskId") Long taskId);

    @Select("SELECT\n" +
            "\tid \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel\n" +
            "WHERE\n" +
            "\tentrust_id = #{entrustId} \n" +
            "\tAND check_item_id = #{checkItemId} \n" +
            "\tAND sample_id = #{sampleId}\n" +
            "\tlimit 1")
    Integer selectEntrustedSampleCheckitemId(@Param("entrustId") Long entrustId, @Param("sampleId") Integer sampleId, @Param("checkItemId") Long checkItemId);
}
