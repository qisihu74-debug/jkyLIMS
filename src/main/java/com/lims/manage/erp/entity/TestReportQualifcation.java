package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (TestReportQualifcation)表实体类
 *
 * @author makejava
 * @since 2022-03-14 14:33:33
 */
@SuppressWarnings("serial")

@Data
@TableName("test_report_qualifcation")
public class TestReportQualifcation extends Model<TestReportQualifcation> {
    //主键id
    @TableId(type = IdType.AUTO)
    //主键自增
    private Integer id;
    //检测项id
    private Integer checkItemId;
    //检测项规范编号
    private String checkBasisCode;
    //自动计算类型
    private String countType;
    //指标内容
    private String specsContent;
    
    private String conditionKey;
    //前置条件值
    private String conditionValue;
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    @JsonFormat(pattern = "yyyy-MM-DD", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-DD", timezone = "GMT+8")
    private Date updateTime;
    //备注
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    public String getCheckBasisCode() {
        return checkBasisCode;
    }

    public void setCheckBasisCode(String checkBasisCode) {
        this.checkBasisCode = checkBasisCode;
    }

    public String getCountType() {
        return countType;
    }

    public void setCountType(String countType) {
        this.countType = countType;
    }

    public String getSpecsContent() {
        return specsContent;
    }

    public void setSpecsContent(String specsContent) {
        this.specsContent = specsContent;
    }

    public String getConditionKey() {
        return conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }
    }

