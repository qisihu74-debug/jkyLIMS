package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import org.apache.ibatis.annotations.Param;

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
     * 根据授权人员 查询 任务单工时列表-总工时
     *
     * @param taskStatisticsVo
     * @return
     */
    List<TestTaskOrderWorkingHours> selectTaskOrderTotalWorkingHours(TaskStatisticsVo taskStatisticsVo);

    /**
     * 根据授权签字人 查询信息
     * @param taskStatisticsVo
     * @return
     */
    List<TestTaskOrderWorkingHours> selectTaskOrderList(TaskStatisticsVo taskStatisticsVo);
}