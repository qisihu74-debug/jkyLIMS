package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 专利授权信息
 */

@Data
@TableName("patent_authorization")
public class PatentAuthorization implements Serializable {

    //主键id
    @TableId(type = IdType.AUTO)
    private Integer id;
    //专利号
    private String patentId;
    //授权时间
    private Date authorizationDate;
    //授权人
    private String authorizationName;


}
