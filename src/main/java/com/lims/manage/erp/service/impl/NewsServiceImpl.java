package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.NewsBean;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.mapper.NewsDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.service.NewsService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2023-04-28 11:11
 * @Copyright © 河南交科院
 */
@Service
public class NewsServiceImpl  extends ServiceImpl<NewsDao, NewsBean> implements NewsService {
    @Autowired
    private SysUserDao userDao;
    @Autowired
    private DeptDao deptDao;
    @Autowired
    private NewsDao newsDao;

    @Override
    public Boolean saveNews(NewsBean newsBean, MultipartFile[] files) {
        //处理文件
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        int num = 0;
        for (MultipartFile multipartFile:files) {
            count++;
            String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_template, multipartFile, multipartFile.getOriginalFilename());
            String s = upload.split("\\?")[0];
            stringBuilder.append(s);
            if (count < files.length){
                stringBuilder.append(",");
            }
        }
        //设置发布部门
        StringBuilder builder = new StringBuilder();
        String dept = userDao.getDeptByUserId(ShiroUtils.getUserInfo().getUserId());
        if (StringUtils.isNotEmpty(dept)){
            String replace = dept.replace("[", "").replace("]", "");
            String[] split = replace.split(",");
            for (String s:split) {
                if (StringUtils.isNotEmpty(s)){
                    String deptName = deptDao.getNameById(Long.parseLong(s));
                    num++;
                    builder.append(deptName);
                    builder.append(",");
                }
            }
        }
        String substring = builder.toString().substring(0, builder.length() - 1);
        newsBean.setId(GenID.getID());
        newsBean.setPublishUser(ShiroUtils.getUserInfo().getName());
        newsBean.setPublishDept(substring);
        newsBean.setFileUrl(stringBuilder.toString());
        if (newsBean.getPublishDate() == null){
            newsBean.setPublishDate(new Date(System.currentTimeMillis()));
        }
        //处理期数
        Integer integer = newsDao.getMaxIndex();
        if (integer == null){
            integer = 1;
        }else {
            integer = integer + 1;
        }
        newsBean.setNextNum(integer);
        int insert = newsDao.insert(newsBean);
        if (insert >= 0){
            return true;
        }else {
            return false;
        }
    }
}
