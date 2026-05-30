package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.NewsBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import com.lims.manage.erp.vo.LinkedDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

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
    @Select("select max(`next_num`) from sys_news")
    Integer getMaxIndex();

    /**
     * 批量新增 链接数据层
     * @param items
     * @return
     */
    int addBatchLinkedData(@Param("items") List<LinkedDataVo> items);

    /**
     * 根据简报id 查询链接列表
     * @param sysNewsId
     * @return
     */
    List<LinkedDataVo> selectLinkedList(@Param("sysNewsId")Long sysNewsId);
}
