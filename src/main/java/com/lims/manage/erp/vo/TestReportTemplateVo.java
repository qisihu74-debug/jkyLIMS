package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.entity.TestReportTemplate;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TestReportTemplateVo {
    private TestReportTemplate testReportTemplate;
    //产品id
    private List<Integer> productIds;
}
