package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lims.manage.erp.entity.DaTaskRecord;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DaTaskRecordService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    @Autowired
    private DaTaskRecordService daTaskRecordService;

    /**
     * 接收客户端上传的文件
     * @param file
     * @return
     */
    @PostMapping("receiveFile")
    public Result receiveFile(MultipartFile file){
        Boolean flag = true;
        if (file != null){
            //判断文件是否已存在
            String[] split = file.getOriginalFilename().split("\\.");
            String code = split[0];
            LambdaQueryWrapper<DaTaskRecord> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(DaTaskRecord::getTaskCode,code);
            DaTaskRecord one = daTaskRecordService.getOne(queryWrapper);
            if (one != null){
                return ResultUtil.error(500,"file already exist!");
            }
            //转换文档为xcel，保存
            String path = "";
            try {
                path = AsposeUtil.doc2excel(file, qiYueSuoEntity.getAutographPath());
            } catch (Exception e) {
                flag = false;
               log.error("文件转换异常:{}",e.getMessage());
            }
            if (flag){
                try {
                    //将ServletOutputStream转为MultipartFile
                    InputStream inputStream = new FileInputStream(path);
                    //处理名称
                    Path path1 = Paths.get(path);
                    String fileName = path1.getFileName().toString();
                    //将文件上传文件服务器，数据插入表
                    String upload = MinIoUtil.upload("device-file",fileName ,inputStream,null );
                    String[] fileUrls = upload.split("\\?");
                    String url = fileUrls[0];

                    DaTaskRecord daTaskRecord = new DaTaskRecord();
                    daTaskRecord.setTaskCode(code);
                    daTaskRecord.setUrl(url);
                    daTaskRecord.setTime(DateUtil.getCurrentTime());
                    daTaskRecordService.save(daTaskRecord);
                    //删除临时pdf文件
                    FileAndFolderUtil.delete(path);
                    System.out.println("临时文件删除成功");
                    return ResultUtil.success("file uploaded successfully ", null);
                }catch (Exception e){
                    log.error("文件上传失败:{}",e.getMessage());
                    //删除临时pdf文件
                    FileAndFolderUtil.delete(path);
                    System.out.println("临时文件删除成功");
                    return ResultUtil.error(500,"upload failed");
                }
            }else {
                return ResultUtil.error(500,"file upload success but lims is exception");
            }
        }else {
            return ResultUtil.error(500,"file is empty");
        }
    }

}
