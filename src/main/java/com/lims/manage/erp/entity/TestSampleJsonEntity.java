package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/30 16:54
 * 样品基础信息 JSON 回传数据
 */
@Data
public class TestSampleJsonEntity {
    /**
     *主键id
     */
    private Integer id;
    /**
     *委托单位
     */
    private Integer companyId;
    /**
     *样品名称
     */
    private String sampleName;
    /**
     *样品编号
     */
    private String sampleCode;
    /**
     *规格/等级
     */
    private Integer specs;
    /**
     *批号、编号
     */
    private String batchNumber;
    /**
     *生产厂家
     */
    private String manufacturer;
    /**
     *样品产地
     */
    private String sampleOrigin;
    /**
     *外观
     */
    private String outward;
    /**
     *样品照片
     */
    private String picture;
    /**
     *保存地点
     */
    private String savePlace;
    /**
     *管理员
     */
    private String admin;
    /**
     *样品组数
     */
    private Integer sampleGroups;
    /**
     *每组样品数
     */
    private Integer quantityPerGroup;
    /**
     *验收员
     */
    private String inspector;
    /**
     *接收日期
     */
    private String receivedDate;
    /**
     *样品要求
     */
    private String sampleRequirement;
    /**
     *代表批量
     */
    private String generation;
    /**
     *样品状态
     */
    private String state;
    /**
     *检验时间
     */
    private Date checkDate;
    /**
     *备注
     */
    private String remark;

    List<TestSampleCollectionJSON> sampleCollectionJSONList;

}
