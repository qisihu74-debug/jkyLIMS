package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestItemOrderWorkingHours;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

public interface TestItemOrderWorkingHoursMapper extends BaseMapper<TestItemOrderWorkingHours> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestItemOrderWorkingHours record);

    int insertSelective(TestItemOrderWorkingHours record);

    TestItemOrderWorkingHours selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestItemOrderWorkingHours record);

    int updateByPrimaryKey(TestItemOrderWorkingHours record);

    /**
     * 根据taskId 获取检测项工时
     *
     * @param taskId
     * @return
     */
    List<TestItemOrderWorkingHours> selectTaskList(@Param("taskId") Long taskId);

    /**
     * 返回检测项总工时
     *
     * @param taskId
     * @param workingHoursId
     * @return
     */
    String getTotalWorkingHours(@Param("taskId") Long taskId, @Param("workingHoursId") Long workingHoursId);
}