package com.lims.manage.erp.controller;

import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import com.lims.manage.erp.vo.TestEntrustedTaskRelVo;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
/**
 * @Author: DLC
 * @Date: 2023/3/15 10:18
 */
@Slf4j
@RestController
@RequestMapping("/batchTask/")
public class TaskBatchExportContoller {

    @Autowired
    private TaskService taskService;
    @Autowired
    TestDetectionService testDetectionService;
    @Autowired
    private TaskMapper taskMapper;

    /**
     * 通过委托单id集合
     * 批量下载doc任务单信息 存放指定文件夹信息
     * @param entrustIds
     * @param response
     */
    @PostMapping("batchDownloadEntrust")
    public void batchDownloadEntrust(@RequestParam("entrustIds") List<Long> entrustIds, HttpServletResponse response) {
        try {
            List<TestEntrustedTaskRelVo> list = taskMapper.getTaskIds(entrustIds);
            for(TestEntrustedTaskRelVo testEntrustedTaskRelVo : list){
                String fileName = "";
                // 3月13日前
                String str1 = "taskOrder11.docx";
                // 2023年3月13日后
                String str2 = "taskOrder20.docx";
                // 获取任务单下单时间 进行比较
                TaskTestEntity taskDetails = taskMapper.getTaskOrderTime(testEntrustedTaskRelVo.getTaskId());
                if(taskDetails!=null && taskDetails.getOrderTime()!=null){
                    Date date = null;
                    //实现将字符串转成⽇期类型
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        date = dateFormat.parse("2023-03-12 23:59:59");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // 截止至 2023-03-12 23:59:59 后 任务单附件为  String str2 = "taskOrder20.docx"
                    if(date.getTime() < taskDetails.getOrderTime().getTime()){
                        fileName = str2;
                    }
                    else {
                        fileName = str1;
                    }
                }
                MinioClient client = MinIoUtil.minioClient;
                InputStream object = client.getObject(BucketsConst.buckets_task_template, fileName);
                TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfoTwo(testEntrustedTaskRelVo.getTaskId(),null);
                XWPFDocument document = taskService.downloadEntrust(taskDetailInfo, object);
                response.reset();
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setContentType("application/x-msdownload");
                response.setCharacterEncoding("UTF-8");
                fileName = URLEncoder.encode(fileName, "UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" + testEntrustedTaskRelVo.getTaskCode()+".doc");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();//二进制OutputStream
                document.write(baos);//文档写入流
                ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());//OutputStream写入InputStream二进制流
                File file= new File("D:\\doc\\worlds\\"+testEntrustedTaskRelVo.getTaskCode()+".doc");
                copyInputStreamToFile(inputStream,file);
                inputStream.close();
            }
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
        }
    }
    /**
     * 流输出到文件的方法
     */
    private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }
}
