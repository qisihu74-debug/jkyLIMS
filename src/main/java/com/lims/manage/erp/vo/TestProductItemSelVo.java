package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestProductItem;
import lombok.Data;

import java.util.List;
@Data
public class TestProductItemSelVo {
    //产品检测项基本信息
    private TestProductItem testProductItem;
    //产品检测方法
    private List<String> methodList;
    //产品检测设备
    private List<String> typeList;
    //产品检测依据
    private List<String> itemStandardList;
    //产品模板名称
    private String reportName;
}
