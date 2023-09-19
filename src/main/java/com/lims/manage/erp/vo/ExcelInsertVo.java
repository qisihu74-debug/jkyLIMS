package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.*;

/**
 * @Author: DLC
 * @Date: 2023/4/25 11:27
 * excel 的检测、记录人 指向的图片
 */
@Data
public class ExcelInsertVo {
    /**
     * 记录类型：例如 检测：
     */
    String recordType;
    /**
     * 签名图片数组
     */
    String[] imags;
    /**
     * sheet名称
     */
    String sheetName;
    /**
     * 设置图表插入的位置
     */
    int topRow;
    /**
     * 设置图表插入的位置
     */
    int leftColumn;
    /**
     * 报告URL附件
     */
    String reportEditUrl;
    /**
     * 产品URL附件
     */
    String productExcelUrl;
    /**
     * 检测项id集合
     */
    List<Integer> list = new ArrayList<>();
    /**
     * 类型（中间复核 或 最终复核）
     */
    String checkReview;
    /**
     * 仪器设备使用记录是否填写（是 或 否）
     */
    String instrumentStatus;
    /**
     * 原始记录是否填写完整/ 电子记录是否备份（是 或 否）
     */
    String originalRecordStatus;
    /**
     * 数据处理是否正确（是 或 否）
     */
    String data;
    /**
     * （通过 或  驳回）
     */
    String status;
    /**
     * 审批意见
     */
    String opinion;
    /**
     * 检测项主键
     */
    Integer itemId;
    /**
     *  0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
     */
    Integer state;
    /**
     * 存放 原始记录中 签名信息
     */
    Map<String,Object> map  = new HashMap<>();
    /**
     * check_item_id
     */
    Integer checkItemId;
    /**
     * sheet 下标
     */
    Integer sheetIndex;
    /**
     * 检测人
     */
    String testSetUrl;
    /**
     * 记录人
     */
    String recordSetUrl;
    /**
     * 审核人
     */
    String reviewedBySetUrl;
    /**
     * 是否编辑数据
     */
    String editData;
    /**
     * 试验完成后生成pdf文件
     */
    String originUrlPdf;
    /**
     * 检测项编号
     */
    String checkItemCode;
    /**
     * 检测项名称
     */
    String checkItemName;
    /**
     * 附件url
     */
    String signUrl;

}
