package com.lims.manage.erp.util;

import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.service.impl.ReportServiceImpl;
import com.lims.manage.erp.vo.SampleOutPutVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        //按时间顺序排序
        if (CollectionUtils.isNotEmpty(list)) {
            list = list.stream()
                    .sorted(Comparator.comparing(SampleOutPutVo::getAcceptanceDate)).collect(Collectors.toList());
        }
        List<SampleOutPutVo> oldList = new ArrayList<>();
        List<SampleOutPutVo> newList = new ArrayList<>();
        // 根据受理日期 区分数据集合
        if (CollectionUtils.isNotEmpty(list)) {
//            list.stream().filter(SampleOutPutVo -> SampleOutPutVo.getAcceptanceDate() )
            for (SampleOutPutVo sampleOutPutVo : list) {
                if (sampleOutPutVo.getAcceptanceDate() != null) {
                    try {
                        //实现将字符串转成⽇期类型
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date1 = sdf.parse("2023-07-31 23:59:59");
                        // 测试此日期是否在指定日期之后.时间不平等
                        if (!sampleOutPutVo.getAcceptanceDate().after(date1) && !sampleOutPutVo.getAcceptanceDate().equals(date1)) {
                            oldList.add(sampleOutPutVo);
                        } else {
                            newList.add(sampleOutPutVo);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 优先考虑合并操作
            if (CollectionUtils.isNotEmpty(oldList) && CollectionUtils.isNotEmpty(newList)) {
                // 进行同时输出： 旧版本与新版本。
                InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "样品入库登记表(合并).xlsx");
                if (fileStream == null) {
                    // 空模板 返回空
                    return null;
                }
                return sampleRetentionMergerExport(newList, oldList, fileStream);
            }
            if (CollectionUtils.isNotEmpty(oldList)) {
                // date <= 2023-07-31 23:59:59
                InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "样品入库登记表.xlsx");
                if (fileStream == null) {
                    // 空模板 返回空
                    return null;
                }
                return sampleRetentionOldVoidExport(oldList, fileStream);
            }
            if (CollectionUtils.isNotEmpty(newList)) {
                // data >= 2023-07-31 23:59:59
                InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "样品入库封存记录.xlsx");
                if (fileStream == null) {
                    // 空模板 返回空
                    return null;
                }
                return sampleRetentionNewVoidExport(newList, fileStream);
            }
        }
        return null;
    }

    /**
     * 样品出入库登记表Excel导出
     *
     * @param list
     * @return
     * @throws Exception
     */
    public InputStream sampleOutPutExport(List<SampleOutPutVo> list) throws Exception {
        if (CollectionUtils.isNotEmpty(list)) {
            list = list.stream()
                    .sorted(Comparator.comparing(SampleOutPutVo::getAcceptanceDate)).collect(Collectors.toList());
        }
        List<SampleOutPutVo> oldList = new ArrayList<>();
        List<SampleOutPutVo> newList = new ArrayList<>();
        // 根据受理日期 区分数据集合
        if (CollectionUtils.isNotEmpty(list)) {
            for (SampleOutPutVo sampleOutPutVo : list) {
                if (sampleOutPutVo.getAcceptanceDate() != null) {
                    try {
                        //实现将字符串转成⽇期类型
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date1 = sdf.parse("2023-07-31 23:59:59");
                        // 测试此日期是否在指定日期之后.时间不平等
                        if (!sampleOutPutVo.getAcceptanceDate().after(date1) && !sampleOutPutVo.getAcceptanceDate().equals(date1)) {
                            oldList.add(sampleOutPutVo);
                        } else {
                            newList.add(sampleOutPutVo);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 优先考虑合并操作
            if (CollectionUtils.isNotEmpty(oldList) && CollectionUtils.isNotEmpty(newList)) {
                // 进行同时输出： 旧版本与新版本。
                InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "委托样品出入库登记表(合并).xlsx");
                if (fileStream == null) {
                    // 空模板 返回空
                    return null;
                }
                return sampleOutPutMergerExport(newList, oldList, fileStream);
            }
            if (CollectionUtils.isNotEmpty(oldList)) {
                // date <= 2023-07-31 23:59:59
                InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "委托样品出入库登记表(旧).xlsx");
                if (fileStream == null) {
                    // 空模板 返回空
                    return null;
                }
                return sampleOutPutOldExport(oldList, fileStream);
            }
            if (CollectionUtils.isNotEmpty(newList)) {
                // data >= 2023-07-31 23:59:59
                InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "委托样品出入库登记表(新).xlsx");
                if (fileStream == null) {
                    // 空模板 返回空
                    return null;
                }
                return sampleOutPutNewExport(newList, fileStream);
            }
        }
        return null;
    }

    /**
     * 样品留样 获取合并的留样列表
     *
     * @param newList
     * @param oldList
     * @param fileStream
     * @return
     * @throws Exception
     */
    public InputStream sampleRetentionMergerExport(List<SampleOutPutVo> newList, List<SampleOutPutVo> oldList, InputStream fileStream) throws Exception {
        Workbook workbook = new Workbook(fileStream);
        // 合并模板中：sheet1 = 旧模板
        Worksheet worksheet = workbook.getWorksheets().get("Sheet1");
        sampleRetentionOldExport(oldList, worksheet);
        // 合并模板中：sheet2 =新模板
        Worksheet worksheet2 = workbook.getWorksheets().get("Sheet2");
        sampleRetentionNewExport(newList, worksheet2);
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 样品出入库 获取合并的出入库列表
     *
     * @param newList
     * @param oldList
     * @param fileStream
     * @return
     * @throws Exception
     */
    public InputStream sampleOutPutMergerExport(List<SampleOutPutVo> newList, List<SampleOutPutVo> oldList, InputStream fileStream) throws Exception {
        Workbook workbook = new Workbook(fileStream);
        // 合并模板中：sheet1 = 旧模板
        Worksheet worksheet = workbook.getWorksheets().get("Sheet1");
        sampleOutPutOldExport(oldList, worksheet);
        // 合并模板中：sheet2 =新模板
        Worksheet worksheet2 = workbook.getWorksheets().get("Sheet2");
        sampleOutPutNewVoidExport(newList, worksheet2);
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 样品出入库 获取旧的出入库列表
     *
     * @param oldList
     * @param fileStream
     * @return
     * @throws Exception
     */
    public InputStream sampleOutPutOldExport(List<SampleOutPutVo> oldList, InputStream fileStream) throws Exception {
        Workbook workbook = new Workbook(fileStream);
        // 合并模板中：sheet1 = 旧模板
        Worksheet worksheet = workbook.getWorksheets().get(0);
        sampleOutPutOldExport(oldList, worksheet);
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 样品出入库 获取新的出入库列表
     *
     * @param newList
     * @param fileStream
     * @return
     * @throws Exception
     */
    public InputStream sampleOutPutNewExport(List<SampleOutPutVo> newList, InputStream fileStream) throws Exception {
        Workbook workbook = new Workbook(fileStream);
        // 合并模板中：sheet2 =新模板
        Worksheet worksheet2 = workbook.getWorksheets().get(0);
        sampleOutPutNewVoidExport(newList, worksheet2);
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 样品留样 获取旧的留样列表
     *
     * @param oldList
     * @param fileStream
     * @return
     * @throws Exception
     */
    public InputStream sampleRetentionOldVoidExport(List<SampleOutPutVo> oldList, InputStream fileStream) throws Exception {
        Workbook workbook = new Workbook(fileStream);
        // 合并模板中：sheet1 = 旧模板
        Worksheet worksheet = workbook.getWorksheets().get(0);
        sampleRetentionOldExport(oldList, worksheet);
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 样品留样 获取新的留样列表
     *
     * @param newList
     * @param fileStream
     * @return
     * @throws Exception
     */
    public InputStream sampleRetentionNewVoidExport(List<SampleOutPutVo> newList, InputStream fileStream) throws Exception {
        Workbook workbook = new Workbook(fileStream);
        // 合并模板中：sheet1 = 旧模板
        Worksheet worksheet = workbook.getWorksheets().get(0);
        sampleRetentionNewExport(newList, worksheet);
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath() + "reservedSample.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * 样品留样 获取旧的留样列表
     *
     * @param list
     * @param worksheet
     * @return
     * @throws Exception
     */
    public void sampleRetentionOldExport(List<SampleOutPutVo> list, Worksheet worksheet) {
        Cells cells = worksheet.getCells();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Integer n = 3;
        String row = "A";
        ReportServiceImpl letterCycle = new ReportServiceImpl();
        for (int i = 0; i < list.size(); i++) {
            SampleOutPutVo sampleOutPutVo = list.get(i);
            int number = i + 1;
            //在sheet里创建第三行
            // 序号
            cells.get(row + n).setValue(number);
            row = letterCycle.getNextUpEn(row);
            // 委托单号
            cells.get(row + n).setValue(sampleOutPutVo.getEntrustCode());
            row = letterCycle.getNextUpEn(row);
            // 样品编号
            cells.get(row + n).setValue(sampleOutPutVo.getSampleCode());
            row = letterCycle.getNextUpEn(row);
            // 样品名称
            cells.get(row + n).setValue(sampleOutPutVo.getSampleName());
            row = letterCycle.getNextUpEn(row);
            // 委托人
            cells.get(row + n).setValue(sampleOutPutVo.getEntrustPeople());
            row = letterCycle.getNextUpEn(row);
            // 入库时间
            if (sampleOutPutVo.getAcceptanceDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getAcceptanceDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 要求完成日期
            if (sampleOutPutVo.getRequestDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getRequestDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 留样人
            cells.get(row + n).setValue(sampleOutPutVo.getSampleHolder());
            row = letterCycle.getNextUpEn(row);
            // 保留日期
            cells.get(row + n).setValue(sampleOutPutVo.getSampleRetentionPeriod());
            row = letterCycle.getNextUpEn(row);
            // 处理人
            cells.get(row + n).setValue(sampleOutPutVo.getHandler());
            row = letterCycle.getNextUpEn(row);
            // 批准人
            cells.get(row + n).setValue(sampleOutPutVo.getApprover());
            row = letterCycle.getNextUpEn(row);
            // 处理日期
            if (sampleOutPutVo.getSellOffDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getSellOffDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 处理方式
            cells.get(row + n).setValue(sampleOutPutVo.getSampleProcessMode());
            row = letterCycle.getNextUpEn(row);
            //  留样备注
            cells.get(row + n).setValue(sampleOutPutVo.getSampleReservedRemrk());
            row = "A";
            n++;
        }
    }

    /**
     * 样品出入库 获取旧的出入库列表
     *
     * @param list
     * @param worksheet
     * @return
     * @throws Exception
     */
    public void sampleOutPutOldExport(List<SampleOutPutVo> list, Worksheet worksheet) {
        Cells cells = worksheet.getCells();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Integer n = 3;
        String row = "A";
        ReportServiceImpl letterCycle = new ReportServiceImpl();
        for (int i = 0; i < list.size(); i++) {
            SampleOutPutVo sampleOutPutVo = list.get(i);
            int number = i + 1;
            //在sheet里创建第三行
            // 序号
            cells.get(row + n).setValue(number);
            row = letterCycle.getNextUpEn(row);
            // 任务单号
            cells.get(row + n).setValue(sampleOutPutVo.getTaskCode());
            row = letterCycle.getNextUpEn(row);
            // 样品编号
            cells.get(row + n).setValue(sampleOutPutVo.getSampleCode());
            row = letterCycle.getNextUpEn(row);
            // 样品名称
            cells.get(row + n).setValue(sampleOutPutVo.getSampleName());
            row = letterCycle.getNextUpEn(row);
            // 入库时间
            if (sampleOutPutVo.getAcceptanceDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getAcceptanceDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 试验完成时间
            if (sampleOutPutVo.getRequestDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getRequestDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 样品接收人：
            if (sampleOutPutVo.getSampleTaker() != null) {
                cells.get(row + n).setValue(sampleOutPutVo.getTaskPublisher());
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 出库日期
            if (sampleOutPutVo.getOutboundDeliveryDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getOutboundDeliveryDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 领样人
            cells.get(row + n).setValue(sampleOutPutVo.getSampleTaker());
            row = letterCycle.getNextUpEn(row);
            // 样品出入库备注
            cells.get(row + n + 4).setValue(sampleOutPutVo.getSampleOutPutRemrk());
            row = "A";
            n++;
        }
    }

    /**
     * 样品出入库 获取新的出入库列表
     *
     * @param list
     * @param worksheet
     * @return
     * @throws Exception
     */
    public void sampleOutPutNewVoidExport(List<SampleOutPutVo> list, Worksheet worksheet) {
        Cells cells = worksheet.getCells();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Integer n = 2;
        String row = "A";
        ReportServiceImpl letterCycle = new ReportServiceImpl();
        for (int i = 0; i < list.size(); i++) {
            SampleOutPutVo sampleOutPutVo = list.get(i);
            int number = i + 1;
            //在sheet里创建第三行
            // 序号
            cells.get(row + n).setValue(number);
            row = letterCycle.getNextUpEn(row);
            // 任务单号
            cells.get(row + n).setValue(sampleOutPutVo.getTaskCode());
            row = letterCycle.getNextUpEn(row);
            // 样品编号
            cells.get(row + n).setValue(sampleOutPutVo.getSampleCode());
            row = letterCycle.getNextUpEn(row);
            // 样品名称
            cells.get(row + n).setValue(sampleOutPutVo.getSampleName());
            row = letterCycle.getNextUpEn(row);
            // 入库时间
            if (sampleOutPutVo.getAcceptanceDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getAcceptanceDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 样品接收人：
            if (sampleOutPutVo.getSampleTaker() != null) {
                cells.get(row + n).setValue(sampleOutPutVo.getTaskPublisher());
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 出库日期
            if (sampleOutPutVo.getOutboundDeliveryDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getOutboundDeliveryDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 领样人
            cells.get(row + n).setValue(sampleOutPutVo.getSampleTaker());
            row = letterCycle.getNextUpEn(row);
            // 样品出入库备注
            cells.get(row + n).setValue(sampleOutPutVo.getSampleOutPutRemrk());
            row = "A";
            n++;
        }
    }

    /**
     * 样品留样 获取新留样列表
     *
     * @param list
     * @param worksheet
     * @return
     * @throws Exception
     */
    public void sampleRetentionNewExport(List<SampleOutPutVo> list, Worksheet worksheet) {
        Cells cells = worksheet.getCells();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Integer n = 3;
        String row = "A";
        ReportServiceImpl letterCycle = new ReportServiceImpl();
        for (int i = 0; i < list.size(); i++) {
            SampleOutPutVo sampleOutPutVo = list.get(i);
            int number = i + 1;
            //在sheet里创建第三行
            // 序号
            cells.get(row + n).setValue(number);
            row = letterCycle.getNextUpEn(row);
            // 任务单号
            cells.get(row + n).setValue(sampleOutPutVo.getEntrustCode());
            row = letterCycle.getNextUpEn(row);
            // 样品编号
            cells.get(row + n).setValue(sampleOutPutVo.getSampleCode());
            row = letterCycle.getNextUpEn(row);
            // 样品名称
            cells.get(row + n).setValue(sampleOutPutVo.getSampleName());
            row = letterCycle.getNextUpEn(row);
//            // 委托人
//            cells.get(row + n).setValue(sampleOutPutVo.getEntrustPeople());
//            row = letterCycle.getNextUpEn(row);
            // 入库时间
            if (sampleOutPutVo.getAcceptanceDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getAcceptanceDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 要求完成日期
            if (sampleOutPutVo.getRequestDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getRequestDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 留样人
            cells.get(row + n).setValue(sampleOutPutVo.getSampleHolder());
            row = letterCycle.getNextUpEn(row);
            // 保留日期
            cells.get(row + n).setValue(sampleOutPutVo.getSampleRetentionPeriod());
            row = letterCycle.getNextUpEn(row);
            // 处理人
            cells.get(row + n).setValue(sampleOutPutVo.getHandler());
            row = letterCycle.getNextUpEn(row);
            // 批准人
            cells.get(row + n).setValue(sampleOutPutVo.getApprover());
            row = letterCycle.getNextUpEn(row);
            // 处理日期
            if (sampleOutPutVo.getSellOffDate() != null) {
                String dateString = formatter.format(sampleOutPutVo.getSellOffDate());
                cells.get(row + n).setValue(dateString);
                row = letterCycle.getNextUpEn(row);
            } else {
                row = letterCycle.getNextUpEn(row);
            }
            // 处理方式
            cells.get(row + n).setValue(sampleOutPutVo.getSampleProcessMode());
            row = letterCycle.getNextUpEn(row);
            //  留样备注
            cells.get(row + n).setValue(sampleOutPutVo.getSampleReservedRemrk());
            row = "A";
            n++;
        }
    }
}
