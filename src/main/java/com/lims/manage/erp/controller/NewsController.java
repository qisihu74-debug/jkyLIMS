package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.NewsBean;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.NewsService;
import com.lims.manage.erp.util.MinIoUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lims.manage.erp.vo.NewsBeanVo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2023-04-28 10:04
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("/news/")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * 新闻发布保存
     * @param json
     * @param files
     * @return
     */
    @PostMapping("saveNews")
    public Result saveNews(@RequestParam("json") String json, MultipartFile[] files){
        if (StringUtils.isEmpty(json) || files == null){
            return ResultUtil.error("缺少参数");
        }
        NewsBean newsBean = JSON.parseObject(json,NewsBean.class);
        Boolean flag = newsService.saveNews(newsBean,files);
        if (flag){
            return ResultUtil.success("发布新闻成功");
        }else {
            return ResultUtil.error("发布消息失败");
        }
    }

    /**
     * 新闻消息列表
     * @param search
     * @return
     */
    @GetMapping("list")
    public Result list(String search, Integer pageNum, Integer pageSize){
        if (pageNum == null || pageSize == null
        ){
            pageNum = 1;
            pageSize = 10;
        }
        Date date = new Date(System.currentTimeMillis());
        LambdaQueryWrapper<NewsBean> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(search),NewsBean::getContent,search);
        lambdaQueryWrapper.le(NewsBean::getPublishDate,date);
        PageHelper.startPage(pageNum,pageSize);
        List<NewsBean> list = newsService.list(lambdaQueryWrapper);
        PageInfo<NewsBean> pageInfo = new PageInfo(list);
        //附件列表返回
        for (NewsBean bean:pageInfo.getList()) {
            List<String> ll = Lists.newArrayList();
            String fileUrl = bean.getFileUrl();
            if (StringUtils.isNotEmpty(fileUrl)){
                String[] split = fileUrl.split(",");
                for (String url:split) {
                    ll.add(url);
                }
            }
        }
        return ResultUtil.success(pageInfo);
    }

    /**
     * 删除
//    /**
//     * 新闻发布保存
//     * @param json
//     * @param files
//     * @return
//     */
//    @PostMapping("saveNews")
//    public Result saveNews(@RequestParam("json") String json, MultipartFile[] files){
//        if (StringUtils.isEmpty(json) || files == null){
//            return ResultUtil.error("缺少参数");
//        }
//        NewsBean newsBean = JSON.parseObject(json,NewsBean.class);
//        Boolean flag = newsService.saveNews(newsBean,files);
//        if (flag){
//            return ResultUtil.success("发布新闻成功");
//        }else {
//            return ResultUtil.error("发布消息失败");
//        }
//    }
//
//    /**
//     * 新闻消息列表
//     * @param search
//     * @return
//     */
//    @GetMapping("list")
//    public Result list(String search, Integer pageNum, Integer pageSize){
//        if (pageNum == null || pageSize == null
//        ){
//            pageNum = 1;
//            pageSize = 10;
//        }
//        Date date = new Date(System.currentTimeMillis());
//        LambdaQueryWrapper<NewsBean> lambdaQueryWrapper = new LambdaQueryWrapper();
//        lambdaQueryWrapper.like(StringUtils.isNotEmpty(search),NewsBean::getContent,search);
//        lambdaQueryWrapper.le(NewsBean::getPublishDate,date);
//        PageHelper.startPage(pageNum,pageSize);
//        List<NewsBean> list = newsService.list(lambdaQueryWrapper);
//        PageInfo<NewsBean> pageInfo = new PageInfo(list);
//        //附件列表返回
//        for (NewsBean bean:pageInfo.getList()) {
//            List<String> ll = Lists.newArrayList();
//            String fileUrl = bean.getFileUrl();
//            if (StringUtils.isNotEmpty(fileUrl)){
//                String[] split = fileUrl.split(",");
//                for (String url:split) {
//                    ll.add(url);
//                }
//            }
//        }
//        return ResultUtil.success(pageInfo);
//    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @GetMapping("delete")
    public Result detete(Long id){
        if (id == null){
    public Result detete(Long id) {
        if (id == null) {
            return ResultUtil.error("缺少参数");
        }
        boolean b = newsService.removeById(id);
        if (b){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }

    /**
     * 下载附件
     * @param url
     * @return
     */
    @GetMapping("download")
    public void download(String url, HttpServletResponse response){
        if (StringUtils.isEmpty(url)){
            return ;
        }
        String[] strings = url.split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        InputStream fileStream = MinIoUtil.getFileStream(bluckName, fileName);
        response.reset();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String fileType =fileName.split("\\.")[1];
        //设置文件ContentType类型
        if("jpg,jepg,gif,png".contains(fileType)){//图片类型
            response.setContentType("image/"+fileType);
        }else if("pdf".contains(fileType)){//pdf类型
            response.setContentType("application/pdf");
        }else if ("xls,xlsx".contains(fileType)){
            response.setContentType("application/x-msdownload");
        }else {
            //自动判断下载文件类型
            response.setContentType("multipart/form-data");
        }
        response.setCharacterEncoding("UTF-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(fileStream,outputStream);
            fileStream.close();
            outputStream.close();
        }catch (Exception e){
            IOUtils.copy(fileStream, outputStream);
            fileStream.close();
            outputStream.close();
        } catch (Exception e) {
            System.out.println("下载新闻附件异常");
        }
    }

    /**
     * 新闻发布保存
     *
     * @param newsBeanVo
     * @return
     */
    @PostMapping("saveNews")
    public Result saveNews(@RequestBody NewsBeanVo newsBeanVo) {
        if (newsBeanVo == null) {
            return ResultUtil.error("缺少参数");
        }
        if (newsBeanVo.getContent() == null || newsBeanVo.getTitle() == null) {
            return ResultUtil.error("缺少参数");
        }
        Boolean flag = newsService.saveNews(newsBeanVo);
        if (flag) {
            return ResultUtil.success("发布新闻成功");
        } else {
            return ResultUtil.error("发布消息失败");
        }
    }

    /**
     * 新闻消息列表
     *
     * @param search
     * @return
     */
    @GetMapping("list")
    public Result list(String search, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null
        ) {
            pageNum = 1;
            pageSize = 10;
        }
        PageInfo<NewsBeanVo> list = newsService.getPageInfoList(search, pageNum, pageSize);
        return ResultUtil.success(list);
    }
}
