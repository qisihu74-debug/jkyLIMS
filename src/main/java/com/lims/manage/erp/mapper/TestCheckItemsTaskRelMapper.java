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
     * 查询 - 工时统计-我的工时
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
            "\tsum( t1.times * t2.report_model_id ) \n" +
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
}
