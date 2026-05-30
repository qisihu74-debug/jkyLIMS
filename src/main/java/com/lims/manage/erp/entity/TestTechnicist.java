package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 技术人员(TestTechnicistVo)表实体类
 *
 * @author makejava
 * @since 2022-02-23 09:14:43
 */
@SuppressWarnings("serial")
public class TestTechnicist extends Model<TestTechnicist> {
    @TableId(type = IdType.AUTO)
    private Integer id;
    //用户编号
    private String userId;
    //团队编号
    private Integer teamId;
    //技术职称
    private String postName;
    //职称等级
    private String postGrade;
    //证书名称
    private String certificateName;
    //证书图片
    private String certificateUrl;
    //专业技能
    private String skill;
    // 0,启用，1,冻结
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //备注
    private String remark;
    @TableField(exist = false)
    private List<TestProductType> list;

    public List<TestProductType> getList() {
        return list;
    }

    public void setList(List<TestProductType> list) {
        this.list = list;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getPostGrade() {
        return postGrade;
    }

    public void setPostGrade(String postGrade) {
        this.postGrade = postGrade;
    }

    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

