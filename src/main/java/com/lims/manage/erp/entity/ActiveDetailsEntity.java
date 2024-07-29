package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 部门信息 上传附件详情
 *
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-26 11:27
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_mr_active_detail")
public class ActiveDetailsEntity {
    /**
     * 内审活动id
     */
    private Integer activeId;
    /**
     * 部门id
     */
    private Integer deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 附件
     */
    private String fileUrl;

}
