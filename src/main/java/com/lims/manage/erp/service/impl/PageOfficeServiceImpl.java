package com.lims.manage.erp.service.impl;

import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.ReqParamBean;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.ExcelInsertVo;
import com.lims.manage.erp.vo.OriginalRecordDataVo;
import com.lims.manage.erp.vo.TaskListParamVo;
import com.zhuozhengsoft.pageoffice.FileSaver;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    TaskServiceImpl taskService = new TaskServiceImpl();
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TestProductItemDao testProductItemDao;


    @Override
    public String getProductExcelUrl(Integer[] ids) throws IOException {
        // 通过检测项主键 获取样品生成附件是否存在。
        String productExcelUrl = testProductItemDao.getProductExcelUrl(77677);
        InputStream fileStream = null;
        if (StringUtils.isEmpty(productExcelUrl)) {
            // -- 开始： 读取产品附件
            productExcelUrl = testProductItemDao.getProductExcel(77677);
            if (StringUtils.isEmpty(productExcelUrl)) {
                logger.info("读取产品excel为null  检测项主键中产品无产品附件");
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
        List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(ids);
        Long entrustId = null;
        Integer sampleId = null;
        // 批量获取 检测项id（有可能对应多个模板） 再进行填充。
        // 通过检测项id 获取 相应的 id关联信息。
        for (int i = 0; i < dataEntitys.size(); i++) {
            TaskIdEntity data = dataEntitys.get(i);
            entrustId = data.getEntrustmentId();
            sampleId = data.getSampleId();
            // 检测项 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
            if (data != null && !data.getState().equals(3)) {
                // 有序信息。
                OriginalRecordDataVo originalData = taskService.getOriginalData(data.getTaskId(), data.getSampleId(), data.getCheckItemId(), data.getIdItem());
                Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
                result.put("result", originalData);
                // 根据原始记录的 模板名 找到 对应的 sheet名称。
                XSSFSheet sheet = wb.getSheet(data.getOriginalName());
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
        String excelUrl = MinIoUtil.upload("file-resources", array[array.length-1], input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 更新 样品Excel附件
        testProductItemDao.updateProductExcelUrl(entrustId, sampleId, excelUrl);
        System.out.println("excelUrl" + excelUrl);
        return excelUrl;
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
        String SaveExcel = dir + file.getFileName();
        // 文件路径
        file.saveToFile(SaveExcel);
        System.out.println("文件返回值 == " + SaveExcel);
        // 查询用户id及签名信息
        // 检测人集合
        List<Long> testSetLong = new ArrayList<>();
        testSetLong.add(1647502446459100L);
        testSetLong.add(1647502682230103L);
        // 检测人签名数组图片
        String[] testImags = new String[testSetLong.size()];
        // 调用方法处理 签名图片存放数组中。
        methodUrlImags(testSetLong, testImags);
        // 记录人集合
        List<Long> recordSetLong = new ArrayList<>();
        recordSetLong.add(1647502446459100L);
        recordSetLong.add(1647502682230103L);
        // 记录人签名数组图片
        String[] recordImags = new String[recordSetLong.size()];
        // 调用方法处理 签名图片存放数组中。
        methodUrlImags(recordSetLong, recordImags);
        System.out.println("暂停看输出");
        List<ExcelInsertVo> excelInsertVoList = new ArrayList<>();
        List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(ids);
        if (!CollectionUtils.isEmpty(dataEntitys)) {
            for (int i = 0; i < dataEntitys.size(); i++) {
                TaskIdEntity taskIdEntity = dataEntitys.get(i);
                ExcelInsertVo excelInsertVo1 = new ExcelInsertVo();
                excelInsertVo1.setSheetName(taskIdEntity.getOriginalName());
                excelInsertVo1.setRecordType("检测：");
                excelInsertVo1.setImags(testImags);
                excelInsertVoList.add(excelInsertVo1);
                ExcelInsertVo excelInsertVo2 = new ExcelInsertVo();
                excelInsertVo2.setSheetName(taskIdEntity.getOriginalName());
                excelInsertVo2.setRecordType("记录：");
                excelInsertVo2.setImags(recordImags);
                excelInsertVoList.add(excelInsertVo2);
            }
        }
        // excel 插入图片
        String newFilePath = dir + GenID.getID() + file.getFileName();
        System.out.println(" newFilePath == " + newFilePath);
        // 图片插入至excel中     SaveExcel 已经删除。
        ExcelImageUtils.ExcelInsertImage(SaveExcel, excelInsertVoList, newFilePath);
        // 去除excel 中标记
        InputStream fileStream = new FileInputStream(newFilePath);
        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
        // 调用方法
        ExcelReplaceUtil.removeOtherSheets("Evaluation Warning", wb);
        fileStream.close();
        OutputStream f = new FileOutputStream("D:\\doc\\e-iceblue\\演示插入结果RemoveSheetName.xlsx");
        wb.write(f);
        f.close();
        return null;
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


}
