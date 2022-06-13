package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.AlertEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2022/5/31 15:50
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface AlertDao extends BaseMapper<AlertEntity> {

    @Delete("delete from test_alert where entrust_id=#{id}")
    void deleteByEntrustId(@Param("id") Long id);
}
