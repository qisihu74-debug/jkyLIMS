package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;

/**
 * 产品与产品模板关联表(TestReportTemplateProductRef)表实体类
 *
 * @author makejava
 * @since 2022-04-13 09:49:09
 */
@SuppressWarnings("serial")
@Data
public class TestReportTemplateProductRef extends Model<TestReportTemplateProductRef> {
    //报告模板id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //产品ID
    private Integer productId;
    //模板ID
    private Integer templateId;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
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

