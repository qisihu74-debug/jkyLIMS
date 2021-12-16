package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 17:33
 * 开始检测 数据 JSON
 */
@Data
public class SampleItemInstrumentVo {
    private Date startTime;
    private Date endTime;
    private String result;
    private Long taskId;
    List<SampleItemInstrumentEntity> itemInstrumentEntityList;
}
