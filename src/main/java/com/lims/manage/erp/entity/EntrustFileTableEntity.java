package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2022/7/8 9:59
 * test_entrusted_info 文件中间表 test_entrust_file_rel
 */
@Data
public class EntrustFileTableEntity {
    /**
     * id
     */
    private Integer id;
    /**
     * 样品id
     */
    private Long entrustId;
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
