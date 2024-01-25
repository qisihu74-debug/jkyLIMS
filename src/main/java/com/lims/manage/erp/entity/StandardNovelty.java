package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-01-23 10:52
 * @Copyright © 河南交科院
 */
@Data
@TableName("test_standard_novelty")
@AllArgsConstructor
@NoArgsConstructor
public class StandardNovelty {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 规范编号
     */
    private String code;
    /**
     * 规范名称
     */
    private String name;
    /**
     * 查新时间
     */
    private String findDate;
    /**
     * 规范状态
     */
    private String status;
    /**
     * 发布日期
     */
    private String releaseDate;
    /**
     * 实施日期/作废日期
     */
    private String implementationDate;
    @TableField(exist = false)
    private String note;
}
