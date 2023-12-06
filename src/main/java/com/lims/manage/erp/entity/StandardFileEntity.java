package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("test_standard_file")
public class StandardFileEntity {
    private Integer id;
    private Integer pid;
    //文件类型1.检测依据，2判定依据，3既是检测又是判定，4其它
    private String type;
    //文件编号
    private String code;
    //文件名称
    private String name;
    //文件地址
    private String fileUrl;
    //标准类型，地标、国标、行标、企标、铁标、协会标准
    private String standardType;
    //失效日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expirationDate;
    // 0,启用，1,冻结
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
    //备注
    private String remark;
    //标准规范状态
    private String standardStatus;
    //发布日期
    private String releaseDate;
    //实施日期/作废日期
    private String implementationDate;
}
