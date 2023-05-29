package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.NewsBean;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.mapper.NewsDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.service.NewsService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.LinkedDataVo;
import com.lims.manage.erp.vo.NewsBeanVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    /**
     * 新增简报信息
     *
     * @param newsBeanVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveNews(NewsBeanVo newsBeanVo) {
        // 主键
        newsBeanVo.setId(GenID.getID());
        // if 链接不为空
        if (!CollectionUtils.isEmpty(newsBeanVo.getLinkedData())) {
            for (LinkedDataVo linkedDataVo : newsBeanVo.getLinkedData()) {
                linkedDataVo.setSysNewsId(newsBeanVo.getId());
            }
            // 进行批量新增 链接数据层
            newsDao.addBatchLinkedData(newsBeanVo.getLinkedData());
        }
        // 新增简报信息
        NewsBean newsBean = new NewsBean();
        newsBean.setId(newsBeanVo.getId());
        newsBean.setTitle(newsBeanVo.getTitle());
        newsBean.setContent(newsBeanVo.getContent());
        if (newsBeanVo.getPublishDate() != null) {
            newsBean.setPublishDate(newsBeanVo.getPublishDate());
        } else {
            newsBean.setPublishDate(new Date(System.currentTimeMillis()));
        }
        int insert = newsDao.insert(newsBean);
        if (insert >= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PageInfo<NewsBeanVo> getPageInfoList(String search, Integer pageNum, Integer pageSize) {
        QueryWrapper<NewsBean> queryWrapper = new QueryWrapper<NewsBean>();
        if (StringUtils.isNotEmpty(search)) {
            queryWrapper.and(wq -> {
                return wq.like("content", search)
                        .or()
                        .like("title", search);
            });
        }
        queryWrapper.orderByDesc("id");
        PageHelper.startPage(pageNum, pageSize);
        List<NewsBean> list = newsDao.selectList(queryWrapper);
        List<NewsBeanVo> beanVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)) {
            for (NewsBean data : list) {
                NewsBeanVo newsBeanVo = new NewsBeanVo();
                newsBeanVo.setId(data.getId());
                newsBeanVo.setContent(data.getContent());
                newsBeanVo.setTitle(data.getTitle());
                newsBeanVo.setPublishDate(data.getPublishDate());
                // 根据 简报主键查询 链接列表
                List<LinkedDataVo> linkedList = newsDao.selectLinkedList(data.getId());
                if (!CollectionUtils.isEmpty(linkedList)) {
                    int serialNumber = 1;
                    for (LinkedDataVo linkedDataVo : linkedList) {
                        linkedDataVo.setSerialNumber(serialNumber);
                        serialNumber += 1;
                    }
                    newsBeanVo.setLinkedData(linkedList);
                }
                // 赋值
                beanVoList.add(newsBeanVo);
            }
        }
        PageInfo<NewsBeanVo> pageInfo = new PageInfo(beanVoList);
        return pageInfo;
    }
}
