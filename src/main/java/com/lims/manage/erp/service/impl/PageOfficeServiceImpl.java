package com.lims.manage.erp.service.impl;

import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.ReqParamBean;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.OriginalRecordDataVo;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @Autowired
    TaskServiceImpl taskService = new TaskServiceImpl();
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TestProductItemDao testProductItemDao;

    @Override
    public String getProductExcelUrl(ReqParamBean bean) throws IOException {
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
//        Integer[] ids = new Integer[bean.getList().size()];
//        for (int j = 0; j < ids.length; j++) {
//            ids[j] = bean.getList().get(j).getItemId();
//        }
        Integer[] ids = new Integer[4];
        ids[0] = 77677;
        ids[1] = 77678;
        ids[2] = 77679;
        ids[3] = 77680;
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
                    // 进行指定标识符数据替换
                    XLSTransformer transformer = new XLSTransformer();
                    transformer.transformWorkbook(sheet.getWorkbook(), result);
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
    public String getProductExcelUrl0(ReqParamBean bean) throws IOException {
        // -- 开始： 读取产品excel数据集
        InputStream fileStream = MinIoUtil.getFileStream("file-resources", "shuini.xlsx");
//        POIFSFileSystem fs = new POIFSFileSystem(fileStream);

        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
//        HSSFWorkbook wb = new HSSFWorkbook(fileStream);
        // 创建：产品excel 汇总的 原始记录Excel 数据
//        HSSFWorkbook wb = new HSSFWorkbook(fs);
//        Integer[] ids = new Integer[bean.getList().size()];
//        for (int j = 0; j < ids.length; j++) {
//            ids[j] = bean.getList().get(j).getItemId();
//        }
        Integer[] ids = new Integer[4];
        ids[0] = 77677;
        ids[1] = 77678;
        ids[2] = 77679;
        ids[3] = 77680;
        List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(ids);
        // 批量获取 检测项id（有可能对应多个模板） 再进行填充。
        // 通过检测项id 获取 相应的 id关联信息。
        for (int i = 0; i < dataEntitys.size(); i++) {
            TaskIdEntity data = dataEntitys.get(i);
            // 有序信息。
            OriginalRecordDataVo originalData = taskService.getOriginalData(data.getTaskId(), data.getSampleId(), data.getCheckItemId(), data.getIdItem());
            Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
            result.put("result", originalData);
            // 根据原始记录的 模板名 找到 对应的 sheet名称。
            XSSFSheet sheet = wb.getSheet(data.getOriginalName());
            if (sheet != null) {
                // 进行指定标识符数据替换
                XLSTransformer transformer = new XLSTransformer();
                transformer.transformWorkbook(sheet.getWorkbook(), result);
            }
        }
//        // 把 wb 数据 存放上传
        InputStream input = AsposeUtil.createExcelStream(wb);
        String excelUrl = MinIoUtil.upload("file-resources", "987654.xls", input, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        System.out.println("excelUrl" + excelUrl);
        return excelUrl;
    }

}
