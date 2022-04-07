package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SampleDetailAddVo {
    private String key;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品ID
     */
    private Integer productId;
    /**
     * 样品名称
     */
    private String aliasName;
    /**
     * 委托单位
     */
    private Integer companyId;
    /**
     * 样品产地
     */
    private String sampleOrigin;
    /**
     * 生产厂家
     */
    private String manufacturer;
    /**
     * 规格/等级
     */
    private String specs;
    /**
     * 代表批量
     */
    private String generation;
    /**
     * 样品数量
     */
    private String sampleQuantity;
    /**
     * 每组样品数量
     */
    private String quantityPerGroup;
    /**
     * 批号编号
     */
    private String batchNumber;
    /**
     * 样品备注
     */
    private String sampleRemark;
    /**
     * 验收员
     */
    private String inspector;
    /**
     * 收样时间
     */
    private Date receivedDate;
    /**
     * 样品要求
     */
    private String sampleRequirement;
    /**
     * 外观描述
     */
    private String outwardDescribe;
    /**
     * 外观
     */
    private List<String> outward;
    /**
     * 样品类型：做原材检测还是配合比检测
     */
    private String sampleType;
}
