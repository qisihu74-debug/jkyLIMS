package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.Const;
//import com.lims.manage.erp.util.FastDFSClient;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.VideoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.controller
 * @desc
 * @date 2021/10/25 14:16
 * @Copyright © 河南交科院
 */

@RestController
@Slf4j
@RequestMapping("/admin/")
public class IntegralController {

    @GetMapping("test2")
    public Result test() throws Exception{
        /*InputStream in = new FileInputStream("D:\\Users\\Administrator\\Desktop\\work\\郭家林工作周总结.docx");
        byte[] data = FastDFSClient.toByteArray(in);
        String url = FastDFSClient.upload_file(data, "测试", in.available());
        System.out.println("文件上传成功，地址:"+url);*/

        String url = "group1/M00/00/00/wKgCI2F5_0aAQlQnAAAxswjI0104808928";
        //byte[] bytes = FastDFSClient.download_file("group1","M00/00/00/wKgCI2F5_0aAQlQnAAAxswjI0104808928");
        //System.out.println("====="+ JSON.toJSONString(bytes));
        return null;
    }

    @GetMapping("test3")
    public Result test3() throws Exception{
        InputStream in = new FileInputStream("D:\\Users\\Administrator\\视频1.mp4");
        File file = new File("D:\\Users\\Administrator\\视频1.mp4");
        Map<String, Object> screenshot = VideoUtil.getScreenshot(file);
        log.info("视频封面为:{}");
        String upload = MinIoUtil.upload("20211028ceshi002", "视频1.mp4", in, Const.contentType);
        System.out.println("文件上传成功，地址:"+upload);
        //地址:http://192.168.2.35:9000/20211028ceshi002/%E6%96%87%E6%A1%A31?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20211028%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=
        // 20211028T072838Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=6eca71ca4089f022b4217e407e997d7a068d8c418fc9637e73a9021519cd4fb7
        return null;
    }

   @GetMapping("test4")
   public Result test4(HttpServletResponse response) throws Exception{
       String fileUrl = MinIoUtil.getUrl("20211028ceshi002", "视频.mp4");
       MinIoUtil.download("20211028ceshi002", "视频.mp4", response);
       return ResultUtil.success(response);
   }

    @GetMapping("test5")
    public Result test5() throws Exception{
        MinIoUtil.deleteFile("20211028ceshi001","文档1");
        return ResultUtil.success("删除成功");
    }
}
