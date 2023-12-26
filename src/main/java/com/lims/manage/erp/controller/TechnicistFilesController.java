package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.aspose.words.CellCollection;
import com.aspose.words.Document;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.PreferredWidth;
import com.aspose.words.Row;
import com.aspose.words.Run;
import com.aspose.words.SaveFormat;
import com.aspose.words.Table;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.entity.ResumeEntity;
import com.lims.manage.erp.entity.TechnicistFiles;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TechnicistFilesService;
import com.lims.manage.erp.util.CombineFilesToZip;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;

    /**
     * 受控文件类型
     *
     * @param type
     */
    @GetMapping("downloadResumeTemp")
    public Result downloadResumeTemp(String type) {
        if (StringUtils.isEmpty(type)) {
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
     *
     * @param technicistId
     * @param file
     * @return
     */
    @PostMapping("uploadResume")
    public Result uploadResume(@RequestParam("technicistId") Integer technicistId, @RequestParam("file") MultipartFile file) {
        if (technicistId == null || file == null || file.isEmpty()) {
            return ResultUtil.error("缺少参数");
        }
        String filename = file.getOriginalFilename();
        String[] split = filename.split("\\.");
        if (!"doc,docx,pdf".contains(split[split.length - 1])) {
            return ResultUtil.error("文件类型不正确，请上传doc,docx或者pdf文件");
        }
        Boolean flag = technicistFilesService.uploadResume(technicistId, file);
        if (flag) {
            return ResultUtil.success("上传或更新人员履历表成功");
        } else {
            return ResultUtil.error("上传或更新人员履历表失败");
        }
    }

    /**
     * 人员履历表预览
     *
     * @param technicistId
     */
    @GetMapping("previewResume")
    public void previewResume(Integer technicistId, HttpServletResponse response) {
        if (technicistId == null) {
            return;
        }
        LambdaQueryWrapper<TechnicistFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TechnicistFiles::getTechnicistId, technicistId);
        queryWrapper.eq(TechnicistFiles::getType, 1);
        queryWrapper.select(TechnicistFiles::getFileUrl);
        TechnicistFiles one = technicistFilesService.getOne(queryWrapper);
        if (one == null || StringUtils.isEmpty(one.getFileUrl())) {
            return;
        }
        String[] strings = one.getFileUrl().split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        String[] split = fileName.split("\\.");
        String rex = split[split.length - 1];
        try {
            InputStream inputStream = MinIoUtil.getFileStream(bluckName, fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            if ("doc,docx".contains(rex)) {
                Document doc = new Document(inputStream);
                doc.save(outputStream, SaveFormat.PDF);
            } else {
                IOUtils.copy(inputStream, outputStream);// copy流数据,i为字节数
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            log.info("导出失败：", e.getMessage());
        }
    }

    /**
     * 新增技术人员档案资料
     *
     * @param jsonParam
     * @param file
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestParam("jsonParam") String jsonParam, MultipartFile file) {
        if (StringUtils.isEmpty(jsonParam) || file == null || file.isEmpty()) {
            return ResultUtil.error("缺少参数");
        }
        String filename = file.getOriginalFilename();
        String[] split = filename.split("\\.");
        if (!"jpg,png,jpeg,pdf".contains(split[split.length - 1])) {
            return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
        }
        TechnicistFiles technicistFiles = JSON.parseObject(jsonParam, TechnicistFiles.class);
        String upload = MinIoUtil.upload(BucketsConst.technicist_files, file, file.getOriginalFilename());
        if (!StringUtils.isEmpty(upload)) {
            String uploadUrl = upload.substring(0, upload.indexOf("?"));
            technicistFiles.setFileUrl(uploadUrl);
        }
        boolean save = technicistFilesService.save(technicistFiles);
        if (save) {
            return ResultUtil.success("添加成功");
        } else {
            return ResultUtil.error("添加失败");
        }
    }

    /**
     * 编辑材料
     *
     * @param jsonParam
     * @param file
     * @return
     */
    @RequestMapping("edit")
    public Result edit(@RequestParam("jsonParam") String jsonParam, MultipartFile file) {
        if (StringUtils.isEmpty(jsonParam)) {
            return ResultUtil.error("缺少参数");
        }
        if (file != null) {
            String filename = file.getOriginalFilename();
            String[] split = filename.split("\\.");
            if (!"jpg,png,jpeg,pdf".contains(split[split.length - 1])) {
                return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
            }
        }
        TechnicistFiles technicistFiles = JSON.parseObject(jsonParam, TechnicistFiles.class);
        TechnicistFiles selectOne = technicistFilesService.getById(technicistFiles.getId());
        //如果文件发生变更删除旧文件，更新新文件
        if (file != null) {
            String fileUrl = selectOne.getFileUrl();
            if (!StringUtils.isEmpty(fileUrl)) {
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
        boolean save = technicistFilesService.updateById(selectOne);
        if (save) {
            return ResultUtil.success("更新成功");
        } else {
            return ResultUtil.error("更新失败");
        }
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @GetMapping("delete")
    public Result delete(Integer id) {
        if (id == null) {
            return ResultUtil.error("缺少参数");
        }
        //删除文件
        TechnicistFiles byId = technicistFilesService.getById(id);
        if (!StringUtils.isEmpty(byId.getFileUrl())) {
            technicistFilesService.delFileByUrl(byId.getFileUrl());
        }
        boolean b = technicistFilesService.removeById(id);
        if (b) {
            return ResultUtil.success("删除成功");
        } else {
            return ResultUtil.error("删除失败");
        }
    }

    /**
     * 查询技术人员档案材料列表信息
     *
     * @param technicistId
     * @param type
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("list")
    public Result list(Integer technicistId, Integer type, Integer pageNum, Integer pageSize) {
        if (technicistId == null || type == null || pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少参数");
        }
        LambdaQueryWrapper<TechnicistFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TechnicistFiles::getTechnicistId, technicistId);
        queryWrapper.eq(TechnicistFiles::getType, type);
        PageHelper.startPage(pageNum, pageSize);
        List<TechnicistFiles> list = technicistFilesService.list(queryWrapper);
        PageInfo<TechnicistFiles> pageInfo = new PageInfo<>(list);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 资料预览
     *
     * @param url
     * @param response
     */
    @GetMapping("downloadFile")
    public void downloadFile(String url, HttpServletResponse response) {
        String[] strings = url.split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        InputStream fileStream = MinIoUtil.getFileStream(bluckName, fileName);
        try {
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(fileStream, outputStream);
            fileStream.close();
            outputStream.close();
        } catch (Exception e) {
            log.error("预览下载文件失败:{}", e);
        }
    }

    /**
     * 技术人员档案导出
     * @param technicistId
     * @param（导出全部目录）档案文件类型1人员履历材料，2证件类材料，3培训类材料， 4业绩类材料，5奖惩类材料，6其它材料
     */
    @GetMapping("exportFiles")
    public void exportFiles(Integer technicistId, HttpServletResponse response) {
        if (technicistId == null) {
            return;
        }
        String path = qiYueSuoEntity.getAutographPath();
        //目录数组
        String[] dirs = {path + GenID.getID() + "-人员履历表",path + GenID.getID() + "-证件材料",path + GenID.getID() + "-培训类材料",
                path + GenID.getID() + "-业绩类材料",path + GenID.getID() + "-奖惩材料",path + GenID.getID() + "-其它材料"};
        List<String> dirList = Lists.newArrayList();
        //文件数组
        List<File>[] fileArray = new List[]{Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList(),
                Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList()};
        LambdaQueryWrapper<TechnicistFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TechnicistFiles::getTechnicistId, technicistId);
        List<TechnicistFiles> list = technicistFilesService.list(queryWrapper);
        List<TechnicistFiles>[] datas = new List[]{Lists.newArrayList(),Lists.newArrayList(),Lists.newArrayList(),
                Lists.newArrayList(),Lists.newArrayList()};
        for (TechnicistFiles technicistFiles : list) {
            if (technicistFiles.getType() == 2) {
                datas[0].add(technicistFiles);
            }
            if (technicistFiles.getType() == 3) {
                datas[1].add(technicistFiles);
            }
            if (technicistFiles.getType() == 4) {
                datas[2].add(technicistFiles);
            }
            if (technicistFiles.getType() == 5) {
                datas[3].add(technicistFiles);
            }
            if (technicistFiles.getType() == 6) {
                datas[4].add(technicistFiles);
            }
        }
        //填充数据
        Boolean[] flags = {false,false,false,false,false};
        Integer[] indexs = {1,1,1,1,1};
        Document[] docs = new Document[5];
        try {
            docs[0] = new Document(MinIoUtil.getFileStream(BucketsConst.controlled_documents, "证件类材料目录.doc"));
            docs[1] = new Document(MinIoUtil.getFileStream(BucketsConst.controlled_documents, "培训类材料目录.doc"));
            docs[2] = new Document(MinIoUtil.getFileStream(BucketsConst.controlled_documents, "业绩类材料目录.doc"));
            docs[3] = new Document(MinIoUtil.getFileStream(BucketsConst.controlled_documents, "奖惩类材料目录.doc"));
            docs[4] = new Document(MinIoUtil.getFileStream(BucketsConst.controlled_documents, "其他材料目录.doc"));
        } catch (Exception e) {
            log.error("获取人事档案相关模板失败:{}", e);
        }
        for (TechnicistFiles technicistFiles : list) {
            //依次处理人员档案材料
            if (technicistFiles.getType() == 1) {
                try {
                    File file = FileAndFolderUtil.getFile(technicistFiles.getFileUrl());
                    fileArray[0].add(file);
                    CombineFilesToZip.addFileToDir(fileArray[0], dirs[0]);
                    dirList.add(dirs[0]);
                } catch (Exception e) {
                    log.error("人员履历表添加到fileList失败:{}", e);
                }
            } else {
                //根据不同受控类型获取模板文件
                if (technicistFiles.getType() == 2) {
                    if (CollectionUtil.isNotEmpty(datas[0])) {
                        indexs[0] = handData(datas[0],docs[0], flags[0], indexs[0], technicistFiles, fileArray[1]);
                    }
                }
                if (technicistFiles.getType() == 3) {
                    if (CollectionUtil.isNotEmpty(datas[1])) {
                        indexs[1] = handData(datas[1], docs[1], flags[1], indexs[1], technicistFiles, fileArray[2]);
                    }
                }
                if (technicistFiles.getType() == 4) {
                    if (CollectionUtil.isNotEmpty(datas[2])) {
                        indexs[2] = handData(datas[2], docs[2], flags[2], indexs[2], technicistFiles, fileArray[3]);
                    }
                }
                if (technicistFiles.getType() == 5) {
                    if (CollectionUtil.isNotEmpty(datas[3])) {
                        indexs[3] = handData(datas[3], docs[3], flags[3], indexs[3], technicistFiles, fileArray[4]);
                    }
                }
                if (technicistFiles.getType() == 6) {
                    if (CollectionUtil.isNotEmpty(datas[4])) {
                        indexs[4] = handData(datas[4], docs[4], flags[4], indexs[4], technicistFiles, fileArray[5]);
                    }
                }
            }
        }
        //目录文件
        for (int i=0;i<docs.length;i++){
            if (docs[i] != null ){
                handDirFile(docs[i], fileArray[i+1], dirs[i+1], path+GenID.getID()+".doc");
                dirList.add(dirs[i+1]);
            }
        }
        //生成压缩包
        String zipPath = path+"人员档案.zip";
        try {
            //将各个目录添加到人员档案目录中
            CombineFilesToZip.downloadZip(dirList, zipPath);
        } catch (Exception e) {
            log.error("技术人员档案导出失败:{}", e);
        } finally {
            //删除目录及文件
            for (String dir : dirList) {
                FileAndFolderUtil.deleteDirectory(dir);
            }
        }
        //响应数据
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        try {
            File file = new File(zipPath);
            FileInputStream inputStream = new FileInputStream(file);
            response.setHeader("Content-Disposition", "attachment;fileName=" + "技术人员档案.zip");
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream,outputStream);
            inputStream.close();
            outputStream.close();
        }catch (Exception e){
            log.error("下载技术人员档案失败:{}",e);
        }finally {
            FileAndFolderUtil.delete(zipPath);
        }
    }

    /**
     * 处理数据
     *
     * @param list
     * @param doc
     * @param flag
     * @param index
     * @param technicistFiles
     * @param fileList
     */
    public int handData(List<TechnicistFiles> list, Document doc, Boolean flag,
                        int index, TechnicistFiles technicistFiles, List<File> fileList) {
        Table table = (Table) doc.getChild(NodeType.TABLE, 0, true);
        int size = table.getRows().getCount();
        //如果附件的数量大于模板初始行数，自动扩容行数
        int addNum = list.size() - size + 1;
        if (list.size() >= size) {
            for (int j = 0; j < addNum; j++) {
                com.aspose.words.Row newRow = new Row(doc);
                for (int col = 0; col < table.getFirstRow().getCells().getCount(); col++) {
                    com.aspose.words.Cell cell = new com.aspose.words.Cell(doc);
                    try {
                        cell.getCellFormat().setPreferredWidth(PreferredWidth.fromPercent(100.0 / table.getFirstRow().getCells().getCount()));
                        cell.getCellFormat().setFitText(true);
                        cell.appendChild(new Paragraph(doc));
                        newRow.appendChild(cell);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    table.appendChild(newRow);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            flag = true;
        }
        //向模板中填充数据,如果扩容了需要对新的行进行编序号
        try {
            CellCollection cells = table.getRows().get(index).getCells();
            if (flag) {
                if (index > size - 1) {
                    //设置序号
                    if (cells.get(0) != null && cells.get(0).getFirstParagraph() != null) {
                        cells.get(0).getFirstParagraph().appendChild(new Run(doc, index + ""));
                    }
                }
            }
            cells.get(1).getFirstParagraph().appendChild(new Run(doc, technicistFiles.getContent()));
            cells.get(2).getFirstParagraph().appendChild(new Run(doc, technicistFiles.getOperator()));
            cells.get(3).getFirstParagraph().appendChild(new Run(doc, DateUtil.formatDate(technicistFiles.getOperateTime())));
            index++;
        } catch (Exception e) {
            log.error("技术人员档案下载数据填充失败:{}", e);
        }
        String fileUrl = technicistFiles.getFileUrl();
        try {
            File file = FileAndFolderUtil.getFile(fileUrl);
            fileList.add(file);
        } catch (Exception e) {
            log.error("目录类型：" + technicistFiles.getType() + "添加到fileList失败:{}", e);
        }
        return index;
    }

    /**
     * 处理文件添加到目录
     *
     * @param document
     * @param fileList
     * @param dir
     * @param filePath
     */
    public void handDirFile(Document document, List<File> fileList, String dir, String filePath) {
        try {
            OutputStream outputStream = new FileOutputStream(filePath);
            document.save(outputStream, SaveFormat.DOC);
            outputStream.close();
            fileList.add(new File(filePath));
            CombineFilesToZip.addFileToDir(fileList, dir);
            FileAndFolderUtil.delete(filePath);
        } catch (Exception e) {
            log.error("目录添加到文件列表失败:{}", e);
        }
    }
}
