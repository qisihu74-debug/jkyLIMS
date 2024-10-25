package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public Result receiveFile(MultipartFile file){
        if (file != null){
            return ResultUtil.success("文件接收成功");
        }else {
            return ResultUtil.error("文件为空请上传");
        }
    }

}
