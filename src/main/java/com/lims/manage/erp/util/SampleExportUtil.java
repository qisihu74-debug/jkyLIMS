package com.lims.manage.erp.util;

import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.service.impl.ReportServiceImpl;
import com.lims.manage.erp.vo.SampleOutPutVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/3/2 9:03
 */
public class SampleExportUtil {
    QiYueSuoEntity qiYueSuoEntity = new QiYueSuoEntity();

    /**
     * 样品入库登记表Excel导出
     * @param list
     * @return
     * @throws Exception
     */
    public InputStream sampleRetentionExport(List<SampleOutPutVo> list) throws Exception {
        InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "样品入库登记表.xlsx");
        Workbook workbook = new Workbook(fileStream);
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Integer n = 3;
        String row = "A";
        ReportServiceImpl letterCycle = new ReportServiceImpl();
        for (int i = 0; i < list.size(); i++) {
            SampleOutPutVo sampleOutPutVo = list.get(i);
            int number = i+1;
            //在sheet里创建第三行
            // 序号
            cells.get(row+n).setValue(number);
            row = letterCycle.getNextUpEn(row);
            // 任务单号
            cells.get(row+n).setValue(sampleOutPutVo.getTaskCode());
            row = letterCycle.getNextUpEn(row);
            // 样品编号
            cells.get(row+n).setValue(sampleOutPutVo.getSampleCode());
            row = letterCycle.getNextUpEn(row);
            // 样品名称
            cells.get(row+n).setValue(sampleOutPutVo.getSampleName());
            row = letterCycle.getNextUpEn(row);
            // 入库时间
            if (sampleOutPutVo.getAcceptanceDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getAcceptanceDate());
                cells.get(row+n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            }else {
                row = letterCycle.getNextUpEn(row);
            }
            // 要求完成日期
            if (sampleOutPutVo.getRequestDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getRequestDate());
                cells.get(row+n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            }
            else {
                row = letterCycle.getNextUpEn(row);
            }
            // 留样人
            cells.get(row+n).setValue(sampleOutPutVo.getSampleHolder());
            row = letterCycle.getNextUpEn(row);
            // 保留日期
            cells.get(row+n).setValue(sampleOutPutVo.getSampleRetentionPeriod());
            row = letterCycle.getNextUpEn(row);
            // 处理人
            cells.get(row+n).setValue(sampleOutPutVo.getHandler());
            row = letterCycle.getNextUpEn(row);
            // 批准人
            cells.get(row+n).setValue(sampleOutPutVo.getApprover());
            row = letterCycle.getNextUpEn(row);
            // 处理日期
            if (sampleOutPutVo.getSellOffDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getSellOffDate());
                cells.get(row+n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            }else {
                row = letterCycle.getNextUpEn(row);
            }
            // 处理方式
            cells.get(row+n).setValue(sampleOutPutVo.getSampleProcessMode());
            row = letterCycle.getNextUpEn(row);
            //  留样备注
            cells.get(row+n).setValue(sampleOutPutVo.getSampleReservedRemrk());
            row = "A";
            n++;
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath()+"reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath()+"reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 样品出入库登记表Excel导出
     * @param list
     * @return
     * @throws Exception
     */
    public InputStream sampleOutPutExport(List<SampleOutPutVo> list) throws Exception {
        InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "委托样品出入库登记表.xlsx");
        Workbook workbook = new Workbook(fileStream);
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Integer n = 2;
        String row = "A";
        ReportServiceImpl letterCycle = new ReportServiceImpl();
        for (int i = 0; i < list.size(); i++) {
            SampleOutPutVo sampleOutPutVo = list.get(i);
            int number = i+1;
            //在sheet里创建第三行
            // 序号
            cells.get(row+n).setValue(number);
            row = letterCycle.getNextUpEn(row);
            // 任务单号
            cells.get(row+n).setValue(sampleOutPutVo.getTaskCode());
            row = letterCycle.getNextUpEn(row);
            // 样品编号
            cells.get(row+n).setValue(sampleOutPutVo.getSampleCode());
            row = letterCycle.getNextUpEn(row);
            // 样品名称
            cells.get(row+n).setValue(sampleOutPutVo.getSampleName());
            row = letterCycle.getNextUpEn(row);
            // 入库时间
            if (sampleOutPutVo.getAcceptanceDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getAcceptanceDate());
                cells.get(row+n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            }else {
                row = letterCycle.getNextUpEn(row);
            }
            // 样品接收人：
            if (sampleOutPutVo.getSampleTaker() != null) {
                cells.get(row+n).setValue(sampleOutPutVo.getTaskPublisher());
                row = letterCycle.getNextUpEn(row);
            }else {
                row = letterCycle.getNextUpEn(row);
            }
            // 出库日期
            if (sampleOutPutVo.getOutboundDeliveryDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getOutboundDeliveryDate());
                cells.get(row+n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            }
            else {
                row = letterCycle.getNextUpEn(row);
            }
            // 领样人
            cells.get(row+n).setValue(sampleOutPutVo.getSampleTaker());
            row = letterCycle.getNextUpEn(row);
            // 样品出入库备注
            cells.get(row+n).setValue(sampleOutPutVo.getSampleOutPutRemrk());
            row = "A";
            n++;
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath()+"reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath()+"reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }


}
