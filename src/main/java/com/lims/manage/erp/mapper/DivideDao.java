package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DivideEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-07-10 11:31
 * @Copyright © 河南交科院
 */
@Mapper
@Repository
public interface DivideDao extends BaseMapper<DivideEntity> {

    /**
     * 查询分组id 按照 部门id提供序号 排序
     *
     * @param activeId
     * @return
     */
    @Select("SELECT\n" +
            "\tt1.* \n" +
            "FROM\n" +
            "\tqs_audit_divide AS t1\n" +
            "\tLEFT JOIN sys_dept AS t2 ON t1.dept_id = t2.id \n" +
            "WHERE\n" +
            "\tt1.active_id = #{activeId} \n" +
            "ORDER BY\n" +
            "\tt2.serial_number ASC")
    List<DivideEntity> selectDivideList(@Param("activeId") String activeId);
}
