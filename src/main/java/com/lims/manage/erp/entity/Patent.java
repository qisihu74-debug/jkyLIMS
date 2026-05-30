package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * (Patent)表实体类
 *
 * @author makejava
 * @since 2022-03-08 10:40:15
 */
@Data
@TableName("patent")
public class Patent extends Model<Patent> {
    //主键id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //专利名称
    private String patentName;
    //专利号
    private String patentId;
    //专利权人
    private String patentee;
    //发明人
    private String inventor;
    //专利类型   发明专利/实用新型
    private String patentType;

    //授权公告日(时间)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date announcementTime;
    //授权公告号(编码)
    private String announcementId;
    //所属产品
    private String product;
    //所属产品ID
    private String productId;
    //专利内容摘要
    private String synopsis;
    //到期时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date maturityTime;
    //创建时间
    private Date createTime;
    //0默认未删除,1删除
    private Integer delFlag;

    @TableField(exist = false)
    private List<PatentAuthorization> patentAuthorizations;

}

