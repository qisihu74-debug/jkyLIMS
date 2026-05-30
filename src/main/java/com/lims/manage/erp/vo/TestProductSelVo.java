package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestProduct;
import lombok.Data;

import java.util.List;
@Data
public class TestProductSelVo {
    //产品基础信息
    private TestProduct testProduct;
    //产品类型名称
    private String productTypeName;
    //产品等级
    private List<String> specsList;
    //产品判定依据
    private List<String> standardList;
    //产品检测项
    private List<TestProductItemSelVo> TestProductItemSelVoList;
}
