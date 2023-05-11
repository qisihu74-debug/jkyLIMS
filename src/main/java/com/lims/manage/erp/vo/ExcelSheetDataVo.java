package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: DLC
 * @Date: 2023/5/11 8:58
 */
@Data
public class ExcelSheetDataVo {
    /**
     *  key = sheet标号 、value =序号
     */
    Map<Integer, Integer> countMap = new HashMap<>();
    // 文件路径
    String SaveFile;
}
