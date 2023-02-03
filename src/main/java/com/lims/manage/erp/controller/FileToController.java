package com.lims.manage.erp.controller;

import com.lims.manage.erp.util.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author: DLC
 * @Date: 2023/1/16 15:35
 */
@Slf4j
@RestController
@RequestMapping("/web/file_output_stream/")
public class FileToController {

    // 通过url 输出桶文件
    @GetMapping("getEntrustFileUrls")
    public void fileToDataStream(HttpServletResponse response, String url) throws Exception {
        String[] strings = url.split("\\/");
        String bucketName = strings[strings.length-2];
         String fileName = strings[strings.length-1];
        MinIoUtil.download(bucketName,fileName,response);
    }


}
