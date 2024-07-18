package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-05 14:50
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_audit_base_data")
public class AduditBaseData {
    /**
     * id
     */
    private int id;
    /**
     * pid
     */
    private int pid;
    /**
     * 目录
     */
    private String directory;
    /**
     * 通用内容
     */
    private String content;
    /**
     * 检测方法
     */
    private String method;
    /**
     * 备注
     */
    private String note;
    /**
     * 类型：CMA,CNAS
     */
    private String type;
    /**
     * 排序
     */
    private int sort;
    @TableField(exist = false)
    private List<AduditBaseData> children;
    @TableField(exist = false)
    private String findings;
    @TableField(exist = false)
    private String opinion;
    @TableField(exist = false)
    private String record;
    public AduditBaseData(Integer id, Integer pid, String directory,String content,String method,String note,String type,int sort) {
        this.id = id;
        this.pid = pid;
        this.directory = directory;
        this.content = content;
        this.method = method;
        this.note = note;
        this.type = type;
        this.sort = sort;
        this.children = new ArrayList<>();
    }
}
