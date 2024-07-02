package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2023-12-18 15:07
 * @Copyright © 河南交科院
 */
@Data
@TableName("test_technicist_files")
public class TechnicistFiles {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 档案文件类型1人员履历材料，2证件类材料，3培训类材料，4业绩类材料，5奖惩类材料，6其它材料
     */
    private Integer type;
    /**
     *材料内容
     */
    private String content;
    /**
     *操作人
     */
    private String operator;
    /**
     *操作时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date operateTime;
    /**
     *更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
    /**
     *技术人员id
     */
    private Integer technicistId;
    /**
     * 附件
     */
    private String fileUrl;

    private Long userId;
}
