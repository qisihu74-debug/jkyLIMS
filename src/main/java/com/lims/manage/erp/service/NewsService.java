package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.NewsBean;
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
}
