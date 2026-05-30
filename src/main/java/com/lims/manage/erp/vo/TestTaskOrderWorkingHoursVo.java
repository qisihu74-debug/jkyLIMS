package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;
import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/11/8 11:37
 */
@Data
public class TestTaskOrderWorkingHoursVo {

    List<TestTaskOrderWorkingHours> list;

    String workingHoursId;
}
