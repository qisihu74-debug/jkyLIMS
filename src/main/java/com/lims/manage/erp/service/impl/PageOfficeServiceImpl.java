package com.lims.manage.erp.service.impl;

import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.ReqParamBean;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.mapper.TaskMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    TaskServiceImpl taskService = new TaskServiceImpl();
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public String getProductExcelUrl(ReqParamBean bean) throws IOException {
        // -- 开始： 读取产品excel数据集
        InputStream fileStream = MinIoUtil.getFileStream("file-resources", "shuini.xlsx");
//        POIFSFileSystem fs = new POIFSFileSystem(fileStream);

        XSSFWorkbook wb = new XSSFWorkbook(fileStream);
//        HSSFWorkbook wb = new HSSFWorkbook(fileStream);
        // 创建：产品excel 汇总的 原始记录Excel 数据
//        HSSFWorkbook wb = new HSSFWorkbook(fs);
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
        InputStream input = createExcelStream(wb);
        String excelUrl = MinIoUtil.upload("file-resources",  "987654.xls",input,"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        System.out.println("excelUrl" + excelUrl);
        return excelUrl;
    }

    /**
     * 流转化
     * @param outputStream
     * @return
     */
    public static InputStream convertIo(FileOutputStream outputStream){
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] bytes = b.toByteArray();
        try {
            outputStream.write(bytes);
        }catch (Exception e){
//            logger.error("流转换失败:{}",e);
        }
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

    /**
     *
     * <p>
     * description:将workbook对象转化为输入流：过程是利用ByteArrayOutputStream为缓存，在将此ByteArrayOutputStream转化为InputStream
     * 利用到了ByteArrayOutputStream来做缓存，先将文件写入其中，然后将其转为字节数组，最后利用ByteArrayInputStream转为输入流，供后续使用
     * </p>
     * @author AbnerLi
     * @date 2017年9月29日上午8:56:18
     * @param students
     * @return
     */
//    @Override
    public  InputStream createExcelStream(XSSFWorkbook workbook) {
        InputStream in = null;
        try
        {
            //临时
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //创建临时文件
            workbook.write(out);
            byte [] bookByteAry = out.toByteArray();
            in = new ByteArrayInputStream(bookByteAry);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return in;
    }

}
