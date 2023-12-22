package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.ResumeEntity;
import com.lims.manage.erp.entity.TechnicistFiles;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TechnicistFilesService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.CombineFilesToZip;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2023-12-19 10:19
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/technicistFile/")
public class TechnicistFilesController {
    @Autowired
    private TechnicistFilesService technicistFilesService;

    /**
     * 受控文件类型
     * @param type
     */
    @GetMapping("downloadResumeTemp")
    public Result downloadResumeTemp(String type){
        if (StringUtils.isEmpty(type)){
            return ResultUtil.error("缺少参数");
        }
        //受控文件查询 TODO
        List<ResumeEntity> list = Lists.newArrayList();
        ResumeEntity resumeEntity = new ResumeEntity();
        resumeEntity.setCode("05-JL/GL-050");
        resumeEntity.setName("人员履历表");
        resumeEntity.setFileUrl("https://minio.lims.design/controlled-documents/人员履历表.doc");
        list.add(resumeEntity);
        return ResultUtil.success(list);
    }

    /**
     * 上传或更新人员履历表，MultipartFile 文件类型（doc,docx,pdf三种文件类型）
     * @param technicistId
     * @param file
     * @return
     */
    @PostMapping("uploadResume")
    public Result uploadResume(@RequestParam("technicistId") Integer technicistId, @RequestParam("file") MultipartFile file){
        if (technicistId == null || file == null || file.isEmpty()){
            return ResultUtil.error("缺少参数");
        }
        String s = file.getOriginalFilename().split("\\.")[1];
        if (!"doc,docx,pdf".contains(s)){
            return ResultUtil.error("文件类型不正确，请上传doc,docx或者pdf文件");
        }
        Boolean flag = technicistFilesService.uploadResume(technicistId,file);
        if (flag){
            return ResultUtil.success("上传或更新人员履历表成功");
        }else {
            return ResultUtil.error("上传或更新人员履历表失败");
        }
    }

