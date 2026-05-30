package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleEntity;
import lombok.Data;

import java.util.List;

@Data
public class SampleDetailVo extends SampleEntity {
    private String companyName;
    private String beginDate;
    private String endDate;
    private Integer pageNum;
    private Integer pageSize;
    private List<String> standardName;
    private List<CheckItemInfoVo> checkItemInfoList;
    private List<String> codeList;
    private String receivedDate;
    private String q;
    private String sampleRetentionArea;
}
