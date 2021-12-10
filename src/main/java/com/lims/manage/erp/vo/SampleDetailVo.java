package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleEntity;
import lombok.Data;

import java.util.List;

@Data
public class SampleDetailVo extends SampleEntity {
    private String companyName;
    private String beginDate;
    private String endDate;
    private List<String> standardName;
    private List<CheckItemInfoVo> checkItemInfoList;
}
