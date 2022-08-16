package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022-08-16 10:25
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_csos_img")
public class DynamicImg {
    @TableId
    private String id;
    private String title;
    private String imgUrl;
}
