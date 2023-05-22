package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.NewsBean;
import com.lims.manage.erp.vo.NewsBeanVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2023-04-28 11:10
 * @Copyright © 河南交科院
 */
public interface NewsService extends IService<NewsBean> {

    Boolean saveNews(NewsBean newsBean, MultipartFile[] files);

    /**
     * 新增简报
     * @param newsBeanVo
     * @return
     */
    Boolean saveNews(NewsBeanVo newsBeanVo);

    /**
     *  查詢 新闻消息列表
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<NewsBeanVo> getPageInfoList(String search, Integer pageNum, Integer pageSize);
}
