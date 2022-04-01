package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 16:23
 *  检测项 下仪器信息
 *  对表 test_entrusted_sample_checkitem_rel 补充说明
 */
@Data
public class SampleItemInstrumentEntity extends SampleItemEntity{
    /**
     * 检测项id
     */
    private Integer itemId;
    /**
     * 检测项 状态
     */
    private Integer state;
    /**
     * 原始记录上传url
     */
    private String originUrl;
    /**
     * 检测项 下 仪器id
     */
    List<Integer> ids;
    /**
     * 检测项 名称
     */
    private String checkItemName;
    /**
     *
     */
    private String opinion;

    /**
     * 文件附件
     */
    private String fileUrlStr;

    /***
     * 文件原始附件名称
     */
    private String itemFileName;

}
