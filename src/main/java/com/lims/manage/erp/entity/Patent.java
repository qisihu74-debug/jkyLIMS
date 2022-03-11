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
 * (Patent)表实体类
 *
 * @author makejava
 * @since 2022-03-08 10:40:15
 */
@SuppressWarnings("serial")
@Data
@TableName("patent")
public class Patent extends Model<Patent> {
    //主键id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //专利名称
    private String patentname;
    //专利时间
    @JsonFormat(pattern = "yyyy-MM-DD", timezone = "GMT+8")
    private Date patenttime;
    //申请人
    private String patenproposer;
    //0默认未删除,1删除
    private Integer delFlag;
    //资料或者描述
    private String patentms;
    //备注
    private String remark;
    //启动0 1冻结
    private String start;
    //图片
    private String url;

    private Integer producid;
    private  String producname;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPatentname() {
        return patentname;
    }

    public void setPatentname(String patentname) {
        this.patentname = patentname;
    }

    public Date getPatenttime() {
        return patenttime;
    }

    public void setPatenttime(Date patenttime) {
        this.patenttime = patenttime;
    }

    public String getPatenproposer() {
        return patenproposer;
    }

    public void setPatenproposer(String patenproposer) {
        this.patenproposer = patenproposer;
    }

    public String getPatentms() {
        return patentms;
    }

    public void setPatentms(String patentms) {
        this.patentms = patentms;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

