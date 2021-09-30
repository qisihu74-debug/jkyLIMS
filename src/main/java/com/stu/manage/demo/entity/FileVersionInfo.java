package com.stu.manage.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/30 16:25
 * @Copyright © 河南交科院
 */
@Data
public class FileVersionInfo {
    private Integer fileId;
    private Integer fileVersion;
    private Date updateTime;
    private String updateLoginNo;
    private String sha;
    private String note;
    // 新的下载 1  下载文件版本
    private String isWithUpload;
}
