package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.QsActiveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description: 内审基础信息
 * @Author: DLC
 * @Date: 2024/7/10 16:17
 */
@Mapper
@Repository
public interface ActiveMapper extends BaseMapper<QsActiveEntity> {

    @Select("SELECT DISTINCT\n" +
            "\tqar.state\n" +
            "FROM\n" +
            "\tqs_audit_divide qad\n" +
            "LEFT JOIN qs_audit_divide_rel qar ON qad.divide_id = qar.divide_id\n" +
            "WHERE\n" +
            "\tqad.active_id = #{activeId}\n" +
            "AND qad.divide_id = #{divideId}")
    List<String> getDiviDeStates(@Param("activeId") int activeId, @Param("divideId") int divideId);
}
