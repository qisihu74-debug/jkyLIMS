package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.NewsBean;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2023-04-28 11:12
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface NewsDao extends BaseMapper<NewsBean> {
}
