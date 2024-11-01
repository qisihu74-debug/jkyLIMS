package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.AsposeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2024-10-24 15:35
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/client/")
public class ClientController {
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;

    /**
     * 接收客户端上传的文件
     * @param file
     * @return
     */
    @PostMapping("receiveFile")
    public Result receiveFile(MultipartFile file, HttpServletResponse response){
        Boolean flag = true;
        if (file != null){
            //转换文档为excel，保存
            ServletOutputStream outputStream = null;
            try {
                outputStream = AsposeUtil.doc2excel(file, response, qiYueSuoEntity.getAutographPath());
                outputStream.flush();
            } catch (Exception e) {
                flag = false;
               log.error("文件转换异常:{}",e.getMessage());
            }
            if (flag){


            }else {
                return ResultUtil.error("failed",null);
            }


            return ResultUtil.success("success",null);
        }else {
            return ResultUtil.error("failed",null);
        }
    }

}
