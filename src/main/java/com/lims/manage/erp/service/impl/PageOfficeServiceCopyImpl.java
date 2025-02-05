package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.aspose.cells.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.http.QiYueSuoDocment;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.job.QiYueSuoHnadler;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.PageOfficeCopyService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import com.zhuozhengsoft.pageoffice.FileSaver;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: DLC
 * @Date: 2023/4/19 9:59
 * @Date: 2023/6/1 9:30
 */
@Service
@Slf4j
public class PageOfficeServiceCopyImpl implements PageOfficeCopyService {
    private final static Logger logger = LoggerFactory.getLogger(PageOfficeServiceCopyImpl.class);
    @Autowired
    TaskServiceImpl taskService;
    @Value("${autograph.path}")
    private String dir;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TestProductItemDao testProductItemDao;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;
    @Autowired
    private TestDetectionDao testDetectionDao;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;
    @Autowired
    private QiYueSuoHnadler qiYueSuoHnadler;
    @Autowired
    private ReportRecordEntityMapper recordEntityMapper;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;

    /**
     * 处理合并完整的excel每个报告的页码
     *
     * @param document
     * @param countMap
     * @param map      key= checkItemId， value对应的下标数据
     */
    private static void handlerPage(Workbook document, Map<Integer, Integer> countMap, Map<Integer, Object> map) {
        Set<Integer> keySet = map.keySet();
        for (Integer checkItemId : keySet) {
            List<ExcelInsertVo> insertVos = (List<ExcelInsertVo>) map.get(checkItemId);
            for (ExcelInsertVo data : insertVos) {
                Worksheet worksheet = document.getWorksheets().get(data.getSheetIndex());
                countMap.put(data.getSheetIndex(), data.getNumber());
                Cells cells = worksheet.getCells();
                int maxRow = cells.getMaxRow();
                int column = cells.getMaxColumn();
                for (int n = 0; n < maxRow; n++) {
                    for (int j = 0; j < column; j++) {
                        Cell cell = cells.get(n, j);
                        if (cell != null) {
                            Object value = cell.getValue();
                            if (value != null) {
                                String string = value.toString();
                                String regex = "第.*\\s.*页，共.*\\s.*页";
                                Pattern pattern = Pattern.compile(regex);
                                if (pattern.matcher(string).matches()) {
                                    cells.get(n, j).setValue("第" + data.getStartPag() + "页，共" + insertVos.size() + "页");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理合并完整的excel每个报告的页码
     *
     * @param document
     * @param countMap
     * @param map      key= sheet页， value对应的sheet内容数据
     */
    private static void handlerPageNew(Workbook document, Map<Integer, Integer> countMap, Map<Integer, Object> map) {
        for (Integer sheetIndex : map.keySet()) {
            ExcelInsertVo data = (ExcelInsertVo) map.get(sheetIndex);
            Worksheet worksheet = document.getWorksheets().get(data.getSheetIndex());
            countMap.put(data.getSheetIndex(), data.getNumber());
            Cells cells = worksheet.getCells();
            int maxRow = cells.getMaxRow();
            int column = cells.getMaxColumn();
            for (int n = 0; n < maxRow; n++) {
                for (int j = 0; j < column; j++) {
                    Cell cell = cells.get(n, j);
                    if (cell != null) {
                        Object value = cell.getValue();
                        if (value != null) {
                            String string = value.toString();
                            String regex = "第.*\\s.*页，共.*\\s.*页";
                            Pattern pattern = Pattern.compile(regex);
                            if (pattern.matcher(string).matches()) {
                                cells.get(n, j).setValue("第" + data.getStartPag() + "页，共" + data.getMaxPage() + "页");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 通过检测项主键 获取样品生成附件是否存在。
     *
     * @param itemId
     * @return
     */
    public ExcelSheetDataVo getProductInputStream(ExcelInsertVo excelInsertVo, Integer itemId) {
        ExcelSheetDataVo excelSheetDataVo = new ExcelSheetDataVo();
        InputStream fileStream = null;
        String productExcelUrl = null;
        if (excelInsertVo != null) {
            if (excelInsertVo.getReportEditUrl() != null) {
                // 优先拿 报告附件
                productExcelUrl = excelInsertVo.getReportEditUrl();
            }
            if (excelInsertVo.getReportEditUrl() == null && excelInsertVo.getProductExcelUrl() != null) {
                // 其次拿 产品附件
                productExcelUrl = excelInsertVo.getProductExcelUrl();
            }
        } else {
            productExcelUrl = null;
        }
        if (StringUtils.isEmpty(productExcelUrl)) {
            // -- 开始： 读取产品附件
            productExcelUrl = testProductItemDao.getProductExcel(itemId);
            if (StringUtils.isEmpty(productExcelUrl)) {
                logger.info("读取产品excel为null  产品主键中产品无产品附件");
            }
            try {
                // 获取公网 附件
                fileStream = FileAndFolderUtil.getInputStream(productExcelUrl);
            } catch (Exception e) {
                logger.info("读取产品excel异常 " + productExcelUrl + e);
            }
        } else {
            // 获取公网 附件
            try {
                fileStream = FileAndFolderUtil.getInputStream(productExcelUrl);
            } catch (Exception e) {
                logger.info("样品附件 " + productExcelUrl + e);
            }
        }
        excelSheetDataVo.setFileStream(fileStream);
        excelSheetDataVo.setProductExcelUrl(productExcelUrl);
        return excelSheetDataVo;
    }
    @Override
    public String getProductExcelUrl(Integer[]
                                             ids, List<ExcelInsertVo> sheetItems, List<TaskIdEntity> dataEntitys) throws Exception {
        String productExcelUrl = null;
        ExcelInsertVo excelInsertVo = testProductItemDao.getExcelUrl(ids[0]);
        Integer itemId = ids[0];
        InputStream fileStream = null;
        // 调用函数 获取 数据内容
        ExcelSheetDataVo productInputStream = getProductInputStream(excelInsertVo, itemId);
        fileStream = productInputStream.getFileStream();
        productExcelUrl = productInputStream.getProductExcelUrl();
        InputStream input = null;
        // 记录检测项中 对应的记录编号
        Map<Integer, String> recordNumberMap = new HashMap<>();
        // 塞入 原始记录 表头部分
        ExcelSheetDataVo excelSheetDataVo = getSaveFile(fileStream, dataEntitys.get(0).getTaskId());
        FileInputStream inputStream = new FileInputStream(new File(excelSheetDataVo.getSaveFile()));
        // 创建一个 XSSFWorkbook 对象，用于处理 .xlsx 格式的 Excel 文件
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        FileAndFolderUtil.delete(excelSheetDataVo.getSaveFile());
        // 通过任务单 判断当前下单时间
        //处理原始记录下载，单位名称问题
        java.sql.Date date = entrustEntityMapper.getEntrustDateByTaskId(dataEntitys.get(0).getTaskId());
        // 通过检测项主键 获取委托单下所有的样品id数据
        List<Integer> sampleIds = testProductItemDao.selectCountSampleIds(ids[0]);
        // 样品id 序号
        Map<Integer, Integer> sampleMapSerial = new HashMap<>();
        for (int j = 0; j < sampleIds.size(); j++) {
            Integer sampleId = sampleIds.get(j);
            // 统计委托单下 所有的样品组数。
            sampleMapSerial.put(sampleId, j + 1);
        }
        String dayString = DateUtil.getDayString(date.getTime());
        // status = true;(检测单位名称：河南交科院检验检测认证有限公司)
        // 否则 status = false; （检测单位名称：河南省公路工程试验检测中心有限公司）
        Boolean status = false;
        if (Integer.parseInt(dayString) >= 20230313) {
            // 检测单位名称：河南交科院检验检测认证有限公司
            status = true;
        }
        try {
            // 动态插入 头部信息:1、批量获取 检测项id（有可能对应多个模板） 再进行填充。2、通过检测项id 获取 相应的 id关联信息。
            methodInsertHeadTemplate(dataEntitys, sheetItems, wb, excelSheetDataVo, recordNumberMap, sampleMapSerial, status);
        } catch (Exception e) {
            System.out.println("编辑原始记录异常抛出");
            e.printStackTrace();
        }
        input = AsposeUtil.createExcelStream(wb);
        // 更新 检测项记录编号
        if (recordNumberMap != null) {
            for (int keyData : recordNumberMap.keySet()) {
                ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                excelInsertVo1.setItemId(keyData);
                excelInsertVo1.setCheckItemCode(recordNumberMap.get(keyData));
                testProductItemDao.updateItemData(excelInsertVo1);
            }
        }
        if (input != null) {
            // 把 wb 数据 存放上传
            String[] array = productExcelUrl.split("\\.");
            if (excelInsertVo == null) {
                String excelUrl = MinIoUtil.upload("file-resources", GenID.getID() + "." + array[array.length - 1], input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                input.close();
                fileStream.close();
                // 更新 样品Excel附件
                testProductItemDao.updateProductExcelUrl(dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId(), excelUrl);
                return excelUrl;
            } else {
                fileStream.close();
                // 私有方法 更新 产品附件及报告附件内容。
                return methodUpdateItemUrl(GenID.getID() + "." + array[array.length - 1], input, ids, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
            }
        }
        return productExcelUrl;
    }

    private void methodInsertHeadTemplate(List<TaskIdEntity> dataEntitys, List<ExcelInsertVo> sheetItems, XSSFWorkbook wb,
                                          ExcelSheetDataVo excelSheetDataVo, Map<Integer, String> recordNumberMap, Map<Integer, Integer> sampleMapSerial, Boolean status) {
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < dataEntitys.size(); i++) {
            TaskIdEntity data = dataEntitys.get(i);
            items.add(data.getIdItem());
        }
        // 1、 获取每组检测项的 数据（试验检测日期、试验条件、主要仪器设备名称及编号）
        Map<Integer, Map<String, String>> mapMap = methodHashMapItem(items, sheetItems);
        for (int i = 0; i < dataEntitys.size(); i++) {
            TaskIdEntity data = dataEntitys.get(i);
            // 检测项 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回 && 检测项对应的sheet 不为空
            if (data != null && !data.getState().equals(3) && !CollectionUtils.isEmpty(sheetItems)) {
                for (ExcelInsertVo excelInsertVo1 : sheetItems) {
                    // 获取sheetIndex工作表
                    XSSFSheet sheet = wb.getSheetAt(excelInsertVo1.getSheetIndex());
                    // sheet != null && checkItemId 相等
                    if (sheet != null && excelInsertVo1.getCheckItemId().equals(data.getCheckItemId())) {
                        //获取工作表的名称
                        String sheetName = sheet.getSheetName();
                        if (keyMap.get(sheetName + data.getCheckItemId()) == null) {
                            keyMap.put(sheetName + data.getCheckItemId(), sheetName);
                            if (data.getEditData() == null) {
                                // 首次填充 设置 sheet页左下角 日期
                                try {
                                    methodSheetTime(sheet, excelSheetDataVo, excelInsertVo1.getSheetIndex(), data.getStartTime());
                                } catch (Exception e) {
                                    log.error("编辑原始记录异常抛出");
                                    e.printStackTrace();
                                }
                                try {
                                    // 首次填充 设置原始记录 头部信息
                                    methodSheetHead(data, sampleMapSerial, excelInsertVo1.getSampleId(), recordNumberMap, sheet, excelInsertVo1.getSheetIndex(), status, mapMap);
                                } catch (Exception e) {
                                    log.error("编辑原始记录异常抛出");
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    // 原始记录已经编辑过 查询：试验时间、试验条件、仪器设备编号。
                                    methodDynamicModificationSheetHead(mapMap, excelInsertVo1.getItemId(), sheet, excelInsertVo1.getSampleId(), excelInsertVo1.getSheetIndex());
                                } catch (Exception e) {
                                    System.out.println("编辑原始记录异常抛出");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置sheet页的左下角日期
     */
    private void methodSheetTime(XSSFSheet sheet, ExcelSheetDataVo excelSheetDataVo, Integer sheetIndex, Date startTime) {
//         key = sheet标号 、value = ExcelInsertVo 中 topRow、leftColumn
        Map<Integer, ExcelInsertVo> indexDataXYMap = excelSheetDataVo.getIndexDataXYMap();
        ExcelInsertVo insertVo = indexDataXYMap.get(sheetIndex);
        // 2、试验检测日期 -- 后期比较
        SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyy年MM月dd日");
        String startTimestr = yyyyMMddHH_NOT_.format(startTime).substring(0, 11);
        sheet.getRow(insertVo.getTopRow()).getCell(insertVo.getLeftColumn()).setCellValue(startTimestr);
    }

    /**
     * 设置sheet页的头部信息
     */
    private void methodSheetHead(TaskIdEntity data, Map<Integer, Integer> sampleMapSerial, Integer sampleId, Map<Integer, String> recordNumberMap,
                                 XSSFSheet sheet, Integer sheetIndex, Boolean status, Map<Integer, Map<String, String>> mapMap) {
        // 根据样品id 与sheet页 获取当前对应的标识符数据
        TaskIdEntity sheetRelHeadContextData = sampleEntityMapper.selectTestItemSheetRelHeadContextData(sampleId, sheetIndex);
        // 有序信息。
        OriginalRecordDataVo originalData = taskService.getOriginalData(data.getTaskId(), data.getSampleId(), data.getCheckItemId(), data.getIdItem());
        Map<String, String> mapStr = mapMap.get(data.getIdItem());
        if (sheetRelHeadContextData != null) {
            // 获取仪器记录信息
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(sheetRelHeadContextData.getEquipmentText())) {
                // 记录模板的标记信息
                originalData.setEquipmentText(sheetRelHeadContextData.getEquipmentText());
                StringBuffer equipmentBuffer = new StringBuffer();
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(mapStr.get("equipment"))) {
                    equipmentBuffer.append(mapStr.get("equipment"));
                    equipmentBuffer.append("、");
                }
                equipmentBuffer.append(sheetRelHeadContextData.getEquipmentText());
                String[] array1 = equipmentBuffer.toString().split("、");
                Set<String> strings = new HashSet<>();
                for (int i = 0; i < array1.length; i++) {
                    strings.add(array1[i]);
                }
                StringBuffer stringBuffer = new StringBuffer();
                for (String str : strings) {
                    stringBuffer.append(str);
                    stringBuffer.append("、");
                }
                originalData.setEquipment(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
            }
        }
        Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
        // 设置组数
        int number = sampleMapSerial.get(sampleId);
        originalData.setRecordNumber(originalData.getRecordNumber());
        // 获取检测项中记录编号
        if (recordNumberMap.get(data.getIdItem()) == null) {
            recordNumberMap.put(data.getIdItem(), originalData.getRecordNumber() + "&" + GenID.getID());
        } else {
            String recordNumber = recordNumberMap.get(data.getIdItem());
            recordNumberMap.put(data.getIdItem(), recordNumber + "," + originalData.getRecordNumber() + "&" + GenID.getID());
        }
        originalData.setTestCondition(mapStr.get("testCondition"));
        originalData.setTestDate(mapStr.get("testDate"));
        result.put("result", originalData);
        // 替换原始记录模板数据
        ExcelReplaceUtil.ExcelReplace(sheet, result, status);
        if (sheetRelHeadContextData == null) {
            // 存储检测项标识符数据：
            TaskIdEntity sheetData = new TaskIdEntity();
            sheetData.setSampleId(sampleId);
            sheetData.setSheetIndex(sheetIndex);
            sheetData.setTestDateText(originalData.getTestDate() == null ? null : originalData.getTestDate());
            sheetData.setTestConditionText(originalData.getTestCondition() == null ? null : originalData.getTestCondition());
            sheetData.setEquipmentText(originalData.getEquipment() == null ? null : originalData.getEquipment());
            sampleEntityMapper.addTestItemSheetRelHeadContext(sheetData);
        } else {
            // 更新操作
            TaskIdEntity sheetData = new TaskIdEntity();
            sheetData.setSampleId(sampleId);
            sheetData.setSheetIndex(sheetIndex);
            sheetData.setTestDateText(originalData.getTestDate() == null ? null : originalData.getTestDate());
            sheetData.setTestConditionText(originalData.getTestCondition() == null ? null : originalData.getTestCondition());
            sheetData.setEquipmentText(originalData.getEquipment() == null ? null : originalData.getEquipment());
            sampleEntityMapper.updateTestItemSheetRelHeadContext(sheetData);
        }
    }

    /**
     * 使用方法 动态 调整 sheet参数
     */
    private void methodDynamicModificationSheetHead(Map<Integer, Map<String, String>> mapMap, Integer itemId, XSSFSheet sheet, Integer sampleId, Integer sheetIndex) {
//        TaskIdEntity data = null;
        // 进行originalData补充 检测项信息。
        OriginalRecordDataVo originalData = new OriginalRecordDataVo();
        // 根据样品id 与sheet页 获取当前对应的标识符数据
        TaskIdEntity sheetRelHeadContextData = sampleEntityMapper.selectTestItemSheetRelHeadContextData(sampleId, sheetIndex);
        // 标识符数据
        Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
        // 录入信息
        Map<String, String> map = mapMap.get(itemId);
        // 试验检测日期
        originalData.setTestDate(org.apache.commons.lang.StringUtils.isNotEmpty(map.get("testDate")) ? map.get("testDate") : sheetRelHeadContextData.getTestDateText());
        // 试验条件
        originalData.setTestCondition(org.apache.commons.lang.StringUtils.isNotEmpty(map.get("testCondition")) ? map.get("testCondition") : sheetRelHeadContextData.getTestConditionText());
        // 为检测项对应的 sheet数据
        // 检测日期
        if (org.apache.commons.lang.StringUtils.isNotEmpty(sheetRelHeadContextData.getTestDateText())) {
            // 1、说明标识符已经被替换 使用新的内容文本
            originalData.setTestDateText(sheetRelHeadContextData.getTestDateText());
        }
        // 试验条件 - Text
        if (org.apache.commons.lang.StringUtils.isNotEmpty(sheetRelHeadContextData.getTestConditionText())) {
            // 1、说明标识符已经被替换 使用新的内容文本
            originalData.setTestConditionText(sheetRelHeadContextData.getTestConditionText());
        }
        // 获取仪器记录信息
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(sheetRelHeadContextData.getEquipmentText())) {
            // 记录模板的标记信息
            originalData.setEquipmentText(sheetRelHeadContextData.getEquipmentText());
            StringBuffer equipmentBuffer = new StringBuffer();
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(map.get("equipment"))) {
                equipmentBuffer.append(map.get("equipment"));
                equipmentBuffer.append("、");
            }
            equipmentBuffer.append(sheetRelHeadContextData.getEquipmentText());
            String[] array1 = equipmentBuffer.toString().split("、");
            Set<String> strings = new HashSet<>();
            for (int i = 0; i < array1.length; i++) {
                strings.add(array1[i]);
            }
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : strings) {
                stringBuffer.append(str);
                stringBuffer.append("、");
            }
            originalData.setEquipment(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
        } else {
            // 主要仪器设备名称及编号
            originalData.setEquipment(org.apache.commons.lang.StringUtils.isNotEmpty(map.get("equipment")) ? map.get("equipment") : sheetRelHeadContextData.getEquipmentText());
        }
        result.put("result", originalData);
        // 替换原始记录模板数据
        ExcelReplaceUtil.ExcelReplace(sheet, result, false);
        // 进行 更新操作
        sheetRelHeadContextData.setTestConditionText(originalData.getTestCondition());
        sheetRelHeadContextData.setEquipmentText(originalData.getEquipment());
        sheetRelHeadContextData.setTestDateText(originalData.getTestDate());
        sampleEntityMapper.updateTestItemSheetRelHeadContext(sheetRelHeadContextData);
    }

    /**
     * 私有方法 调用 签名图片存放数组中。
     *
     * @param userIds 用户ID
     * @param Imags   本地附件签名数组
     */
    private void methodUrlImags(List<Long> userIds, String[] Imags) throws Exception {
        List<TaskListParamVo> list = taskMapper.getUserSignatureUrls(userIds);
        if (!CollectionUtils.isEmpty(list)) {
            // 两次循环 是希望根据 userIds 进行排序 设置数值
            for (Long userId : userIds) {
                for (int i = 0; i < list.size(); i++) {
                    TaskListParamVo taskListParamVo = list.get(i);
                    if (userId.equals(Long.valueOf(taskListParamVo.getInspector()))) {
                        // 获取的url签名信息 存放至本地附件
                        String[] image = taskListParamVo.getSignatureUrl().split("/");
                        String downloadDir = dir + GenID.getID() + image[image.length - 1];
                        // 远端URL 存放本地
                        InputStream initialStream = FileAndFolderUtil.getInputStream(taskListParamVo.getSignatureUrl());
                        File targetFile = new File(downloadDir);
                        OutputStream outStream = new FileOutputStream(targetFile);
                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while ((bytesRead = initialStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, bytesRead);
                        }
                        Imags[i] = downloadDir;
                        initialStream.close();
                        outStream.close();
                    }
                }
            }
        }
    }

    /**
     * @param excelUrl 本地文件地址
     * @param ids
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateOriginalRecordUrl(String excelUrl, Integer[] ids) throws Exception {
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
        // 获取公网 附件
        PDFHelper3.getLicense();
        FileInputStream fileStream = new FileInputStream(excelUrl);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 把 XSSFWorkbook 转为 InputStream
        InputStream input = AsposeUtil.createExcelStream(wb);
        String[] arrays = excelUrl.split("\\.");
        methodUpdateItemUrl(GenID.getID() + "." + arrays[arrays.length - 1], input, ids, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
        // 删除附件
        FileAndFolderUtil.delete(excelUrl);
        fileStream.close();
        input.close();
        return "更新成功";
    }


    /**
     * 完成复核：中间检测项 及 最终复核
     *
     * @param excelInsertVo
     * @param userId
     * @param taskId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishCheckItemReview(ExcelInsertVo excelInsertVo, Long userId, Long taskId) throws Exception {
        if (excelInsertVo.getInstrumentStatus().equals("否") || excelInsertVo.getOriginalRecordStatus().equals("否") || excelInsertVo.getData().equals("否")) {
            excelInsertVo.setStatus("驳回");
        }
        // 判断 通过、驳回
        // 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
        Integer state = null;
        // 原始记录 复核通过
        if (excelInsertVo.getStatus().equals("是")) {
            // 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
            state = 3;
        } else {
            // 原始记录 复核驳回
            // 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
            state = 4;
        }
        // 获取 类型（中间复核 或 最终复核）
        if (excelInsertVo.getCheckReview().equals("中间复核")) {
            // 中间复核通过 数据 = 1
            excelInsertVo.setCheckReview("1");
            // 检测项状态 = null
            excelInsertVo.setState(null);
        } else {
            // 最终复核 数据 = null
            excelInsertVo.setCheckReview(null);
            // 检测项状态 = 通过
            excelInsertVo.setState(state);
        }
        // 数据赋值
        if (!CollectionUtils.isEmpty(excelInsertVo.getList())) {
            for (Integer itemd : excelInsertVo.getList()) {
                excelInsertVo.setItemId(itemd);
                // 更新审核人
                excelInsertVo.setReviewedBySetUrl(userId.toString());
                sampleEntityMapper.updateItemReview(excelInsertVo);
            }
        }
        // TODO： 9月19 复核通过后： 构造json数据 进行 合同发起
        try {
            // 通过检测项主键集合与任务单id： 查询已编辑并且是带有sheet的去重itemId
            List<Integer> ids = methodDisinctItemList(taskId, excelInsertVo.getList());
            if (CollectionUtil.isNotEmpty(ids)) {
                return jsonCheckItemMehtod(excelInsertVo.getList());
            } else {
                return true;
            }
        } catch (Exception e) {
            System.out.println("批量复核异常抛出：复核通过后： 构造json数据 进行 合同发起" + e);
        }
        return true;

    }

    /**
     * 通过检测项主键集合与任务单id： 查询已编辑并且是带有sheet的去重itemId
     *
     * @param taskId 任务单id
     * @param list   检测项
     * @return
     */
    private List<Integer> methodDisinctItemList(Long taskId, List<Integer> list) {
        // 通过任务单 带出检测项Id
        Map<Integer, List<Integer>> map = selectTaskIds(taskId);
        // 通过检测项 查询 sheet数据
        // 查询检测项对应的 sheet下标
        Integer[] array = new Integer[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(array);
        if (CollectionUtils.isEmpty(sheetItems)) {
            return null;
        }
        Set<Integer> set = new HashSet<>();
        // 进行比较
        for (ExcelInsertVo excelInsertVo : sheetItems) {
            List<Integer> itemIds = map.get(excelInsertVo.getSampleId());
            if (CollectionUtil.isNotEmpty(itemIds)) {
                for (Integer id : itemIds) {
                    if (excelInsertVo.getItemId().equals(id)) {
                        set.add(id);
                    }
                }
            }
        }
        if (CollectionUtil.isNotEmpty(set)) {
            List<Integer> ids = new ArrayList<>();
            for (Integer id : set) {
                ids.add(id);
            }
            return ids;
        }
        return null;
    }

    /**
     * 私有方法 处理 复核人签名信息。
     *
     * @param array  检测项id
     * @param userId 获取复核人 : 进行签名
     * @throws Exception
     */
    private String methodReviewExcel(Integer[] array, Long userId) throws Exception {
        // 复核人集合
        List<Long> testSetLong = new ArrayList<>();
        testSetLong.add(userId);
        // 检测人签名数组图片
        String[] testImags = new String[testSetLong.size()];
        // 调用方法处理 签名图片存放数组中。
        methodUrlImags(testSetLong, testImags);
//        // 日期数组图片
//        String[] Imags = new String[1];
//        String imagePath = dir + GenID.getID()+"."+"jpg";
//        // 生成日期图片
//        ImgUtils.inserNewImage(imagePath);
//        // 更改图片背景颜色
//        ImgUtils.changeImgColor(imagePath);
//        Imags[0] = imagePath;
        // 查询附件 存在则 删除附件
        ExcelInsertVo excelInsertVo2 = testProductItemDao.getExcelUrl(array[0]);
        InputStream fileStream = null;
        // 处理流文件 存放至本地路径
        String saveExcel = "";
        String newFilePath = "";
        if (excelInsertVo2 != null) {
            if (excelInsertVo2.getReportEditUrl() != null) {
                // 优先拿 报告附件
                try {
                    // 获取公网 附件
                    fileStream = FileAndFolderUtil.getInputStream(excelInsertVo2.getReportEditUrl());
                    String[] names = excelInsertVo2.getReportEditUrl().split("\\.");
                    newFilePath = dir + GenID.getID() + "." + names[names.length - 1];
                    saveExcel = dir + GenID.getID() + "." + names[names.length - 1];
                    // 获取 saveExcel 附件
                    XSSFWorkbook wb = new XSSFWorkbook(fileStream);
                    OutputStream f = new FileOutputStream(saveExcel);
                    wb.write(f);
                    f.close();
                    fileStream.close();
                } catch (Exception e) {
                    logger.info("读取报告URL附件异常 " + excelInsertVo2.getReportEditUrl() + e);
                }
            }
            if (excelInsertVo2.getReportEditUrl() == null && excelInsertVo2.getProductExcelUrl() != null) {
                // 其次拿 产品附件。
                try {
                    // 获取公网 附件
                    fileStream = FileAndFolderUtil.getInputStream(excelInsertVo2.getProductExcelUrl());
                    String[] names = excelInsertVo2.getProductExcelUrl().split("\\.");
                    newFilePath = dir + GenID.getID() + "." + names[names.length - 1];
                    saveExcel = dir + GenID.getID() + "." + names[names.length - 1];
                    // 获取 saveExcel 附件
                    XSSFWorkbook wb = new XSSFWorkbook(fileStream);
                    OutputStream f = new FileOutputStream(saveExcel);
                    wb.write(f);
                    f.close();
                    fileStream.close();
                } catch (Exception e) {
                    logger.info("读取产品URL附件异常 " + excelInsertVo2.getProductExcelUrl() + e);
                }
            }
        }
        // 拿到文件后 saveExcel 获取 sheet名 返回
        Map<String, Integer> mapSheet = getSheetMap(saveExcel);
        // 设置图片的list
        List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(array);
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        // 通过检测项id 获取对应的 sheet下标
        if (!CollectionUtils.isEmpty(sheetItems)) {
            for (ExcelInsertVo excelInsertVo : sheetItems) {
                Map<String, Object> map = new HashMap<>();
                // 遍历对应的 sheet页
                for (String key : mapSheet.keySet()) {
                    Integer sheetIndex = mapSheet.get(key);
                    // 替换 sheet名
                    if (excelInsertVo.getSheetIndex().equals(sheetIndex) && keyMap.get(key) == null) {
                        keyMap.put(key, key);
                        ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                        excelInsertVo1.setSheetName(key);
                        excelInsertVo1.setRecordType("复核：");
                        excelInsertVo1.setImags(testImags);
                        // key 使用 sheet名加类型进行拼接
                        map.put(excelInsertVo1.getSheetName() + excelInsertVo1.getRecordType(), excelInsertVo1);
                        ExcelInsertVo data = new ExcelInsertVo();
                        data.setSheetName(key);
                        data.setMap(map);
                        excelInsertVoList.add(data);
                    }
                }
            }
        }
        // 设置文本的list
        List<ExcelInsertVo> contextList = new ArrayList<>();
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        String titleStr = formatter.format(currentTime);
        Map<String, Object> map = new HashMap<>();
        for (String key : keyMap.keySet()) {
            ExcelInsertVo excelInsertVo3 = new ExcelInsertVo();
            excelInsertVo3.setSheetName(key);
            excelInsertVo3.setRecordType("日期：");
            excelInsertVo3.setData(titleStr);
            // key 使用 sheet名加类型进行拼接
            map.put(excelInsertVo3.getSheetName() + excelInsertVo3.getRecordType(), excelInsertVo3);
            ExcelInsertVo excelInsertVo = new ExcelInsertVo();
            excelInsertVo.setSheetName(key);
            excelInsertVo.setMap(map);
            contextList.add(excelInsertVo);
        }
        // 塞入指定位置 文本内容
        ExcelImageUtils.inserContext(saveExcel, contextList, newFilePath);
        // 清除图片
        ExcelImageUtils.seachXY(newFilePath, excelInsertVoList, saveExcel);
        // 插入图片
        ExcelImageUtils.inserImage(saveExcel, excelInsertVoList, newFilePath);
        // 删除附件
        FileAndFolderUtil.delete(saveExcel);
        // 删除图片信息
        for (int i = 0; i < testImags.length - 1; i++) {
            FileAndFolderUtil.delete(testImags[i]);
        }
//        FileAndFolderUtil.delete(imagePath);
        // 返回塞入的复核人签名本地附件
        return newFilePath;
    }

    /**
     * 更新 产品附件及报告附件内容。
     *
     * @param saveFileUrl 上传附件URL
     * @param input       流文件
     * @param array       检测项集合
     * @param entrustId   委托单id
     * @param sampleId    样品id
     */
    @Transactional(rollbackFor = Exception.class)
    public String methodUpdateItemUrl(String saveFileUrl, InputStream input, Integer[] array, Long
            entrustId, Integer sampleId) throws IOException {
        // 查询附件 存在则 删除附件
        ExcelInsertVo excelInsertVo = testProductItemDao.getExcelUrl(array[0]);
        if (excelInsertVo != null) {
            // 报告附件URL
            String excelUrl = "";
            if (excelInsertVo.getReportEditUrl() != null) {
                // 本地附件 上传 到远端仓库
                excelUrl = MinIoUtil.upload("sample-enclosure", saveFileUrl, input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                String[] urls = excelInsertVo.getReportEditUrl().split("/");
                MinIoUtil.deleteFile("sample-enclosure", urls[urls.length - 1]);
                // 更新报告excel附件
                testProductItemDao.updateReportExcelUrl(entrustId, sampleId, excelUrl);
                input.close();
            }
            if (excelInsertVo.getProductExcelUrl() != null) {
                String productUrl = "";
                // 本地附件 上传 到远端仓库
                if (StringUtils.isEmpty(excelUrl) || "".equals(excelUrl)) {
                    // 报告附件没有上传 上传产品附件
                    productUrl = MinIoUtil.upload("file-resources", saveFileUrl, input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    input.close();
                } else {
                    InputStream fileStream = null;
                    // 根据上传完成的附件 使用并上传。
                    try {
                        // 获取公网 附件
                        fileStream = FileAndFolderUtil.getInputStream(excelUrl);
                        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
                        // 把 XSSFWorkbook 转为 InputStream
                        InputStream input000 = AsposeUtil.createExcelStream(wb);
                        // 报告附件没有上传
                        productUrl = MinIoUtil.upload("file-resources", saveFileUrl, input000, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        fileStream.close();
                        input000.close();
                    } catch (Exception e) {
                        logger.info("读取报告附件URL异常 " + excelUrl + e);
                    }
                }
                String[] urls = excelInsertVo.getProductExcelUrl().split("/");
                MinIoUtil.deleteFile("file-resources", urls[urls.length - 1]);
                // 更新 报告Excel附件
                testProductItemDao.updateProductExcelUrl(entrustId, sampleId, productUrl);
                input.close();
                return productUrl;
            }
        }
        return null;
    }

    @Override
    public String saveOriginalRecord(HttpServletRequest request, FileSaver file) throws Exception {
        // 检测人集合 、记录人集合 带出签名信息。 存放指定的位置。
        // 检测人
        String testSet = file.getFormField("testSet");
        // 记录人
        String recordSet = file.getFormField("recordSet");
        // 检测参数
        String list = file.getFormField("items");
        // 获取检测项id 集合
        String[] items = list.split(",");
        Integer[] ids = new Integer[items.length];
        for (int j = 0; j < items.length; j++) {
            ids[j] = Integer.parseInt(items[j]);
        }
        // 生成的 完成后的file文件
        String saveExcel = dir + file.getFileName();
        // 文件路径
        file.saveToFile(saveExcel);
        // 查询用户id及签名信息
        // 检测人集合
        List<Long> testSetLong = new ArrayList<>();
        testSetLong.add(Long.valueOf(testSet));
        // 检测人与记录人不相同时
        if (!testSet.equals(recordSet)) {
            testSetLong.add(Long.valueOf(recordSet));
        }
        // 检测人签名数组图片
        String[] testImags = new String[testSetLong.size()];
        // 调用方法处理 签名图片存放数组中。
        PDFHelper3.getLicense();
        methodUrlImags(testSetLong, testImags);
        // 记录人集合
        List<Long> recordSetLong = new ArrayList<>();
        recordSetLong.add(Long.valueOf(recordSet));
        // 记录人签名数组图片
        String[] recordImags = new String[recordSetLong.size()];
        // 如果 检测人与记录人相同 签名数组复用即可。
        if (testSet.equals(recordSet)) {
            recordImags[0] = testImags[0];
        } else {
            // 调用方法处理 签名图片存放数组中。
            methodUrlImags(recordSetLong, recordImags);
        }
        List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
        // 处理模糊比较 sheetName
        Workbook workbook = new Workbook(saveExcel);
        int count = workbook.getWorksheets().getCount();
        Map<String, Integer> mapSheet = new HashMap<>();
        for (int o = 0; o < count; o++) {
            // 设置全部可读
            workbook.getWorksheets().get(o).setVisible(true);
            String sheetName = workbook.getWorksheets().get(o).getName();
            mapSheet.put(sheetName, o);
        }
        workbook.save(saveExcel, SaveFormat.XLSX);
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
        // 通过检测项id 获取对应的 sheet下标
        if (!CollectionUtils.isEmpty(sheetItems)) {
            for (ExcelInsertVo excelInsertVo : sheetItems) {
                Map<String, Object> map = new HashMap<>();
                // 遍历对应的 sheet页
                for (String key : mapSheet.keySet()) {
                    Integer sheetIndex = mapSheet.get(key);
                    // 替换 sheet名
                    if (excelInsertVo.getSheetIndex().equals(sheetIndex) && keyMap.get(key) == null) {
                        keyMap.put(key, key);
                        ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                        excelInsertVo1.setSheetName(key);
                        excelInsertVo1.setRecordType("检测：");
                        excelInsertVo1.setImags(testImags);
                        // key 使用 sheet名加类型进行拼接
                        map.put(excelInsertVo1.getSheetName() + excelInsertVo1.getRecordType(), excelInsertVo1);
                        ExcelInsertVo excelInsertVo2 = new ExcelInsertVo();
                        excelInsertVo2.setSheetName(key);
                        excelInsertVo2.setRecordType("记录：");
                        excelInsertVo2.setImags(recordImags);
                        // key 使用 sheet名加类型进行拼接
                        map.put(excelInsertVo2.getSheetName() + excelInsertVo2.getRecordType(), excelInsertVo2);
                        ExcelInsertVo data = new ExcelInsertVo();
                        data.setSheetName(key);
                        data.setMap(map);
                        excelInsertVoList.add(data);
                    }
                }
            }
        }
        // excel 插入图片
        String[] names = file.getFileName().split("\\.");
        String newFilePath = dir + GenID.getID() + "." + names[1];
        // 清除图片
        ExcelImageUtils.seachXY(saveExcel, excelInsertVoList, newFilePath);
        // 插入图片
        ExcelImageUtils.inserImage(newFilePath, excelInsertVoList, saveExcel);
        // 删除附件
        FileAndFolderUtil.delete(newFilePath);
        // 删除图片信息
        for (int i = 0; i < testImags.length - 1; i++) {
            FileAndFolderUtil.delete(testImags[i]);
        }
        // 检测人与记录不相同时
        if (testImags.length > recordImags.length) {
            for (int i = 0; i < recordImags.length - 1; i++) {
                FileAndFolderUtil.delete(recordImags[i]);
            }
        }
        return saveExcel;
    }

    @Override
    public String CompleteTheReview(ExcelInsertVo excelInsertVo) {
        SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(excelInsertVo.getList().get(0));
        Long taskId = sampleItemInstrumentEntity2.getTaskId();
        List<Integer> states = taskMapper.selectCheckItemState(taskId, sampleItemInstrumentEntity2.getDeptId());
        for (Integer stateItem : states) {
            if (stateItem != 3) {
                return "当前任务单下检测项未全部复核成功";
            }
        }
        // 修改test_task state 状态 为6：
        TaskTestEntity taskTestEntity = new TaskTestEntity();
        taskTestEntity.setId(taskId);
        taskTestEntity.setState(6);
        // 任务单 复核成功 记录复核时间。
        taskTestEntity.setReviewTime(new Date(System.currentTimeMillis()));
        //记录日志
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(" 任务单id" + taskId);
        stringBuilder2.append("  任务单复核时间 :" + new Timestamp(taskTestEntity.getReviewTime().getTime()));
        stringBuilder2.append(" 任务单状态: " + taskTestEntity.getState());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-任务单复核成功\n\t" + stringBuilder2.toString(), Const.TASK_TEST, true);
        taskMapper.updateTestTask(taskTestEntity);
        return "任务单复核成功";
    }

    /**
     * 作废
     *
     * @param wb
     * @param taskId
     * @return
     */
    public Map<Integer, Integer> inserItemPageDiscard(XSSFWorkbook wb, Long taskId) {
        // 获取检测项的数据。
        List<TaskIdEntity> list = taskMapper.selectItemPages(taskId);
        Integer[] array = new Integer[list.size()];
        // list 获取
        for (int i = 0; i < list.size(); i++) {
            TaskIdEntity data = list.get(i);
            array[i] = data.getIdItem();
        }
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(array);
        // 当前任务单下检测项没有对应sheet下标 返回空
        if (CollectionUtils.isEmpty(sheetItems)) {
            return null;
        }
        // key = sheet标号 、value =序号
        Map<Integer, Integer> countMap = new HashMap<>();
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        // 序号
        Integer number = 1;
        // 循环遍历所有工作表
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            // 获取第i个工作表
            XSSFSheet sheet = wb.getSheetAt(i);
            if (sheet != null) {
                // 获取工作表的名称
                String sheetName = sheet.getSheetName();
                for (int j = 0; j < sheetItems.size(); j++) {
                    ExcelInsertVo data = sheetItems.get(j);
                    if (data.getSheetIndex().equals(i) && keyMap.get(sheetName) == null) {
                        keyMap.put(sheetName, sheetName);
                        if (countMap.get(i) == null) {
                            countMap.put(i, number);
                            number++;
                        }
                    }
                }
            }
        }
        return countMap;
    }

    /**
     * 通过任务单id 获取对应检测项中 对应的excel页码。
     *
     * @param taskId
     * @return
     */
    public Map<Integer, Object> inserItemPage(Long taskId) {
        // key= checkItemId， value对应的下标数据
        HashMap<Integer, Object> map = new HashMap<>();
        // 获取检测项的数据。
        List<TaskIdEntity> list = taskMapper.selectItemPages(taskId);
        Integer[] array = new Integer[list.size()];
        // list 获取
        for (int i = 0; i < list.size(); i++) {
            TaskIdEntity data = list.get(i);
            array[i] = data.getIdItem();
        }
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(array);
        // 当前任务单下检测项没有对应sheet下标 返回空
        if (CollectionUtils.isEmpty(sheetItems)) {
            return null;
        }
        // 设置去重操作
        Map<String, Integer> mapSet = new HashMap<>();
        // 序号
        Integer number = 1;
        for (int j = 0; j < sheetItems.size(); j++) {
            number = j + 1;
            ExcelInsertVo data = sheetItems.get(j);
            for (int y = 0; y < sheetItems.size(); y++) {
                ExcelInsertVo dataY = sheetItems.get(y);
                // 遍历数据：检测项相同：获取检测项对应的所有excel下标及序号。
                if (data.getCheckItemId().equals(dataY.getCheckItemId()) && mapSet.get(data.getCheckItemId() + "set" + data.getSheetIndex()) == null) {
                    // 设置每组检测项中信息
                    ExcelInsertVo insertVo = new ExcelInsertVo();
                    // 检测项主键
                    insertVo.setCheckItemId(data.getCheckItemId());
                    // 序号
                    insertVo.setNumber(number);
                    // sheet下标
                    insertVo.setSheetIndex(data.getSheetIndex());
                    // 去重： checkItemId+sheetIndex 进行去重
                    mapSet.put(insertVo.getCheckItemId() + "set" + insertVo.getSheetIndex(), 1);
                    if (map.get(data.getCheckItemId()) == null) {
                        List<ExcelInsertVo> insertVos = new ArrayList<>();
                        // 起始页
                        insertVo.setStartPag(1);
                        insertVos.add(insertVo);
                        map.put(data.getCheckItemId(), insertVos);
                    } else {
                        List<ExcelInsertVo> insertVosValues = (List<ExcelInsertVo>) map.get(data.getCheckItemId());
                        // 起始页
                        insertVo.setStartPag(insertVosValues.size() + 1);
                        insertVosValues.add(insertVo);
                        map.put(data.getCheckItemId(), insertVosValues);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 通过任务单id 获取对应检测项中 对应的excel页码。
     *
     * @param taskId
     * @return
     */
    public Map<Integer, Object> inserItemPageNew(Long taskId) {
        // key= checkItemId， value对应的下标数据
        HashMap<Integer, Object> map = new HashMap<>();
        // 通过检测项 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetByTestIdIndex(taskId);
        // 当前任务单下检测项没有对应sheet下标 返回空
        if (CollectionUtils.isEmpty(sheetItems)) {
            return null;
        }
        // 通过任务单id 获取对应检测项对应页码（是否有机器码）
        String countofpage = testProductItemDao.selectItemSheetRelExtend(taskId);

        // 进行排序
        TreeSet<Integer> integerList = new TreeSet<>();
        for (ExcelInsertVo data : sheetItems) {
            // 进行排序操作
            if (data.getSheetIndex() != null) {
                integerList.add(data.getSheetIndex());
            }
        }

        // 最大页码
        Integer maxPage = 0;
        try {
            maxPage = (org.apache.commons.lang.StringUtils.isNotEmpty(countofpage) ? Integer.parseInt(countofpage) : 0) + integerList.size();
        } catch (Exception e) {
            log.error("任务单id {} + 对应检测项页码为空", taskId);
        }

        // for 循环 进行存储
        Iterator<Integer> iterator = integerList.iterator();
        int xuhao = 0;
        while (iterator.hasNext()) {

            int sheetIndex = iterator.next();

            // 设置每组检测项中信息
            ExcelInsertVo insertVo = new ExcelInsertVo();
            // 序号 自增
            xuhao += 1;
            // 序号
            insertVo.setNumber(xuhao);
            // sheet下标
            insertVo.setSheetIndex(sheetIndex);
            // 起始页
            insertVo.setStartPag(xuhao);
            // 最大页码
            insertVo.setMaxPage(maxPage);
            map.put(sheetIndex, insertVo);
        }
        return map;
    }

    public ExcelSheetDataVo getSaveFile(InputStream fileStream, Long taskId) throws Exception {
        ExcelSheetDataVo excelSheetDataVo = new ExcelSheetDataVo();
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // key= checkItemId， value对应的下标数据
        Map<Integer, Object> map = inserItemPageNew(taskId);
        // key = sheet标号 、value = 序号
        Map<Integer, Integer> countMap = new HashMap<>();
        // key = sheet标号 、value = ExcelInsertVo 中 topRow、leftColumn
        Map<Integer, ExcelInsertVo> indexDataXYMap = new HashMap<>();
        fileStream.close();
        // 把 XSSFWorkbook 转为 InputStream
        InputStream input = AsposeUtil.createExcelStream(wb);
        Workbook document = new Workbook(input);
        handlerPageNew(document, countMap, map);
        for (Integer index : countMap.keySet()) {
            // 1、进行 根据标号 添加 日期内容 2、sheet 指定位置 填充文字 3、设置图表插入的位置
            ExcelInsertVo data = new ExcelInsertVo();
            data.setRecordType("日期：");
            data.setSheetIndex(index);
            ExcelReplaceUtil.getSheetRowAndIndexColumn(data, wb);
            indexDataXYMap.put(index, data);
        }
        String path = dir + GenID.getID() + ".xlsx";
        document.save(path);
        input.close();
        excelSheetDataVo.setCountMap(countMap);
        excelSheetDataVo.setSaveFile(path);
        // 每个sheet页中 日期的坐标系数。
        excelSheetDataVo.setIndexDataXYMap(indexDataXYMap);
        // 创建一个文件输入流
        return excelSheetDataVo;
    }

    /**
     * 通过本地附件 获取 所有的 sheetName
     *
     * @param saveFile
     * @return
     * @throws IOException
     */
    public Map<String, Integer> getSheetMap(String saveFile) throws IOException {
        Map<String, Integer> countMap = new HashMap<>();
        InputStream fileStream = new FileInputStream(saveFile);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 循环遍历所有工作表
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            // 获取第i个工作表
            XSSFSheet sheet = wb.getSheetAt(i);
            if (sheet != null) {
                // 获取工作表的名称
                String sheetName = sheet.getSheetName();
                countMap.put(sheetName, i);
            }
        }
        fileStream.close();
        return countMap;
    }

    @Override
    public String updateExcelVisible(String saveFileUrl, Integer[] array, InputStream inputStream) throws
            IOException {
        // 私有方法 更新 产品附件及报告附件内容。
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(array);
        methodUpdateItemUrl(saveFileUrl, inputStream, array, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
        return null;
    }

    @Override
    public TeamVo getTaskInspectorAndRecorder(List<LabelValueVo> teamVo, Long taskId) {
        TeamVo teamVo1 = new TeamVo();
        // 查询任务单中检测人、记录人。
        PersonInfoVo personInfo = taskMapper.getPersonInfo(taskId);
        // 获取任务单中检测人
        String[] inspectorArrays = personInfo.getInspector().split("\\,");
        LinkedList<LabelValueVo> linkedList = new LinkedList<>();
        linkedList.addAll(teamVo);
        for (int i = 0; i < linkedList.size(); i++) {
            LabelValueVo data = linkedList.get(i);
            for (int j = 0; j <= inspectorArrays.length - 1; j++) {
                String[] str1 = inspectorArrays[j].split("\\&");
                Long value = Long.parseLong(str1[1]);
                if (data.getValue().equals(value)) {
                    linkedList.remove(i);
                    linkedList.addFirst(data);
                }
            }
        }
        teamVo1.setInspectorVo(linkedList);
        // 获取任务单中记录人
        String[] recorderArrays = personInfo.getRecorder().split("\\,");
        LinkedList<LabelValueVo> recorderList = new LinkedList<>();
        recorderList.addAll(teamVo);
        for (int i = 0; i < recorderList.size(); i++) {
            LabelValueVo data = recorderList.get(i);
            for (int j = 0; j <= recorderArrays.length - 1; j++) {
                String[] str1 = recorderArrays[j].split("\\&");
                Long value = Long.parseLong(str1[1]);
                if (data.getValue().equals(value)) {
                    recorderList.remove(i);
                    recorderList.addFirst(data);
                }
            }
        }
        teamVo1.setRecorderVo(recorderList);
        return teamVo1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean editItemdData(Integer[] ids, String testSet, String recordSet) {
        testProductItemDao.updateBatchItemData(ids, testSet, recordSet);
        return true;
    }

    @Override
    public Map<String, String> getName(FileSaver file) {
        // 检测人集合 、记录人集合 带出签名信息。
        // 检测人
        String testSet = file.getFormField("testSet");
        // 记录人
        String recordSet = file.getFormField("recordSet");
        // 检测人集合
        List<Long> testSetLong = new ArrayList<>();
        if (!StringUtils.isEmpty(testSet)) {
            testSetLong.add(Long.valueOf(testSet));
        }
        // 检测人与记录人不相同时
        if (!StringUtils.isEmpty(recordSet) && !StringUtils.isEmpty(testSet)) {
            if (!testSet.equals(recordSet)) {
                testSetLong.add(Long.valueOf(recordSet));
            }
        }
        Map<String, String> map = new HashMap<>();
        StringBuffer stringBuffer = new StringBuffer();
        if (CollectionUtil.isNotEmpty(testSetLong)) {
            for (Long testId : testSetLong) {
                stringBuffer.append(testId);
                stringBuffer.append(",");
            }
        }
        if (stringBuffer.length() > 1) {
            map.put("testSet", stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
        }
        if (!StringUtils.isEmpty(recordSet)) {
            map.put("recordSet", recordSet);
        }
        return map;
    }

    @Override
    public String saveOriginalRecord2(Integer[] ids) throws Exception {
        ExcelInsertVo pathUrl = testProductItemDao.getExcelUrl(ids[0]);
        if (pathUrl == null) {
            return null;
        }
        // 签名 图片
        List<String> signatureImages = new ArrayList<>();
        Integer itemId = ids[0];
        // 通过检测项主键 获取样品生成附件是否存在。
        ExcelSheetDataVo productInputStream = getProductInputStream(pathUrl, itemId);
        if (productInputStream == null) {
            return null;
        }
        File file = FileAndFolderUtil.getFile(productInputStream.getProductExcelUrl());
        if (file == null) {
            return null;
        }
        String saveExcel = file.getPath();
        List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
        // 处理模糊比较 sheetName
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(saveExcel);
        int count = workbook.getWorksheets().getCount();
        Map<String, Integer> mapSheet = new HashMap<>();
        for (int o = 0; o < count; o++) {
            // 设置全部可读
            workbook.getWorksheets().get(o).setVisible(true);
            String sheetName = workbook.getWorksheets().get(o).getName();
            mapSheet.put(sheetName, o);
        }
        workbook.save(saveExcel, SaveFormat.XLSX);
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
        if (CollectionUtils.isEmpty(sheetItems)) {
            FileAndFolderUtil.delete(saveExcel);
            return null;
        }
        // 通过检测项id 获取对应的 sheet下标
        if (!CollectionUtils.isEmpty(sheetItems)) {
            for (ExcelInsertVo excelInsertVo : sheetItems) {
                Map<String, Object> map = new HashMap<>();
                // 遍历对应的 sheet页
                for (String key : mapSheet.keySet()) {
                    Integer sheetIndex = mapSheet.get(key);
                    // 替换 sheet名
                    if (excelInsertVo.getSheetIndex().equals(sheetIndex) && keyMap.get(key) == null) {
                        keyMap.put(key, key);
                        ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                        excelInsertVo1.setSheetName(key);
                        excelInsertVo1.setRecordType("检测：");
                        // 获取检测项中 检测人主键
                        if (excelInsertVo.getTestSetUrl() != null) {
                            List<Long> userIds = new ArrayList<>();
                            String[] testArray = excelInsertVo.getTestSetUrl().split("\\,");
                            for (int i = 0; i < testArray.length; i++) {
                                userIds.add(Long.valueOf(testArray[i]));
                            }
                            String[] imags = new String[userIds.size()];
                            methodUrlImags(userIds, imags);
                            excelInsertVo1.setImags(imags);
                            // 记录签名信息
                            for (int j = 0; j < imags.length; j++) {
                                signatureImages.add(imags[j]);
                            }
                        }
                        // key 使用 sheet名加类型进行拼接
                        map.put(excelInsertVo1.getSheetName() + excelInsertVo1.getRecordType(), excelInsertVo1);
                        ExcelInsertVo excelInsertVo2 = new ExcelInsertVo();
                        excelInsertVo2.setSheetName(key);
                        excelInsertVo2.setRecordType("记录：");
                        // 获取检测项中 记录人主键
                        if (excelInsertVo.getRecordSetUrl() != null) {
                            List<Long> userIds = new ArrayList<>();
                            String[] testArray = excelInsertVo.getRecordSetUrl().split("\\,");
                            for (int i = 0; i < testArray.length; i++) {
                                userIds.add(Long.valueOf(testArray[i]));
                            }
                            String[] recordImags = new String[userIds.size()];
                            methodUrlImags(userIds, recordImags);
                            excelInsertVo2.setImags(recordImags);
                            // 记录签名信息
                            for (int j = 0; j < recordImags.length; j++) {
                                signatureImages.add(recordImags[j]);
                            }
                        }
                        // key 使用 sheet名加类型进行拼接
                        map.put(excelInsertVo2.getSheetName() + excelInsertVo2.getRecordType(), excelInsertVo2);
                        ExcelInsertVo data = new ExcelInsertVo();
                        data.setSheetName(key);
                        data.setMap(map);
                        excelInsertVoList.add(data);
                    }
                }
            }
        }
        String[] names = file.getName().split("\\.");
        String newFilePath = dir + GenID.getID() + "." + names[1];
        // 清除图片
        ExcelImageUtils.seachXY(saveExcel, excelInsertVoList, newFilePath);
        // 插入图片
        ExcelImageUtils.inserImage(newFilePath, excelInsertVoList, saveExcel);
        // 删除附件
        FileAndFolderUtil.delete(newFilePath);
        // 消除图片信息
        for (String iamgPath : signatureImages) {
            FileAndFolderUtil.delete(iamgPath);
        }
        return saveExcel;
    }

    @Override
    public QiYueSuoResponse createbycategoryBatch(QiYueSuoReqBean reqBean, List<String> stringList) {
        Map<String, Long> map = new HashMap<>();
        Set<Long> setList = new HashSet<>();
        for (int i = 0; i < reqBean.getList().size(); i++) {
            String checkItemCode = stringList.get(i);
            //step1 根据文件类型创建合同文档
            Long itemId = reqBean.getList().get(i);
            setList.add(itemId);
            String url = testProductItemDao.selectItemOriginUrlPdf(itemId);
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(url)) {
                File file = null;
                try {
                    String uri = "";
                    if (url.contains("?")) {
                        uri = url.substring(0, url.indexOf("?"));
                    } else {
                        uri = url;
                    }
                    file = FileAndFolderUtil.getFile(uri);
                } catch (Exception e) {
                    logger.error("将报告地址转为File文件失败:{}", e);
                    continue;
                }
                if (file != null) {
                    QiYueSuoResponse response = qiYueSuoHnadler.creatFile(file, checkItemCode, "pdf", null, null, null);
                    if (response != null && response.getCode() == 0) {
                        //根据报告编号存储文档id
                        List<QiYueSuoDocment> result = response.getResult();
                        map.put(checkItemCode, result.get(0).getDocumentId());
                        //更新文档id和印章类型
                        testProductItemDao.updateQysInfo(itemId, result.get(0).getDocumentId());
                    } else {
                        return response;
                    }
                }
            }
        }
        //Step2 创建合同
        long id = GenID.getID();
        Long contractId = null;
        if (map != null && map.size() > 0) {
            Set<String> set = map.keySet();
            List<String> docs = new ArrayList<>();
            for (String reportCode : set) {
                docs.add(map.get(reportCode) + "");
            }
            reqBean.setDocuments(docs);
            reqBean.setEntrustId(id);
            // 设置 具体签名信息：
            QiYueSuoResponse response = qiYueSuoHnadler.createbyTestcategory(reqBean);
            if (response != null && response.getCode() == 0) {
                //更新文档id和印章类型
                if (response.getContractId() != null) {
                    contractId = response.getContractId();
                    testProductItemDao.updateContractIdByCodes(setList, contractId);
                    System.out.println("set == " + set + "  contractId== " + contractId);
                }
            } else {
                return response;
            }
        }
        //step3 获取签署链接
        String info = recordEntityMapper.getInitInfo();
        QiYueSuoSeaLBean qiYueSuoSeaLBean = new QiYueSuoSeaLBean();
        qiYueSuoSeaLBean.setContractId(contractId);
        qiYueSuoSeaLBean.setEntrustId(id);
        qiYueSuoSeaLBean.setContact("");
        qiYueSuoSeaLBean.setExpireTime(259200);
        qiYueSuoSeaLBean.setReceiverName(ShiroUtils.getUserInfo().getName());
        qiYueSuoSeaLBean.setTenantName(info);
        qiYueSuoSeaLBean.setTenantType("COMPANY");
        QiYueSuoResponse response = qiYueSuoHnadler.signurl(qiYueSuoSeaLBean);
        Long userId = ShiroUtils.getUserInfo().getUserId();
        String sysUserName = sysUserDao.getSysUserName(userId);
//        //更新签署链接、状态
        if (response != null && response.getCode() == 0) {
            testProductItemDao.updateUrlAndStateByContractId(contractId, response.getSignUrl(), "2", sysUserName + "&" + userId + "", new Date(System.currentTimeMillis()));
        }
        response.setContractId(contractId);
        return response;
    }

    @Override
    public String updateItemOriginUrlPdf(SampleItemInstrumentVo sampleItemInstrumentVo) throws Exception {
        // 试验完成：检测项对应原始记录签名信息更新
        if (CollectionUtil.isNotEmpty(sampleItemInstrumentVo.getItemInstrumentEntityList())) {
            Integer[] ids = new Integer[sampleItemInstrumentVo.getItemInstrumentEntityList().size()];
            for (int i = 0; i < sampleItemInstrumentVo.getItemInstrumentEntityList().size(); i++) {
                SampleItemInstrumentEntity data = sampleItemInstrumentVo.getItemInstrumentEntityList().get(i);
                ids[i] = data.getItemId();
                // 查询检测项详情：获取是否编辑(有无对应检测项)、附件pdf是否已经上传。
                ExcelInsertVo excelInsertVo = testProductItemDao.selectCheckDetails(data.getItemId());
                // sheet 已经编辑
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(excelInsertVo.getEditData())) {
                    // 附件pdf 未上传
                    if (StringUtils.isEmpty(excelInsertVo.getOriginUrlPdf())) {
                        List<Integer> list = new ArrayList<>();
                        list.add(data.getItemId());
                        // 调用方法 实现 xlsx 转pdf 并上传桶文件 返回minIo链接
                        String urlPdf = excelmethod(list, excelInsertVo.getCheckItemCode());
                        // 更新xlsx 转pdf 进行上传。
                        ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                        excelInsertVo1.setItemId(data.getItemId());
                        excelInsertVo1.setOriginUrlPdf(urlPdf);
                        testProductItemDao.updateItemData(excelInsertVo1);
                    }
                }
            }
        }
        return null;
    }

    public String excelmethod(List<Integer> list, String checkItemCode) throws Exception {
        String newFilePath = qiYueSuoEntity.getAutographPath() + GenID.getID() + ".xlsx";
        String path = qiYueSuoEntity.getAutographPath() + GenID.getID() + ".pdf";
        ExcelInsertVo excelInsertVo = new ExcelInsertVo();
        excelInsertVo.setList(list);
        Integer[] ids = new Integer[excelInsertVo.getList().size()];
        for (int i = 0; i < excelInsertVo.getList().size(); i++) {
            ids[i] = excelInsertVo.getList().get(i);
        }
        // excel 转 pdf
        XSSFWorkbook wb = getOriginalRecordAttachment(excelInsertVo);
        FileOutputStream out = new FileOutputStream(newFilePath);
        wb.write(out);
        out.flush();//刷新
        InputStream out000 = new FileInputStream(newFilePath);
        //相应pdf
        ByteArrayOutputStream b1 = PDFHelper3.excel2pdf(out000, path);
        InputStream inputStream = FileAndFolderUtil.parseOut(b1);
        // 本地附件 上传 到远端仓库
        String excelUrl = MinIoUtil.upload("sample-enclosure", checkItemCode + ".pdf", inputStream, "application/pdf");
        inputStream.close();
        out000.close();
        b1.close();
        out.close();//关闭
        // 删除附件
        FileAndFolderUtil.delete(newFilePath);
        FileAndFolderUtil.delete(path);
        // 文件上传
        return excelUrl;
    }

    /**
     * 返回原始记录
     * List 检测项主键
     * CheckReview 类型（中间复核 或 最终复核）
     *
     * @return
     */
    public XSSFWorkbook getOriginalRecordAttachment(ExcelInsertVo excelInsertVo) throws IOException {
        Integer[] ids = new Integer[excelInsertVo.getList().size()];
        for (int i = 0; i < excelInsertVo.getList().size(); i++) {
            ids[i] = excelInsertVo.getList().get(i);
        }
        // 通过检测项主键 获取样品生成附件是否存在。
        String productExcelUrl = testProductItemDao.getProductExcelUrl(excelInsertVo.getList().get(0));
        InputStream fileStream = null;
        // 获取公网 附件
        try {
            fileStream = FileAndFolderUtil.getInputStream(productExcelUrl);
        } catch (Exception e) {
            logger.info("样品附件 " + productExcelUrl + e);
        }
        if (fileStream == null) {
            return null;
        }
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        Map<String, String> mapSheet = new HashMap<>();
        // 循环遍历所有工作表
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            // 获取第i个工作表
            XSSFSheet sheet = wb.getSheetAt(i);
            if (sheet != null) {
                // 获取工作表的名称
                String sheetName = sheet.getSheetName();
                mapSheet.put(sheetName, sheetName);
            }
        }
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
        // 获取 sheetName
        Map<String, Object> map = new HashMap<>();
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(sheetItems)) {
            for (ExcelInsertVo excelInsertVo1 : sheetItems) {
                // 获取sheetIndex工作表
                XSSFSheet sheet = wb.getSheetAt(excelInsertVo1.getSheetIndex());
                if (sheet != null) {
                    //获取工作表的名称
                    String sheetName = sheet.getSheetName();
                    if (keyMap.get(sheetName) == null) {
                        keyMap.put(sheetName, sheetName);
                    }
                }
            }
        }
        for (String key : keyMap.keySet()) {
            XSSFSheet sheet = wb.getSheet(key);
            if (sheet != null) {
                // 设置全部可读
                wb.getSheet(key).setVerticallyCenter(true);
                map.put(key, 0);
            }
        }
        // sheetName 不包含 则清除
        ExcelReplaceUtil.removeSheetName(map, wb);
        fileStream.close();
        return wb;
    }


    @Override
    public Result startInitiateContractLock(QiYueSuoReqBean reqBean, List<ExcelInsertVo> list) {
        List<String> stringList = new ArrayList<>();
        // 效验每个检测项的信息
        for (ExcelInsertVo excelInsertVo : list) {
            stringList.add(excelInsertVo.getCheckItemCode());
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringList.size(); i++) {
            stringBuilder.append(stringList.get(i));
            if (i == stringList.size() - 1) {
                continue;
            } else {
                stringBuilder.append(",");
            }
        }
        reqBean.setSubject(stringBuilder.toString());
        QiYueSuoResponse response = createbycategoryBatch(reqBean, stringList);
        if (response != null && response.getCode() == 0) {
            return ResultUtil.success("向契约锁发起报告制作申请成功!");
        } else {
            return ResultUtil.error("向契约锁发起报告制作申请失败：" + response.getMessage());
        }
    }

    /**
     * TODO： 9月19 复核通过后：
     * 通过检测项id 获取效验通过：
     * 可以发起合同的检测人、记录人、复核人 有序排列
     *
     * @param itemList
     * @return
     */
    public HashMap<String, Object> inspectionAndTesting(List<Integer> itemList) {
        HashMap<String, Object> map = new HashMap<>();
        Long[] array = new Long[itemList.size()];
        for (int i = 0; i < itemList.size(); i++) {
            array[i] = Long.valueOf(itemList.get(i));
        }
        List<ExcelInsertVo> list = testProductItemDao.selectCheckList(array);
        List<String> stringList = new ArrayList<>();
        List<Long> arrays = new ArrayList<>();
        // 任务单下 对应的检测人、记录人、复核人
        List<Long> userIds = new ArrayList<>();
        // 获取去重后的 数据
        List<ExcelInsertVo> items = new ArrayList<>();
        // 效验每个检测项的信息
        for (ExcelInsertVo excelInsertVo : list) {
            Boolean status = true;
            if (excelInsertVo.getState() != 3) {
                // "创建合同失败：当前检测项 " + excelInsertVo.getCheckItemName() + " 未通过复核 "
                status = false;
            }
            if (StringUtils.isEmpty(excelInsertVo.getEditData())) {
                // "创建合同失败：当前检测项 " + excelInsertVo.getCheckItemName() + " 未进行excel在线编辑 "
                status = false;
            }
//            if (org.apache.commons.lang3.StringUtils.isNotEmpty(excelInsertVo.getSignUrl())) {
//                // "创建合同失败：当前检测项 " + excelInsertVo.getCheckItemName() + " 电子印章signUrl已存在 "
//                status = false;
//            }
            // 条件成立
            if (status) {
                // 进行赋值
                arrays.add(excelInsertVo.getItemId().longValue());
                stringList.add(excelInsertVo.getCheckItemCode());
                // 目前只需要记录一组即可
                if (CollectionUtils.isEmpty(userIds)) {
                    // 获取对应的检测项中 检测人、记录人、复核人
                    String[] array1 = excelInsertVo.getTestSetUrl().split("\\,");
                    userIds.add(Long.valueOf(array1[0]));
                    userIds.add(Long.valueOf(excelInsertVo.getRecordSetUrl()));
                    userIds.add(Long.valueOf(excelInsertVo.getReviewedBySetUrl()));
                }
                items.add(excelInsertVo);
            }
        }
        if (CollectionUtils.isEmpty(items)) {
            // items = null 说明复核通过后：复核发起契约锁的数据为空
            return null;
        }
        map.put("arrays", arrays);
        map.put("userIds", userIds);
        map.put("list", items);
        return map;
    }

    /**
     * TODO： 9月19 复核通过后： 构造json数据 进行 合同发起
     *
     * @param itemList
     * @return
     */
    public Boolean jsonCheckItemMehtod(List<Integer> itemList) {
        // 复核通过后： 通过检测项id 获取效验通过可以发起合同的检测人、记录人、复核人 有序排列
        HashMap<String, Object> map = inspectionAndTesting(itemList);
        if (map == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(map.keySet())) {
            return false;
        }
        // 获取 user信息
        LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUserEntity::getUserId, (List<Long>) map.get("userIds"));
        List<SysUserEntity> userList = sysUserDao.selectList(queryWrapper);
        // 处理数据格式
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        QiYueSuoReqBean reqBean = new QiYueSuoReqBean();
        // 检测项主键集合
        reqBean.setList((List<Long>) map.get("arrays"));
        reqBean.setSend(true);
        // 发起人名字
        reqBean.setCreatorName(userInfo.getName());
        // 发起人手机号
        reqBean.setCreatorContact(userInfo.getMobile());
        //step3 获取签署链接
        String info = recordEntityMapper.getInitInfo();
        // 公司信息
        reqBean.setTenantName(info);
        List<Signatories> signatories = new ArrayList<>();
        Signatories data = new Signatories();
        // NO暂定 = 1
        data.setSerialNo(String.valueOf(1));
        data.setTenantName(info);
        data.setTenantType("COMPANY");
        List<Actions> actions = new ArrayList<>();
        List<ExcelInsertVo> list = (List<ExcelInsertVo>) map.get("list");
        ExcelInsertVo insertVo = list.get(0);
        // 检测人不为空
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(insertVo.getTestSetUrl())) {
            String[] array1 = insertVo.getTestSetUrl().split("\\,");
            String testPerson = array1[0];
            methodLoop(testPerson, actions, userList, info, "1");
        }
        // 记录人不为空
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(insertVo.getRecordSetUrl())) {
            methodLoop(insertVo.getRecordSetUrl(), actions, userList, info, "2");
        }
        // 复核人不为空
        if (org.apache.commons.lang3.StringUtils.isNotEmpty((insertVo.getReviewedBySetUrl()))) {
            methodLoop(insertVo.getReviewedBySetUrl(), actions, userList, info, "3");
        }
        data.setActions(actions);
        signatories.add(data);
        reqBean.setSignatories(signatories);
        System.out.println("reqBean === " + reqBean);
        // 数据正常后 ： 进行发起合同印章数据
        Result msg = startInitiateContractLock(reqBean, list);
        return true;
    }

    void methodLoop(String userId, List<Actions> actions, List<SysUserEntity> userList, String info, String
            serialNo) {
        for (int i = 0; i < userList.size(); i++) {
            Actions actions1 = new Actions();
            SysUserEntity userData = userList.get(i);
            if (userId.contains(userData.getUserId().toString())) {
                actions1.setType("PERSONAL");
                actions1.setName(info);
                actions1.setSerialNo(serialNo);
                actions1.setSealIds("[]");
                List<ActionOperators> actionOperators = new ArrayList<>();
                ActionOperators actiondata = new ActionOperators();
                actiondata.setOperatorName(userData.getName());
                actiondata.setOperatorContact(userData.getMobile());
                actionOperators.add(actiondata);
                actions1.setActionOperators(actionOperators);
                // 设置签名页数
                Location location = new Location();
                // 签署页码，坐标指定位置时必须，0:全部页，-1:最后一页，其他:第page页
                location.setPage(0);
                List<Location> locations = new ArrayList<>();
                locations.add(location);
                actions1.setLocations(locations);
                actions.add(actions1);
            }
        }
    }

    /**
     * 进行每组检测项下对应的sheet下标 调整头部信息
     *
     * @param paramVo
     * @return
     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public String updateItemOriginUr(EndTestParamVo paramVo) throws IOException {
//        // 1、获取检测项信息列表对应的 sheet下标。
//        // 查询检测项对应的 sheet下标
//        Integer[] ids = new Integer[paramVo.getItemInstrumentEntityList().size()];
//        for (int j = 0; j < paramVo.getItemInstrumentEntityList().size(); j++) {
//            ids[j] = paramVo.getItemInstrumentEntityList().get(j);
//        }
//        // 会获取任务单下 所有检测项
//        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
//        // 2、 获取每组检测项的 数据（试验检测日期、试验条件、主要仪器设备名称及编号）
//        Map<Integer, Map<String, String>> mapMap = methodHashMapItem(paramVo.getItemInstrumentEntityList(), sheetItems);
//        // 3、读取产品附件
//        String productExcelUrl = null;
//        ExcelInsertVo excelInsertVo = testProductItemDao.getExcelUrl(ids[0]);
//        Integer itemId = ids[0];
//        // 通过检测项主键 获取样品生成附件是否存在。
//        InputStream inputStream = null;
//        // 调用函数 获取 数据内容
//        ExcelSheetDataVo productInputStream = getProductInputStream(excelInsertVo, itemId);
//        if (productInputStream == null) {
//            return null;
//        }
//        if (productInputStream.getProductExcelUrl() == null) {
//            return null;
//        }
//        inputStream = productInputStream.getFileStream();
//        productExcelUrl = productInputStream.getProductExcelUrl();
//        // 创建一个 XSSFWorkbook 对象，用于处理 .xlsx 格式的 Excel 文件
//        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
////        ----------------------------------上列 设置基础数据----------------------------
//        // 根据key 保证 sheet不重复使用。
//        Map<String, String> keyMap = new HashMap<>();
//        // 检测项 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回 && 检测项对应的sheet 不为空
//        for (ExcelInsertVo excelInsertVo1 : sheetItems) {
//            // 获取sheetIndex工作表
//            XSSFSheet sheet = wb.getSheetAt(excelInsertVo1.getSheetIndex());
//            if (sheet != null) {
//                //获取工作表的名称
//                String sheetName = sheet.getSheetName();
//                if (keyMap.get(sheetName) == null) {
//                    keyMap.put(sheetName, sheetName);
//                    // 设置数据
//                    OriginalRecordDataVo originalData = new OriginalRecordDataVo();
//                    Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
//                    // 录入信息
//                    Map<String, String> map = mapMap.get(excelInsertVo1.getItemId());
//                    // 试验检测日期
//                    originalData.setTestDate(map.get("testDate"));
//                    // 试验条件
//                    originalData.setTestCondition(map.get("testCondition"));
//                    // 主要仪器设备名称及编号
//                    originalData.setEquipment(map.get("equipment"));
//                    result.put("result", originalData);
//                    // 替换原始记录模板数据
//                    ExcelReplaceUtil.ExcelHeadReplace(sheet, result);
//                }
//            }
//        }
//        InputStream input = null;
//        input = AsposeUtil.createExcelStream(wb);
//        if (input != null) {
//            // 循环设置
//            List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
//            // 把 wb 数据 存放上传
//            String[] array = productExcelUrl.split("\\.");
//            if (excelInsertVo == null) {
//                String excelUrl = MinIoUtil.upload("file-resources", GenID.getID() + "." + array[array.length - 1], input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//                input.close();
////                fileStream.close();
//                // 更新 样品Excel附件
//                testProductItemDao.updateProductExcelUrl(dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId(), excelUrl);
//                return excelUrl;
//            } else {
////                fileStream.close();
//                // 私有方法 更新 产品附件及报告附件内容。
//                return methodUpdateItemUrl(GenID.getID() + "." + array[array.length - 1], input, ids, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
//            }
//        }
//        return "操作成功";
//    }

    /**
     * 进行每组检测项下对应的sheet下标 调整头部信息
     *
     * @param paramVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateItemOriginUr(EndTestParamVo paramVo) throws IOException {
        // 1、获取检测项信息列表对应的 sheet下标。
        // 查询检测项对应的 sheet下标
        Integer[] ids = new Integer[paramVo.getItemInstrumentEntityList().size()];
        for (int j = 0; j < paramVo.getItemInstrumentEntityList().size(); j++) {
            ids[j] = paramVo.getItemInstrumentEntityList().get(j);
        }
        // 会获取任务单下 所有检测项
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
        if (CollectionUtils.isEmpty(sheetItems)) {
            return null;
        }
        Boolean status = false;
        for (ExcelInsertVo insertVo : sheetItems) {
            if (insertVo.getEditData() != null) {
                status = true;
            }
        }
        if (!status) {
            return null;
        }
        // 3、读取产品附件
        String productExcelUrl = null;
        ExcelInsertVo excelInsertVo = testProductItemDao.getExcelUrl(ids[0]);
        Integer itemId = ids[0];
        // 通过检测项主键 获取样品生成附件是否存在。
        InputStream inputStream = null;
        // 调用函数 获取 数据内容
        ExcelSheetDataVo productInputStream = getProductInputStream(excelInsertVo, itemId);
        if (productInputStream == null) {
            return null;
        }
        if (productInputStream.getProductExcelUrl() == null) {
            return null;
        }
        inputStream = productInputStream.getFileStream();
        productExcelUrl = productInputStream.getProductExcelUrl();
        // 创建一个 XSSFWorkbook 对象，用于处理 .xlsx 格式的 Excel 文件
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
//        ----------------------------------上列 设置基础数据----------------------------
        try {
            // 动态插入 头部信息:1、批量获取 检测项id（有可能对应多个模板） 再进行填充。2、通过检测项id 获取 相应的 id关联信息。
            // 循环设置
            List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
            methodInsertHeadTemplate(dataEntitys, sheetItems, wb, null, null, null, false);
        } catch (Exception e) {
            System.out.println("编辑原始记录异常抛出");
            e.printStackTrace();
        }
        InputStream input = null;
        input = AsposeUtil.createExcelStream(wb);
        if (input != null) {
            // 循环设置
            List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
            // 把 wb 数据 存放上传
            String[] array = productExcelUrl.split("\\.");
            if (excelInsertVo == null) {
                String excelUrl = MinIoUtil.upload("file-resources", GenID.getID() + "." + array[array.length - 1], input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                input.close();
                // 更新 样品Excel附件
                testProductItemDao.updateProductExcelUrl(dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId(), excelUrl);
                return excelUrl;
            } else {
                // 私有方法 更新 产品附件及报告附件内容。
                return methodUpdateItemUrl(GenID.getID() + "." + array[array.length - 1], input, ids, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
            }
        }
        return "操作成功";
    }

    public Map<Integer, Map<String, String>> methodHashMapItem(List<Integer> integerList, List<ExcelInsertVo> sheetItems) {
        Map<Integer, Map<String, String>> mapMap = new HashMap<>();
        // 每个检测项主键对应的 仪器信息
        Map<Integer, List<TestInstrumentEntity>> itemInstrumentList = new HashMap<>();
        for (Integer id : integerList) {
            Map<String, String> itemMap = new HashMap<>();
            // 1、主要仪器设备名称及编号 、试验条件
            List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(id);
            // 试验条件
            StringBuilder wendugBuilder = new StringBuilder();
            // 仪器的开始检测时间
            Date startTime = null;
            // 仪器结束时间
            Date endTime = null;
            if (instrumentEntityList != null && !instrumentEntityList.isEmpty()) {
                // 记录：每个检测项主键对应的 仪器信息
                itemInstrumentList.put(id, instrumentEntityList);
                //试验条件: 温度湿度 获取第一组信息
                wendugBuilder.append("温度：");
                if (StringUtils.isEmpty(instrumentEntityList.get(0).getTemperature())) {
                    wendugBuilder.append("-");
                } else {
                    wendugBuilder.append(instrumentEntityList.get(0).getTemperature());
                }
                wendugBuilder.append(" 湿度：");
                if (StringUtils.isEmpty(instrumentEntityList.get(0).getHumidity())) {
                    wendugBuilder.append("-");
                } else {
                    wendugBuilder.append(instrumentEntityList.get(0).getHumidity() + " ");
                }
                for (int i = 0; i < instrumentEntityList.size(); i++) {
                    // 记录仪器的检测开始时间
                    if (startTime == null) {
                        // 当前检测项仪器开始时间不为空
                        if (instrumentEntityList.get(i).getStartTime() != null) {
                            startTime = instrumentEntityList.get(i).getStartTime();
                        }
                    } else {
                        // 当前检测项仪器开始时间不为空
                        if (instrumentEntityList.get(i).getStartTime() != null) {
                            // 进行比较 检测项仪器的开始时间
                            // boolean before(Date when)    测试此日期是否在指定日期之前。
                            if (instrumentEntityList.get(i).getStartTime().before(startTime)) {
                                startTime = instrumentEntityList.get(i).getStartTime();
                            }
                        }
                    }
                    // 记录仪器的检测结束时间
                    if (endTime == null) {
                        // 当前检测项仪器结束时间不为空
                        if (instrumentEntityList.get(i).getEndTime() != null) {
                            endTime = instrumentEntityList.get(i).getEndTime();
                        }
                    } else {
                        // 当前检测项仪器结束时间不为空
                        if (instrumentEntityList.get(i).getEndTime() != null) {
                            // 进行比较 检测项仪器的结束时间
                            // boolean after(Date when)   测试此日期是否在指定日期之后。
                            if (instrumentEntityList.get(i).getEndTime().after(endTime)) {
                                endTime = instrumentEntityList.get(i).getEndTime();
                            }
                        }
                    }
                }
            }
            // 试验条件
            itemMap.put("testCondition", wendugBuilder.toString());
            // 2、试验检测日期 -- 后期比较
            SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyy年MM月dd日");
            String startTimestr = "";
            String endTimestr = "";
            if (startTime != null && endTime != null) {
                startTimestr = yyyyMMddHH_NOT_.format(startTime).substring(0, 11);
                endTimestr = yyyyMMddHH_NOT_.format(endTime).substring(0, 11);
            } else {
                SampleItemInstrumentEntity itemDetail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(id);
                // 获取试验开始时间 == null  则 设置为检测项的 开始时间与结束时间
                if (itemDetail.getStartTime() != null) {
                    startTimestr = yyyyMMddHH_NOT_.format(itemDetail.getStartTime()).substring(0, 11);
                }
                if (itemDetail.getEndTime() != null) {
                    endTimestr = yyyyMMddHH_NOT_.format(itemDetail.getEndTime()).substring(0, 11);
                }
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(startTimestr) && org.apache.commons.lang3.StringUtils.isNotEmpty(endTimestr)) {
                // 仪器检测时间与结束时间一致的话  合并即可
                if (startTimestr.equals(endTimestr)) {
                    itemMap.put("testDate", endTimestr);
                } else {
                    itemMap.put("testDate", startTimestr + "~" + endTimestr);
                }
            } else {
                itemMap.put("testDate", startTimestr);
            }
            mapMap.put(id, itemMap);
        }
        // 调用方法解决 ： 多个参数使用一个原始记录表格，仪器设备带出不全
        methodItemSheet(itemInstrumentList, sheetItems, mapMap);
        return mapMap;
    }

    @Override
    public Map<Integer, List<Integer>> selectTaskIds(Long taskId) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        List<ExcelInsertVo> list = testProductItemDao.selectTaskIdItems(taskId);
        if (CollectionUtil.isNotEmpty(list)) {
            for (ExcelInsertVo data : list) {
                if (map.get(data.getSampleId()) == null) {
                    List<Integer> items = new ArrayList<>();
                    items.add(data.getItemId());
                    map.put(data.getSampleId(), items);
                } else {
                    List<Integer> items = map.get(data.getSampleId());
                    items.add(data.getItemId());
                    map.put(data.getSampleId(), items);
                }
            }
        }
        return map;
    }

    /**
     * 解决 ： 多个参数使用一个原始记录表格，仪器设备带出不全
     *
     * @param itemInstrumentMap 每组检测项对应的仪器信息集合
     * @param sheetItems        sheet下标集合
     * @param mapMap            返回检测项与map生成数据
     */
    void methodItemSheet(Map<Integer, List<TestInstrumentEntity>> itemInstrumentMap, List<ExcelInsertVo> sheetItems, Map<Integer, Map<String, String>> mapMap) {
        // 统计每个sheet页所对应的 设备编号信息
        Map<Integer, Map<Integer, String>> sheetInstrumentMap = new HashMap<>();
        // 遍历下标数据
        for (ExcelInsertVo excelInsertVo : sheetItems) {
            // 每组检测项主键 对应的仪器集合
            if (CollectionUtil.isNotEmpty(itemInstrumentMap.get(excelInsertVo.getItemId()))) {
                // 循环展示检测项中 仪器使用记录
                List<TestInstrumentEntity> list = itemInstrumentMap.get(excelInsertVo.getItemId());
                if (CollectionUtil.isNotEmpty(list)) {
                    for (TestInstrumentEntity testInstrumentEntity : list) {
                        // 转变为sheet页对应的 仪器信息
                        if (sheetInstrumentMap.get(excelInsertVo.getSheetIndex()) == null) {
                            // 仪器对应的数据
                            Map<Integer, String> instrumentMap = new HashMap<>();
                            // 仪器编号信息
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(testInstrumentEntity.getModel());
                            stringBuilder.append(testInstrumentEntity.getName());
                            stringBuilder.append("（");
                            stringBuilder.append(testInstrumentEntity.getCode());
                            stringBuilder.append("）");
                            instrumentMap.put(testInstrumentEntity.getId(), stringBuilder.toString());
                            sheetInstrumentMap.put(excelInsertVo.getSheetIndex(), instrumentMap);
                        }
                        else{
                            // 仪器对应的数据
                            Map<Integer, String> sheetIndexMap = sheetInstrumentMap.get(excelInsertVo.getSheetIndex());
                            // 仪器编号信息
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(testInstrumentEntity.getModel());
                            stringBuilder.append(testInstrumentEntity.getName());
                            stringBuilder.append("（");
                            stringBuilder.append(testInstrumentEntity.getCode());
                            stringBuilder.append("）");
                            sheetIndexMap.put(testInstrumentEntity.getId(), stringBuilder.toString());
                            sheetInstrumentMap.put(excelInsertVo.getSheetIndex(), sheetIndexMap);
                        }
                    }
                }
            }
        }
        // mapMap：返回检测项与map生成数据
        // 统计每个sheet页所对应的 设备编号信息  Map<Integer, Map<Integer, String>> sheetInstrumentMap = new HashMap<>();
        // 遍历下标数据
        for (ExcelInsertVo excelInsertVo : sheetItems) {
            if (mapMap.get(excelInsertVo.getItemId()) != null) {
                if (sheetInstrumentMap.get(excelInsertVo.getSheetIndex()) != null) {
                    Map<Integer, String> sheetIndexMap = sheetInstrumentMap.get(excelInsertVo.getSheetIndex());
                    // 仪器编号信息
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Integer instrumentId : sheetIndexMap.keySet()) {
                        String instrumentContext = sheetIndexMap.get(instrumentId);
                        stringBuilder.append(instrumentContext);
                        // 多个仪器展示信息
                        stringBuilder.append("、");
                    }
                    Map<String, String> map = mapMap.get(excelInsertVo.getItemId());
                    // 设备编号
                    map.put("equipment", stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
                    mapMap.put(excelInsertVo.getItemId(), map);
                }
            }
        }
    }
}