    /**
     * 人员履历表预览
     * @param technicistId
     */
    @GetMapping("previewResume")
    public Result previewResume(Integer technicistId, HttpServletResponse response){
        if (technicistId == null){
            return ResultUtil.error("缺少参数");
        }
        LambdaQueryWrapper<TechnicistFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TechnicistFiles::getTechnicistId,technicistId);
        queryWrapper.eq(TechnicistFiles::getType,1);
        queryWrapper.select(TechnicistFiles::getFileUrl);
        TechnicistFiles one = technicistFilesService.getOne(queryWrapper);
        String[] strings = one.getFileUrl().split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        InputStream fileStream = MinIoUtil.getFileStream(bluckName, fileName);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), StandardCharsets.UTF_8));
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(fileStream,outputStream);
            fileStream.close();
            outputStream.close();
        }catch (Exception e){
            log.error("预览人员履历表文件失败:{}",e);
        }
        return ResultUtil.success();
    }

    /**
     * 新增技术人员档案资料
     * @param jsonParam
     * @param file
     * @return
     */
    @RequestMapping("add")
    @ResponseBody
    public Result add(@RequestParam("jsonParam") String jsonParam, MultipartFile file){
        if (StringUtils.isEmpty(jsonParam) || file == null || file.isEmpty()){
            return ResultUtil.error("缺少参数");
        }
        String s = file.getOriginalFilename().split("\\.")[1];
        if (!"jpg,png,jpeg,pdf".contains(s)){
            return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
        }
        TechnicistFiles technicistFiles = JSON.parseObject(jsonParam, TechnicistFiles.class);
        String upload = MinIoUtil.upload(BucketsConst.technicist_files, file, file.getOriginalFilename());
        if (!StringUtils.isEmpty(upload)) {
            String uploadUrl = upload.substring(0, upload.indexOf("?"));
            technicistFiles.setFileUrl(uploadUrl);
        }
        boolean save = technicistFilesService.save(technicistFiles);
        if (save){
            return ResultUtil.success("添加成功");
        }else {
            return ResultUtil.error("添加失败");
        }
    }

    /**
     * 编辑材料
     * @param jsonParam
     * @param file
     * @return
     */
    @RequestMapping("edit")
    public Result edit(@RequestParam("jsonParam") String jsonParam, @RequestParam("file") MultipartFile file){
        if (StringUtils.isEmpty(jsonParam)){
            return ResultUtil.error("缺少参数");
        }
        String s = file.getOriginalFilename().split("\\.")[1];
        if (!"jpg,png,jpeg,pdf".contains(s)){
            return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
        }
        TechnicistFiles technicistFiles = JSON.parseObject(jsonParam, TechnicistFiles.class);
        TechnicistFiles selectOne = technicistFilesService.getById(technicistFiles.getId());
        //如果文件发生变更删除旧文件，更新新文件
        if (file != null){
            String fileUrl = selectOne.getFileUrl();
            if (!StringUtils.isEmpty(fileUrl)){
                technicistFilesService.delFileByUrl(fileUrl);
            }
            String upload = MinIoUtil.upload(BucketsConst.technicist_files, file, file.getOriginalFilename());
            if (!StringUtils.isEmpty(upload)) {
                String uploadUrl = upload.substring(0, upload.indexOf("?"));
                selectOne.setFileUrl(uploadUrl);
            }
        }
        selectOne.setUpdateTime(new Date(System.currentTimeMillis()));
        selectOne.setContent(technicistFiles.getContent());
        boolean save = technicistFilesService.updateById(technicistFiles);
        if (save){
            return ResultUtil.success("更新成功");
        }else {
            return ResultUtil.error("更新失败");
        }
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("delete")
    public Result delete(Integer id){
        if (id == null){
            return ResultUtil.error("缺少参数");
        }
        //删除文件
        TechnicistFiles byId = technicistFilesService.getById(id);
        if (!StringUtils.isEmpty(byId.getFileUrl())){
            technicistFilesService.delFileByUrl(byId.getFileUrl());
        }
        boolean b = technicistFilesService.removeById(id);
        if (b){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }

    /**
     * 查询技术人员档案材料列表信息
     * @param technicistId
     * @param type
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("list")
    public Result list(Integer technicistId,Integer type,Integer pageNum,Integer pageSize){
        if (technicistId == null || type == null || pageNum == null || pageSize == null){
            return ResultUtil.error("缺少参数");
        }
        LambdaQueryWrapper<TechnicistFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TechnicistFiles::getTechnicistId,technicistId);
        queryWrapper.eq(TechnicistFiles::getType,type);
        PageHelper.startPage(pageNum,pageSize);
        List<TechnicistFiles> list = technicistFilesService.list(queryWrapper);
        PageInfo<TechnicistFiles> pageInfo = new PageInfo<>(list);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 资料预览
     * @param url
     * @param response
     */
    @GetMapping("downloadFile")
    public void downloadFile(String url,HttpServletResponse response){
        String[] strings = url.split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        InputStream fileStream = MinIoUtil.getFileStream(bluckName, fileName);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), StandardCharsets.UTF_8));
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(fileStream,outputStream);
            fileStream.close();
            outputStream.close();
        }catch (Exception e){
            log.error("预览下载文件失败:{}",e);
        }
    }

    /**
     * 技术人员档案导出
     * @param technicistId
     * @param type （如果不传类型默认导出全部目录）档案文件类型1人员履历材料，2证件类材料，3培训类材料，
     *             4业绩类材料，5奖惩类材料，6其它材料
     * @param contains 是否包含附件0不包含，1包含
     */
    @GetMapping("exportFiles")
    public void exportFiles(Integer technicistId,Integer type, Integer contains){
        if (technicistId == null){
            return;
        }
        //type如果为空导出所有类型材料表
        List<File> fileList = Lists.newArrayList();
        List<File> fileList2 = Lists.newArrayList();
        List<File> fileList3 = Lists.newArrayList();
        List<File> fileList4 = Lists.newArrayList();
        List<File> fileList5 = Lists.newArrayList();
        List<File> fileList6 = Lists.newArrayList();
        File file2 = null;
        File file3 = null;
        File file4 = null;
        File file5 = null;
        File file6 = null;
        if (type == null){
            file2 = new File("https://minio.lims.design/controlled-documents/证件类材料目录.doc");
            file3 = new File("https://minio.lims.design/controlled-documents/培训类材料目录.doc");
            file4 = new File("https://minio.lims.design/controlled-documents/业绩类材料目录.doc");
            file5 = new File("https://minio.lims.design/controlled-documents/奖惩类材料目录.doc");
            file6 = new File("https://minio.lims.design/controlled-documents/其他材料目录.doc");
        }else {
            //根据受控文件类型获取对应文件模板 TODO
            file2 = new File("https://minio.lims.design/controlled-documents/证件类材料目录.doc");
        }
        LambdaQueryWrapper<TechnicistFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TechnicistFiles::getTechnicistId,technicistId);
        if (type != null){
            queryWrapper.eq(TechnicistFiles::getType,type);
        }
        List<TechnicistFiles> list = technicistFilesService.list(queryWrapper);

        List<TechnicistFiles> list2 = Lists.newArrayList();
        List<TechnicistFiles> list3 = Lists.newArrayList();
        List<TechnicistFiles> list4 = Lists.newArrayList();
        List<TechnicistFiles> list5 = Lists.newArrayList();
        List<TechnicistFiles> list6 = Lists.newArrayList();
        if (type != null){
            for (TechnicistFiles technicistFiles :list){
                switch (type) {
                    case 2:
                        list2.add(technicistFiles);
                        break;
                    case 3:
                        list3.add(technicistFiles);
                        break;
                    case 4:
                        list4.add(technicistFiles);
                        break;
                    case 5:
                        list5.add(technicistFiles);
                        break;
                    case 6:
                        list6.add(technicistFiles);
                        break;
                    default:
                        break;
                }
            }
        }else {
            for (TechnicistFiles technicistFiles :list){
               if (technicistFiles.getType() ==2){
                   list2.add(technicistFiles);
               }
               if (technicistFiles.getType() ==3){
                   list3.add(technicistFiles);
               }
               if (technicistFiles.getType() ==4){
                   list4.add(technicistFiles);
               }
               if (technicistFiles.getType() ==5){
                   list5.add(technicistFiles);
               }
               if (technicistFiles.getType() ==6){
                   list6.add(technicistFiles);
                }
            }
        }
        //填充数据
        boolean flag2 =false;
        boolean flag3 =false;
        boolean flag4 =false;
        boolean flag5 =false;
        boolean flag6 =false;
        int index2 = 1;
        int index3 = 1;
        int index4 = 1;
        int index5 = 1;
        int index6 = 1;
        InputStream inputStream2 = null;
        XWPFDocument doc2 = null;
        InputStream inputStream3 = null;
        XWPFDocument doc3 = null;
        InputStream inputStream4 = null;
        XWPFDocument doc4 = null;
        InputStream inputStream5 = null;
        XWPFDocument doc5 = null;
        InputStream inputStream6 = null;
        XWPFDocument doc6 = null;
        try {
            if (file2 != null){
                inputStream2 = new FileInputStream(file2);
                doc2 = new XWPFDocument(inputStream2);
            }
            if (file3 != null){
                inputStream3 = new FileInputStream(file3);
                doc3 = new XWPFDocument(inputStream3);
            }
            if (file4 != null){
                inputStream4 = new FileInputStream(file4);
                doc4 = new XWPFDocument(inputStream4);
            }
            if (file5 != null){
                inputStream5 = new FileInputStream(file5);
                doc5 = new XWPFDocument(inputStream5);
            }
            if (file6 != null){
                inputStream6 = new FileInputStream(file6);
                doc6 = new XWPFDocument(inputStream6);
            }
        }catch (Exception e){
            log.error("加载人员档案材料模板异常:{}",e);
        }
        for (TechnicistFiles technicistFiles :list){
            //依次处理人员档案材料
            if (technicistFiles.getType()==1){
                File file = new File(technicistFiles.getFileUrl());
                fileList.add(file);
            }else {
                //根据不同受控类型获取模板文件
                if (type != null){
                    switch (type) {
                        case 2:
                            handData(list2,doc2,flag2,index2,technicistFiles,contains,fileList2);
                            break;
                        case 3:
                            handData(list3,doc3,flag3,index3,technicistFiles,contains,fileList3);
                            break;
                        case 4:
                            handData(list4,doc4,flag4,index4,technicistFiles,contains,fileList4);
                            break;
                        case 5:
                            handData(list5,doc5,flag5,index5,technicistFiles,contains,fileList5);
                            break;
                        case 6:
                            handData(list6,doc6,flag6,index6,technicistFiles,contains,fileList6);
                            break;
                        default:
                            break;
                    }
                }else {
                    if (CollectionUtil.isNotEmpty(list2)){
                        handData(list2,doc2,flag2,index2,technicistFiles,contains,fileList2);
                    }
                    if (CollectionUtil.isNotEmpty(list3)){
                        handData(list3,doc3,flag3,index3,technicistFiles,contains,fileList3);
                    }
                    if (CollectionUtil.isNotEmpty(list4)){
                        handData(list4,doc4,flag4,index4,technicistFiles,contains,fileList4);
                    }
                    if (CollectionUtil.isNotEmpty(list5)){
                        handData(list5,doc5,flag5,index5,technicistFiles,contains,fileList5);
                    }
                    if (CollectionUtil.isNotEmpty(list6)){
                        handData(list6,doc6,flag6,index6,technicistFiles,contains,fileList6);
                    }
                }
            }
        }
        //目录文件
        if (doc2 != null){
            try {
                OutputStream outputStream = new FileOutputStream("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\2.doc");
                doc2.write(outputStream);
                fileList.add(new File("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\2.doc"));
                FileAndFolderUtil.delete("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\2.doc");
            }catch (Exception e){
                log.error("目录添加到文件列表失败:{}",e);
            }
            //附件
            if (fileList2.size()>0){
                fileList.addAll(fileList2);
            }
        }
        if (doc3 != null){
            try {
                OutputStream outputStream = new FileOutputStream("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\3.doc");
                doc3.write(outputStream);
                fileList.add(new File("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\3.doc"));
                FileAndFolderUtil.delete("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\3.doc");
            }catch (Exception e){
                log.error("目录添加到文件列表失败:{}",e);
            }
            if (fileList3.size()>0){
                fileList.addAll(fileList3);
            }
        }
        if (doc4 != null){
            try {
                OutputStream outputStream = new FileOutputStream("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\4.doc");
                doc4.write(outputStream);
                fileList.add(new File("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\4.doc"));
                FileAndFolderUtil.delete("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\4.doc");
            }catch (Exception e){
                log.error("目录添加到文件列表失败:{}",e);
            }
            if (fileList4.size()>0){
                fileList.addAll(fileList4);
            }
        }
        if (doc5 != null){
            try {
                OutputStream outputStream = new FileOutputStream("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\5.doc");
                doc5.write(outputStream);
                fileList.add(new File("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\5.doc"));
                FileAndFolderUtil.delete("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\5.doc");
            }catch (Exception e){
                log.error("目录添加到文件列表失败:{}",e);
            }
            if (fileList5.size()>0){
                fileList.addAll(fileList5);
            }
        }
        if (doc6 != null){
            try {
                OutputStream outputStream = new FileOutputStream("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\6.doc");
                doc6.write(outputStream);
                fileList.add(new File("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\6.doc"));
                FileAndFolderUtil.delete("D:\\Users\\Administrator\\Desktop\\人员档案\\AA\\6.doc");
            }catch (Exception e){
                log.error("目录添加到文件列表失败:{}",e);
            }
            if (fileList6.size()>0){
                fileList.addAll(fileList6);
            }
        }
        //下载压缩包
        try {
            CombineFilesToZip.handerFiles(fileList,"人员档案.zip");
        }catch (Exception e){
            log.error("技术人员档案导出失败:{}",e);
        }
    }

    /**
     * 处理数据
     * @param list2
     * @param doc2
     */
    public void handData(List<TechnicistFiles> list2,XWPFDocument doc2,Boolean flag2,
                         int index2,TechnicistFiles technicistFiles,Integer contains,List<File> fileList2){
        try {
            List<XWPFTable> tables = doc2.getTables();
            XWPFTable table = tables.get(0);
            int size = table.getRows().size();
            //如果附件的数量大于模板初始行数，自动扩容行数
            if (list2.size()>=size){
                AsposeUtil.addRows(table,size,list2.size()-size+1);
                flag2 = true;
            }
            //向模板中填充数据
            List<XWPFTableRow> rows = table.getRows();
            if (flag2){
                if (index2>size-1){
                    //设置序号
                    rows.get(index2).getTableCells().get(0).setText(index2+"");
                }
            }
            rows.get(index2).getTableCells().get(1).setText(technicistFiles.getContent());
            rows.get(index2).getTableCells().get(2).setText(technicistFiles.getOperator());
            rows.get(index2).getTableCells().get(3).setText(DateUtil.formatDate(technicistFiles.getOperateTime()));
            index2 ++;
        }catch (Exception e){
            log.error("向模板中填充数据失败:{}",e);
        }
        if (contains == 1){
            String fileUrl = technicistFiles.getFileUrl();
            File file = new File(fileUrl);
            fileList2.add(file);
        }
    }
}
