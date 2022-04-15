package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2022/4/15 9:59
 * test_sample 文件中间表 test_sample_file_table
 */
@Data
public class SampleFileTableEntity {
    /**
     * id
     */
    private Integer id;
    /**
     * 样品id
     */
    private Integer sampleId;
    /**
     * 附件url
     */
    private String fileUrl;
    /**
     * 附件url原始名称
     */
    private String fileUrlStr;
    /**
     * 创建时间
     */
    private Date carateTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
