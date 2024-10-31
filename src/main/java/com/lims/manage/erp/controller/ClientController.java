package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2024-10-24 15:35
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("/client/")
public class ClientController {

    /**
     * 接收客户端上传的文件
     * @param file
     * @return
     */
    @PostMapping("receiveFile")
    public Result receiveFile(MultipartFile file, HttpServletRequest request){

        try {
            request.setCharacterEncoding("UTF-8");
            if (file != null){
                String UPLOAD_DIR = "D:\\Users\\Administrator\\My Document\\WeChat Files\\wxid_ry85h8s4iqzz21\\FileStorage\\File\\2024-10\\";
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String fileName = file.getOriginalFilename();
                System.out.println("转码前:"+fileName);
                String name = "";
                try {
                    name = new String(fileName.getBytes(),"UTF-8");
                    System.out.println("转码后:"+name);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                java.nio.file.Path filePath = Paths.get(UPLOAD_DIR, name);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                return ResultUtil.success("文件接收成功");
            }else {
                return ResultUtil.error("文件为空请上传");
            }
        }catch (Exception e){
            System.out.println("解析失败:{}"+e.getMessage());

        }
        String des = "";
        try {
            des = new String("上传成功".getBytes(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ResultUtil.success(des);
    }

}
