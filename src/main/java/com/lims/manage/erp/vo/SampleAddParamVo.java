package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SampleAddParamVo {
    /**
     *样品名称
     */
    private String sampleName;
    /**
     *委托单位
     */
    private Integer companyId;
//    /**
//     * 客户名称
//     */
//    private String customerName;
    /**
     *样品产地
     */
    private String sampleOrigin;
    /**
     *生产厂家
     */
    private String manufacturer;
    /**
     *规格/等级
     */
    private Integer specs;
    /**
     * 收样时间
     */
    private Date receivedDate;
    /**
     *代表批量
     */
    private String generation;
    /**
     *检测组数
     */
    private Integer sampleGroups;
    /**
     *每组数量
     */
    private Integer quantityPerGroup;
    /**
     * 样品要求
     */
    private String sampleRequirement;
    /**
     * 验收员
     */
    private String inspector;
    /**
     * 外观
     */
    private String outward;
    /**
     * 每个样品的批次，照片
     */
    private List<SampleAddDetailVo> details;

}
