package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.vo.ExcelInsertVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TeamVo;
import com.zhuozhengsoft.pageoffice.FileSaver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @Author: DLC
 * @Date: 2023/4/19 9:58
 *  在线编辑
 */
public interface PageOfficeCopyService {

    /**
     * 返回的 原始记录表头 塞入 的URL
     * @param ids
     * @return
     */
    String getProductExcelUrl(Integer[] ids,List<ExcelInsertVo> sheetItems,List<TaskIdEntity> dataEntitys) throws Exception;

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
    String updateOriginalRecordUrl(String excelUrl, Integer[] ids) throws Exception;

    /**
     *  完成复核：中间检测项 及 最终复核
     * @param excelInsertVo
     * @param userId
     * @return
     */
    Boolean finishCheckItemReview(ExcelInsertVo excelInsertVo, Long userId) throws Exception;

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
    String updateExcelVisible(String saveFileUrl, Integer[] array, InputStream inputStream) throws IOException;

    /**
     * 线上编辑原始记录模板时，根据任务单 选择检测人，记录人
     * @param teamVo
     * @param taskId
     * @return
     */
    TeamVo getTaskInspectorAndRecorder(List<LabelValueVo> teamVo, Long taskId);

    /**
     * 编辑原始数据
     *
     * @param ids
     * @return
     */
    Boolean editItemdData(Integer[] ids, String testSet, String recordSet);

    /**
     * 读取file中 签名信息
     *
     * @param file
     * @return
     */
    Map<String, String> getName(FileSaver file);

    /**
     * 2023年9月6日 试验完成：更新检测项中 签名信息
     *
     * @param ids 检测项主键集合
     * @return
     */
    String saveOriginalRecord2(Integer[] ids) throws Exception;

    QiYueSuoResponse createbycategoryBatch(QiYueSuoReqBean reqBean, List<String> stringList);
}
