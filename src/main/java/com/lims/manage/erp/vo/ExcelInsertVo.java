package com.lims.manage.erp.vo;

import lombok.Data;

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

}
