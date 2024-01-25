package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.StandardNovelty;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-01-23 11:10
 * @Copyright © 河南交科院
 */
public interface StandardNoveltyDao extends BaseMapper<StandardNovelty> {
    void updateBatchByCode(@Param("list") List<StandardNovelty> list);

    @Delete("delete from test_standard_novelty")
    void deleteAll();
}
