package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2023-04-28 10:05
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_news")
public class NewsBean {
    @TableField
    private Long id;

    /**
     * 发布期数
     */
    private Integer nextNum;

    /**
     * 类型0技术质量
     */
    private Integer type;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容描述
     */
    private String content;

    /**
     * 发布部门
     */
    private String publishDept;

    /**
     * 发布人
     */
    private String publishUser;

    /**
     * 发布日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date publishDate;

    /**
     * 附件地址
     */
    private String fileUrl;

    /**
     * 附件url集合
     */
    @TableField(exist = false)
    private List<String> list;
}
