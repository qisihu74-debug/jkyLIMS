package com.lims.manage.erp.service.impl;

import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.ExcelInsertVo;
import com.lims.manage.erp.vo.OriginalRecordDataVo;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.zhuozhengsoft.pageoffice.FileSaver;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


    @Override
    public String getProductExcelUrl(Integer[] ids) throws IOException {
        // 通过检测项主键 获取样品生成附件是否存在。
        String productExcelUrl = null;
        ExcelInsertVo excelInsertVo = testProductItemDao.getExcelUrl(ids[0]);
        if (excelInsertVo != null) {
            if (excelInsertVo.getReportEditUrl() != null) {
                // 优先拿 报告附件
                productExcelUrl = excelInsertVo.getReportEditUrl();
            }
            if (excelInsertVo.getReportEditUrl() ==null && excelInsertVo.getProductExcelUrl() != null) {
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
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
        // 批量获取 检测项id（有可能对应多个模板） 再进行填充。
        // 通过检测项id 获取 相应的 id关联信息。
        for (int i = 0; i < dataEntitys.size(); i++) {
            TaskIdEntity data = dataEntitys.get(i);
            // 检测项 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
            if (data != null && !data.getState().equals(3)) {
                // 有序信息。
                OriginalRecordDataVo originalData = taskService.getOriginalData(data.getTaskId(), data.getSampleId(), data.getCheckItemId(), data.getIdItem());
                Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
                result.put("result", originalData);
                // 根据原始记录的 模板名 找到 对应的 sheet名称。
                XSSFSheet sheet = wb.getSheet(data.getCheckItemName());
                if (sheet != null) {
                    // 替换原始记录模板数据
                    ExcelReplaceUtil.ExcelReplace(sheet, result);
                }
            }
        }
        // 把 XSSFWorkbook 转为 InputStream
        InputStream input = AsposeUtil.createExcelStream(wb);
        // 把 wb 数据 存放上传
        String[] array = productExcelUrl.split("/");
        if (excelInsertVo == null) {
            String excelUrl = MinIoUtil.upload("file-resources", GenID.getID() + array[array.length - 1], input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 更新 样品Excel附件
            testProductItemDao.updateProductExcelUrl(dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId(), excelUrl);
            return excelUrl;
        } else {
            // 私有方法 更新 产品附件及报告附件内容。
            return methodUpdateItemUrl(GenID.getID() + array[array.length - 1], input, ids, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
        }
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
        methodUrlImags(testSetLong, testImags);
        // 记录人集合
        List<Long> recordSetLong = new ArrayList<>();
        recordSetLong.add(Long.valueOf(recordSet));
        // 记录人签名数组图片
        String[] recordImags = new String[recordSetLong.size()];
        // 如果 检测人与记录人相同 签名数组复用即可。
        if(testSet.equals(recordSet)){
            recordImags[0] = testImags[0];
        }else {
            // 调用方法处理 签名图片存放数组中。
            methodUrlImags(recordSetLong, recordImags);
        }
        List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
        if (!CollectionUtils.isEmpty(dataEntitys)) {
            for (int i = 0; i < dataEntitys.size(); i++) {
                TaskIdEntity taskIdEntity = dataEntitys.get(i);
                ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                excelInsertVo1.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo1.setRecordType("检测：");
                excelInsertVo1.setImags(testImags);
                excelInsertVoList.add(excelInsertVo1);
                ExcelInsertVo excelInsertVo2 = new ExcelInsertVo();
                excelInsertVo2.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo2.setRecordType("记录：");
                excelInsertVo2.setImags(recordImags);
                excelInsertVoList.add(excelInsertVo2);
            }
        }
        // excel 插入图片
        String[] names = file.getFileName().split("\\.");
        String newFilePath = dir + GenID.getID() +"."+ names[1];
        // 图片插入至excel中     SaveExcel 已经删除。
        ExcelImageUtils.ExcelInsertImage(saveExcel, excelInsertVoList, newFilePath, true);
        // 去除excel 中标记
        InputStream fileStream = new FileInputStream(newFilePath);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 调用方法 清除sheet名 = Evaluation Warning
        ExcelReplaceUtil.removeOtherSheets("Evaluation Warning", wb);
        fileStream.close();
        OutputStream f = new FileOutputStream(saveExcel);
        wb.write(f);
        f.close();
        // 删除附件
        FileAndFolderUtil.delete(newFilePath);
        // 删除图片信息
        for (int i = 0; i < testImags.length - 1; i++) {
            FileAndFolderUtil.delete(testImags[i]);
        }
        // 检测人与记录不相同时
        if(!testSet.equals(recordSet)){
            for (int i = 0; i < recordImags.length - 1; i++) {
                FileAndFolderUtil.delete(recordImags[i]);
            }
        }
        return saveExcel;
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
            for (int i = 0; i < list.size(); i++) {
                TaskListParamVo taskListParamVo = list.get(i);
                // 获取的url签名信息 存放至本地附件
                String[] image = taskListParamVo.getSignatureUrl().split("/");
                String downloadDir = dir + GenID.getID() + image[image.length - 1];
                System.out.println("recordImags + downloadDir == " + downloadDir);
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
        FileInputStream fileStream = new FileInputStream(excelUrl);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 把 XSSFWorkbook 转为 InputStream
        InputStream input = AsposeUtil.createExcelStream(wb);
        String[] arrays = excelUrl.split("\\.");
        methodUpdateItemUrl(GenID.getID()+"."+arrays[arrays.length-1], input, ids, dataEntitys.get(0).getEntrustmentId(), dataEntitys.get(0).getSampleId());
        // 删除附件
        FileAndFolderUtil.delete(excelUrl);
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
            for (int i = 0; i < array.length - 1; i++) {
                array[i] = excelInsertVo.getList().get(i);
            }
        }
        // 私有方法 处理 复核人签名信息。
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
        List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(array);
        if (!CollectionUtils.isEmpty(dataEntitys)) {
            for (int i = 0; i < dataEntitys.size(); i++) {
                TaskIdEntity taskIdEntity = dataEntitys.get(i);
                ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                excelInsertVo1.setSheetName(taskIdEntity.getCheckItemName());
                excelInsertVo1.setRecordType("复核：");
                excelInsertVo1.setImags(testImags);
                excelInsertVoList.add(excelInsertVo1);
            }
        }
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
                } catch (Exception e) {
                    logger.info("读取产品URL附件异常 " + excelInsertVo2.getProductExcelUrl() + e);
                }
            }
        }
        // 图片插入至excel中     SaveExcel 已经删除。
        ExcelImageUtils.ExcelInsertImage(saveExcel, excelInsertVoList, newFilePath, false);
        // 去除excel 中标记
        InputStream fileStream2 = new FileInputStream(newFilePath);
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
    public String methodUpdateItemUrl(String saveFileUrl, InputStream input, Integer[] array, Long entrustId, Integer sampleId) {
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
            }
            if (excelInsertVo.getProductExcelUrl() != null) {
                String productUrl = "";
                // 本地附件 上传 到远端仓库
                if (StringUtils.isEmpty(excelUrl) || "".equals(excelUrl)) {
                    // 报告附件没有上传 上传产品附件
                    productUrl = MinIoUtil.upload("file-resources", saveFileUrl, input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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
                    } catch (Exception e) {
                        logger.info("读取报告附件URL异常 " + excelUrl + e);
                    }
                }
                String[] urls = excelInsertVo.getProductExcelUrl().split("/");
                MinIoUtil.deleteFile("file-resources", urls[urls.length - 1]);
                // 更新 报告Excel附件
                testProductItemDao.updateProductExcelUrl(entrustId, sampleId, productUrl);
                return productUrl;
            }
        }
        return null;
    }
}
