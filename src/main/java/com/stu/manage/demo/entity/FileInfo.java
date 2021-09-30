package com.stu.manage.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/30 16:05
 * @Copyright © 河南交科院
 */
@Data
public class FileInfo {
    private Integer fileId;
    private Integer categoryId;
    private Integer fileType;
    private String fileName;
    private String path;
    private Date createTime;
    private Integer systemId;
    private Integer statusCode;
    private String sha;
    private Integer isDel;
    private String note;
    private String categoryName;

    private Integer currentVersion;
}
