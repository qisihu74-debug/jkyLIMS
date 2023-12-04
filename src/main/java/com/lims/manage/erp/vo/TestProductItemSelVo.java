package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestOriginalRecordTemplate;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.entity.TestProductItemMethodRel;
import lombok.Data;

import java.util.List;
@Data
public class TestProductItemSelVo {
    //产品检测项基本信息
    private TestProductItem testProductItem;
    //产品检测方法
    private List<TestProductItemMethodRel> methodList;
    //产品检测设备
    private List<String> typeList;
    //产品检测依据
    private List<String> itemStandardList;
    //检测项所属科室
    private List<String> teamList;
    //产品模板名称
    private String reportName;
    //子检测项
    private List<TestProductItemSelVo> children;
    //线下原始记录模板集合
    private List<TestOriginalRecordTemplate> templateSet;
}
