package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class TestLaboratoryVo {
    private Integer id;
    //实验室名称
    private String name;
    //实验室编码
    private String code;
    //实验室管理员
    private String administintor;
    //实验室所在位置
    private String position;
    //联系方式
    private String phone;
    //照片
    private String picture;
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
    //数量
    private Integer quantity;
    @TableField(exist = false)
    private Integer pageSize;
    @TableField(exist = false)
    private Integer pageNum;
}
