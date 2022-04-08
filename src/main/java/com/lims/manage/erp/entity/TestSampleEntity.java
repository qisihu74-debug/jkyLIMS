package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lims.manage.erp.vo.SampleDetailAddVo;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@TableName("test_sample")
public class TestSampleEntity {
    private Integer id;

    private Integer companyId;

    private String sampleName;

    private String sampleCode;

    private String specs;

    private String batchNumber;

    private String manufacturer;

    private String sampleOrigin;

    private String outward;

    private String savePlace;

    private String admin;

    private Integer sampleGroups;

    private Integer quantityPerGroup;

    private String inspector;

    private String receivedDate;

    private String sampleRequirement;

    private String generation;

    private String state;

    private Date checkDate;

    private String remark;

    private String file;

    private Integer productId;

    private String outwardDescribe;

    private String insertFlag;

    private Integer isUse;

    private String sampleQuantity;

    private String aliasName;

    private String sampleType;

    private String sampleRemark;

    private String picture;

    @TableField(exist = false)
    private Integer pageNum;
    @TableField(exist = false)
    private Integer pageSize;
    @TableField(exist = false)
    private String beginDate;
    @TableField(exist = false)
    private String endDate;
    @TableField(exist = false)
    private String companyName;
    @TableField(exist = false)
    private List<String> outwardArr;


    public TestSampleEntity() {
    }

    public TestSampleEntity(SampleDetailAddVo vo,String sampleCode) {
        this.companyId = vo.getCompanyId();
        this.sampleName = vo.getAliasName();
        this.sampleCode = sampleCode;
        this.specs = vo.getSpecs();
        this.batchNumber = vo.getBatchNumber();
        this.manufacturer = vo.getManufacturer();
        this.sampleOrigin = vo.getSampleOrigin();
        this.outward = vo.getOutward() == null ? null : vo.getOutward().toString();
        this.inspector = vo.getInspector();
        this.receivedDate = new SimpleDateFormat("yyyy-MM-dd").format(vo.getReceivedDate());
        this.sampleRequirement = vo.getSampleRequirement();
        this.generation = vo.getGeneration();
        this.state = 0+"";
        this.productId = vo.getProductId();
        this.outwardDescribe = vo.getOutwardDescribe();
        this.isUse = 0;
        this.sampleQuantity = vo.getSampleQuantity();
        this.quantityPerGroup = vo.getQuantityPerGroup();
        this.aliasName = vo.getAliasName();
        this.sampleType = vo.getSampleType();
        this.sampleRemark = vo.getSampleRemark();
    }
}