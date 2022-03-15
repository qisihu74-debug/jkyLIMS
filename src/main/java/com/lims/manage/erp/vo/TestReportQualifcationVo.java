package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class TestReportQualifcationVo {
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
    //检测项id
    private String checkItemName;
}
