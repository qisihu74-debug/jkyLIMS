package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;

public interface TestTaskOrderWorkingHoursMapper  extends BaseMapper<TestTaskOrderWorkingHours> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestTaskOrderWorkingHours record);

    int insertSelective(TestTaskOrderWorkingHours record);

    TestTaskOrderWorkingHours selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestTaskOrderWorkingHours record);

    int updateByPrimaryKey(TestTaskOrderWorkingHours record);
}