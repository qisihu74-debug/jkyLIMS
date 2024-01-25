package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.JsonRootBean;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-01-25 11:24
 * @Copyright © 河南交科院
 */
public interface AppServiceDao extends BaseMapper<JsonRootBean> {
    @Select("select num from sys_index")
    int getIndex();

    @Update("update sys_index set num = #{num} where id=1")
    void updateIndex(@Param("num") int num);
}
