package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestProductItem;
import lombok.Data;

import java.util.List;
@Data
public class TestProductItemParamVo {
    //产品检测项基本信息
    private TestProductItem testProductItem;
    //产品检测项的检测方法
    private List<Integer> methodIds;
    //产品检测项的检测依据
    private List<Integer> standardIds;
    //产品检测项的设备
    private List<Integer> typeIds;
}
