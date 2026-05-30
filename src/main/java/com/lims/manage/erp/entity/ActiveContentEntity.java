package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-26 11:27
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_mr_active_content")
public class ActiveContentEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 内审活动id
     */
    private Integer activeId;
    /**
     * 评审内容
     */
    private String content;
    /**
     * 涉及部门/人员
     */
    private String deptOrUser;

}
