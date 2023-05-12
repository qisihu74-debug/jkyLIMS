package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.ExcelInsertVo;
import com.zhuozhengsoft.pageoffice.FileSaver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: DLC
 * @Date: 2023/4/19 9:58
 *  在线编辑
 */
public interface PageOfficeService {

    /**
     * 返回的 原始记录表头 塞入 的URL
     * @param ids
     * @return
     */
    String getProductExcelUrl(Integer[] ids) throws Exception;

    /**
     * 完成编辑
     * @param request
     * @param file
     * @return
     */
    String saveOriginalRecord(HttpServletRequest request, FileSaver file) throws Exception;

    /**
     * 更新样品记录URL
     * @param excelUrl
     * @param ids
     * @return
     */
    String updateOriginalRecordUrl(String excelUrl,Integer[] ids) throws Exception;

    /**
     *  完成复核：中间检测项 及 最终复核
     * @param excelInsertVo
     * @param userId
     * @return
     */
    Boolean finishCheckItemReview(ExcelInsertVo excelInsertVo,Long userId) throws Exception;

    /**
     * 完成复核：根据检测项id 判断任务单
     * @param excelInsertVo
     * @return
     */
    String CompleteTheReview(ExcelInsertVo excelInsertVo);

    /**
     * 更新文件
     * @param array
     * @param inputStream
     * @return
     */
    String updateExcelVisible(String saveFileUrl,Integer[] array, InputStream inputStream) throws IOException;
}
