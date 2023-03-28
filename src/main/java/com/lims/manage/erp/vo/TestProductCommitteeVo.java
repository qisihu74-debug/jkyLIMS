package com.lims.manage.erp.vo;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 产品委员会
 */
@Data
public class TestProductCommitteeVo extends Model<TestProductCommitteeVo> {
    //产品委员会ID
    private Integer councilId;
    //用户ID
    private Integer userId;
    //委员名称
    private String councilName;
    //所属部门
    private String department;
    //所属团队
    private String position;
    //委员身份
    private String councilIdentity;

    //0默认未删除,1删除
    private Integer delFlag;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
