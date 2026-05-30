package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

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
@TableName("qs_mr_active_detail_file_rel")
public class ActiveDetailsFileUrlEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * qs_mr_active_detail 中 activeDetailId
     */
    private Integer activeDetailId;

    /**
     * 附件
     */
    private String fileUrl;

    /**
     * 附件名称
     */
    private String fileUrlName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 内审活动id
     */
    private Integer activeId;

}
