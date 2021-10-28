package com.jifen.manage.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jifen.manage.demo.filter.PassToken;
import com.jifen.manage.demo.result.Result;
import com.jifen.manage.demo.result.ResultUtil;
import com.jifen.manage.demo.util.FastDFSClient;
import com.jifen.manage.demo.util.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.controller
 * @desc
 * @date 2021/10/25 14:16
 * @Copyright © 河南交科院
 */

@RestController
@Slf4j
@RequestMapping("/admin/")
public class IntegralController {

    @GetMapping("test2")
    @PassToken
    public Result test() throws Exception{
        /*InputStream in = new FileInputStream("D:\\Users\\Administrator\\Desktop\\work\\郭家林工作周总结.docx");
        byte[] data = FastDFSClient.toByteArray(in);
        String url = FastDFSClient.upload_file(data, "测试", in.available());
        System.out.println("文件上传成功，地址:"+url);*/

        String url = "group1/M00/00/00/wKgCI2F5_0aAQlQnAAAxswjI0104808928";
        byte[] bytes = FastDFSClient.download_file("group1","M00/00/00/wKgCI2F5_0aAQlQnAAAxswjI0104808928");
        System.out.println("====="+ JSON.toJSONString(bytes));
        return null;
    }

    @GetMapping("test3")
    @PassToken
    public Result test3() throws Exception{
        InputStream in = new FileInputStream("D:\\Users\\Administrator\\Desktop\\work\\郭家林工作周总结.docx");

        String upload = MinIoUtil.upload("20211028ceshi002", "文档1", in, ".docx");
        System.out.println("文件上传成功，地址:"+upload);
        //地址:http://192.168.2.35:9000/20211028ceshi002/%E6%96%87%E6%A1%A31?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20211028%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=
        // 20211028T072838Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=6eca71ca4089f022b4217e407e997d7a068d8c418fc9637e73a9021519cd4fb7
        return null;
    }

   @GetMapping("test4")
   @PassToken
   public Result test4(HttpServletResponse response) throws Exception{
       String fileUrl = MinIoUtil.getFileUrl("20211028ceshi002", "文档1");
       MinIoUtil.download("20211028ceshi002", "文档1", response);
       return ResultUtil.success(response);
   }

}
