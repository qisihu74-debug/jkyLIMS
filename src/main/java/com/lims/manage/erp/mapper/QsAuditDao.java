package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.InternalAuditorActive;
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
 * @date 2024-07-05 14:02
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface QsAuditDao {

    @Select("<script>"
            + "SELECT"
            + " qaa.active_id,"
            + " qad.divide_id,"
            + " qaa.name,"
            + " qaa.start_time,"
            + " qaa.end_time,"
            + " qaa.editor_date,"
            + " IFNULL(qdr.state, '检查待开始') AS state"
            + " FROM qs_audit_active qaa"
            + " LEFT JOIN qs_audit_divide qad ON qaa.active_id = qad.active_id"
            + " LEFT JOIN qs_audit_divide_rel qdr ON qad.divide_id = qdr.divide_id"
            + " WHERE qad.auditor_id = #{userId}"
            + "<if test=\"name != null and name.trim().length() > 0\">"
            + " AND qaa.name LIKE CONCAT('%', #{name}, '%')"
            + "</if>"
            + "</script>")
    List<InternalAuditorActive> internalAuditorActiveList(@Param("name") String name, @Param("userId") Long userId);

}
