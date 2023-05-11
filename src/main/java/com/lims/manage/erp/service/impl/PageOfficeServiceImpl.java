package com.lims.manage.erp.service.impl;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestDetectionDao;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.ExcelInsertVo;
import com.lims.manage.erp.vo.ExcelSheetDataVo;
import com.lims.manage.erp.vo.OriginalRecordDataVo;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.zhuozhengsoft.pageoffice.FileSaver;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
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
import java.util.*;

/**
 * @Author: DLC
 * @Date: 2023/4/19 9:59
 */
@Service
@Slf4j
public class PageOfficeServiceImpl implements PageOfficeService {
    private final static Logger logger = LoggerFactory.getLogger(PageOfficeServiceImpl.class);

    @Value("${autograph.path}")
    private String dir;

    @Autowired
    TaskServiceImpl taskService;
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


    /**
     * 处理合并完整的excel每个报告的页码
     *
     * @param document
     * @param countMap
     */
    private static void handlerPage(com.aspose.cells.Workbook document, Map<Integer, Integer> countMap, int total) {
        //报告总页数
        //填充每个子报告每页的页码
        Set<Integer> keySet = countMap.keySet();
        for (Integer page : keySet) {
            int count = countMap.get(page);
            Worksheet worksheet = document.getWorksheets().get(page);
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
                            if ("第   页，共   页".equals(string)) {
                                cells.get(n, j).setValue("第" + count + "页，共" + total + "页");
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getProductExcelUrl(Integer[] ids) throws Exception {
        // 通过检测项主键 获取样品生成附件是否存在。
        String productExcelUrl = null;
        ExcelInsertVo excelInsertVo = testProductItemDao.getExcelUrl(ids[0]);
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
        InputStream fileStream = null;
        if (StringUtils.isEmpty(productExcelUrl)) {
            // -- 开始： 读取产品附件
            productExcelUrl = testProductItemDao.getProductExcel(ids[0]);
            if (StringUtils.isEmpty(productExcelUrl)) {
                logger.info("读取产品excel为null  产品主键中产品无产品附件");
                return null;
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
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
        ExcelSheetDataVo excelSheetDataVo = getSaveFile(fileStream, dataEntitys.get(0).getTaskId());
        FileInputStream inputStream = new FileInputStream(new File(excelSheetDataVo.getSaveFile()));
        // 创建一个 XSSFWorkbook 对象，用于处理 .xlsx 格式的 Excel 文件
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        FileAndFolderUtil.delete(excelSheetDataVo.getSaveFile());
        // 批量获取 检测项id（有可能对应多个模板） 再进行填充。
        // 通过检测项id 获取 相应的 id关联信息。
        for (int i = 0; i < dataEntitys.size(); i++) {
            TaskIdEntity data = dataEntitys.get(i);
            // 检测项 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
            if (data != null && !data.getState().equals(3)) {
                // 模糊匹配
                // 循环遍历所有工作表
                for (int j = 0; j < wb.getNumberOfSheets(); j++) {
                    // 获取第i个工作表
                    XSSFSheet sheet = wb.getSheetAt(j);
                    if (sheet != null) {
                        // 获取工作表的名称
                        String sheetName = sheet.getSheetName();
                        if (data.getCheckItemName().contains(sheetName)) {
                            Map<Integer, Integer> countMap = excelSheetDataVo.getCountMap();
                            int number = countMap.get(j);
                            // 有序信息。
                            OriginalRecordDataVo originalData = taskService.getOriginalData(data.getTaskId(), data.getSampleId(), data.getCheckItemId(), data.getIdItem());
                            Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
                            originalData.setRecordNumber(originalData.getRecordNumber() + "-" + number);
                            result.put("result", originalData);
                            // 替换原始记录模板数据
                            ExcelReplaceUtil.ExcelReplace(sheet, result);
                        }
                    }
                }
                // 根据原始记录的 模板名 找到 对应的 sheet名称。
//                XSSFSheet sheet = wb.getSheet(data.getCheckItemName());
//                if (sheet != null) {
//                    // 替换原始记录模板数据
//                    ExcelReplaceUtil.ExcelReplace(sheet, result);
//                }
            }
        }
        // 把 XSSFWorkbook 转为 InputStream
        InputStream input = AsposeUtil.createExcelStream(wb);
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
     *
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
        methodUpdateItemUrl(GenID.getID()+"."+arrays[arrays.length-1], input, ids, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
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
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishCheckItemReview(ExcelInsertVo excelInsertVo, Long userId) throws Exception {
        if(excelInsertVo.getInstrumentStatus().equals("否") || excelInsertVo.getOriginalRecordStatus().equals("否") || excelInsertVo.getData().equals("否"))
        {
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
                // 更新检测项
                sampleEntityMapper.updateItemReview(excelInsertVo);
            }
        }
        // 通过检测项id 获取数据Excel
        Integer[] array = new Integer[excelInsertVo.getList().size()];
        if (array.length == 1) {
            array[0] = excelInsertVo.getList().get(0);
        } else {
            for (int i = 0; i < array.length ; i++) {
                array[i] = excelInsertVo.getList().get(i);
            }
        }
        // 私有方法 处理 复核人签名信息。
        PDFHelper3.getLicense();
        String saveExcel = methodReviewExcel(array, userId);
//        System.out.println(" saveExcel ==  " + saveExcel);
        //上传文件到文件服务器、删除本地临时缓存的文件
        String[] arrays = saveExcel.split("\\.");
        String saveFileUrl = GenID.getID() + "." + arrays[arrays.length - 1];
//        String saveFileUrl = arrays[arrays.length - 1];
        InputStream input = new FileInputStream(saveExcel);
        // 私有方法 更新 产品附件及报告附件内容。
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(array);
        methodUpdateItemUrl(saveFileUrl, input, array, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
        // 删除本地文件
        FileAndFolderUtil.delete(saveFileUrl);
        FileAndFolderUtil.delete(saveExcel);
        input.close();
        return true;
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
        Map<String, String> mapSheet = getSheetMap(saveExcel);
        List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(array);
        if (!CollectionUtils.isEmpty(dataEntitys)) {
            for (int i = 0; i < dataEntitys.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                TaskIdEntity taskIdEntity = dataEntitys.get(i);
                ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                for (String key : mapSheet.keySet()) {
                    // 替换 sheet名
                    if (taskIdEntity.getCheckItemName().contains(key)) {
                        taskIdEntity.setCheckItemName(key);
                    }
                }
                excelInsertVo1.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo1.setRecordType("复核：");
                excelInsertVo1.setImags(testImags);
                // key 使用 sheet名加类型进行拼接
                map.put(excelInsertVo1.getSheetName() + excelInsertVo1.getRecordType(), excelInsertVo1);
                ExcelInsertVo excelInsertVo = new ExcelInsertVo();
                excelInsertVo.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo.setMap(map);
                excelInsertVoList.add(excelInsertVo);
            }
        }
        // 清除图片
        ExcelImageUtils.seachXY(saveExcel, excelInsertVoList, newFilePath);
        // 插入图片
        ExcelImageUtils.inserImage(newFilePath, excelInsertVoList, saveExcel);
        // 去除excel 中标记
        InputStream fileStream2 = new FileInputStream(saveExcel);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream2);
        // 调用方法 清除sheet名 = Evaluation Warning
        ExcelReplaceUtil.removeOtherSheets("Evaluation Warning", wb);
        fileStream2.close();
        OutputStream f = new FileOutputStream(saveExcel);
        wb.write(f);
        f.close();
        // 删除附件
        FileAndFolderUtil.delete(newFilePath);
        // 删除图片信息
        for (int i = 0; i < testImags.length - 1; i++) {
            FileAndFolderUtil.delete(testImags[i]);
        }
        // 返回塞入的复核人签名本地附件
        return saveExcel;
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
    public String methodUpdateItemUrl(String saveFileUrl, InputStream input, Integer[] array, Long entrustId, Integer sampleId) throws IOException {
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
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
        // 处理模糊比较 sheetName
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(saveExcel);
        int count = workbook.getWorksheets().getCount();
        Map<String, String> mapSheet = new HashMap<>();
        for (int o = 0; o < count; o++) {
            // 设置全部可读
            workbook.getWorksheets().get(o).setVisible(true);
            String sheetName = workbook.getWorksheets().get(o).getName();
            mapSheet.put(sheetName, sheetName);
        }
        if (!CollectionUtils.isEmpty(dataEntitys)) {
            for (int i = 0; i < dataEntitys.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                TaskIdEntity taskIdEntity = dataEntitys.get(i);
                for (String key : mapSheet.keySet()) {
                    // 替换 sheet名
                    if (taskIdEntity.getCheckItemName().contains(key)) {
                        taskIdEntity.setCheckItemName(key);
                    }
                }
                ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                excelInsertVo1.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo1.setRecordType("检测：");
                excelInsertVo1.setImags(testImags);
                // key 使用 sheet名加类型进行拼接
                map.put(excelInsertVo1.getSheetName() + excelInsertVo1.getRecordType(), excelInsertVo1);
                ExcelInsertVo excelInsertVo2 = new ExcelInsertVo();
                excelInsertVo2.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo2.setRecordType("记录：");
                excelInsertVo2.setImags(recordImags);
                // key 使用 sheet名加类型进行拼接
                map.put(excelInsertVo2.getSheetName() + excelInsertVo2.getRecordType(), excelInsertVo2);
                ExcelInsertVo excelInsertVo = new ExcelInsertVo();
                excelInsertVo.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo.setMap(map);
                excelInsertVoList.add(excelInsertVo);
            }
        }
        // excel 插入图片
        String[] names = file.getFileName().split("\\.");
        String newFilePath = dir + GenID.getID() + "." + names[1];
        // 清除图片
        ExcelImageUtils.seachXY(saveExcel, excelInsertVoList, newFilePath);
        // 插入图片
        ExcelImageUtils.inserImage(newFilePath, excelInsertVoList, saveExcel);
        // 去除excel 中标记
        // 创建一个文件输入流
        FileInputStream fileStream = new FileInputStream(new File(saveExcel));
        // 创建一个 XSSFWorkbook 对象，用于处理 .xlsx 格式的 Excel 文件
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        ExcelReplaceUtil.removeOtherSheets("Evaluation Warning", wb);
        OutputStream f = new FileOutputStream(saveExcel);
        wb.write(f);
        f.close();
        fileStream.close();
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

    public Map<Integer, Integer> inserItemPage(XSSFWorkbook wb, Long taskId) {
        // 获取检测项的数据。
        List<TaskIdEntity> list = taskMapper.selectItemPages(taskId);
        // key = sheet标号 、value =序号
        Map<Integer, Integer> countMap = new HashMap<>();
        // 序号
        Integer number = 1;
        // 循环遍历所有工作表
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            // 获取第i个工作表
            XSSFSheet sheet = wb.getSheetAt(i);
            if (sheet != null) {
                // 获取工作表的名称
                String sheetName = sheet.getSheetName();
                for (int j = 0; j < list.size(); j++) {
                    TaskIdEntity data = list.get(j);
                    if (data.getCheckItemName().contains(sheetName)) {
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

//    public Map<Integer, Integer> getCountMap(InputStream fileStream,Long taskId) throws Exception {
//        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
//        Map<Integer, Integer> countMap = inserItemPage(wb, taskId);
//        fileStream.close();
//        return countMap;
//
//    }

    public ExcelSheetDataVo getSaveFile(InputStream fileStream, Long taskId) throws Exception {
        ExcelSheetDataVo excelSheetDataVo = new ExcelSheetDataVo();
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        Map<Integer, Integer> countMap = inserItemPage(wb, taskId);
        fileStream.close();
        // 把 XSSFWorkbook 转为 InputStream
        InputStream input = AsposeUtil.createExcelStream(wb);
        int total = countMap.size();
        com.aspose.cells.Workbook document = new Workbook(input);
        handlerPage(document, countMap, total);
        String path = dir + GenID.getID() + ".xlsx";
        document.save(path);
        input.close();
        excelSheetDataVo.setCountMap(countMap);
        excelSheetDataVo.setSaveFile(path);
        // 创建一个文件输入流
        return excelSheetDataVo;
    }

    /**
     * 通过本地附件 获取 所有的 sheetName
      * @param saveFile
     * @return
     * @throws IOException
     */
    public Map<String,String> getSheetMap(String saveFile) throws IOException {
        Map<String,String> countMap = new HashMap<>();
        InputStream fileStream = new FileInputStream(saveFile);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 循环遍历所有工作表
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            // 获取第i个工作表
            XSSFSheet sheet = wb.getSheetAt(i);
            if (sheet != null) {
                // 获取工作表的名称
                String sheetName = sheet.getSheetName();
                countMap.put(sheetName,sheetName);
            }
        }
        fileStream.close();
        return countMap;
    }

    @Override
    public String updateExcelVisible(String saveFileUrl,Integer[] array, InputStream inputStream) throws IOException {
        // 私有方法 更新 产品附件及报告附件内容。
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(array);
        methodUpdateItemUrl(saveFileUrl, inputStream, array, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
        return null;
    }


//    public static void main(String[] args) throws Exception {
//        String fileName = "D:\\doc\\e-iceblue\\4602092399671262.xlsx";
//        InputStream fileStream = new FileInputStream(fileName);
//        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
//        Map<Integer, Integer> map = new PageOfficeServiceImpl().inserItemPage(wb);
//        int total = map.size();
//        // 把 XSSFWorkbook 转为 InputStream
//        InputStream input000 = AsposeUtil.createExcelStream(wb);
//        fileStream.close();
//        com.aspose.cells.Workbook document = new Workbook(input000);
//         handlerPage(document,map,total);
//        String path = "D:\\doc\\e-iceblue\\"+ "name" + ".xlsx";
//        document.save(path);
//        input000.close();
//    }
}
