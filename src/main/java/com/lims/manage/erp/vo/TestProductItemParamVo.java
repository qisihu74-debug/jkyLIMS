package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.entity.TestProductItemMethodRel;
import lombok.Data;

import java.util.List;
@Data
public class TestProductItemParamVo {
    //产品检测项基本信息
    private TestProductItem testProductItem;
    //产品检测项的检测方法
    private List<TestProductItemMethodRel> methodIds;
    //产品检测项的检测依据
    private List<Integer> standardIds;
    //产品检测项的设备
    private List<Integer> typeIds;
    //产品检测项所属科室
    private List<Integer> itemIds;
    //产品绑定的报告模板下的sheet
    private List<LabelValueVo> templateSheet;
    //检测项绑定的sheet下标
    private List<Integer> sheetIndex;
    //线下原始记录模板集合
    private List<Integer> templateSet;
}
