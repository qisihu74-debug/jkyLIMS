package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DivideRectificationRecord;
import com.lims.manage.erp.vo.NonconformityVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-07-09 16:09
 * @Copyright © 河南交科院
 */
public interface DivideRectificationRecordDao extends BaseMapper<DivideRectificationRecord> {


    @Select("<script>" +
            "SELECT a.name AS auditName, DATE_FORMAT(a.start_time,'%Y-%m-%d') AS auditTime, d.dept_name AS deptName, " +
            "  r.auditor_name AS auditorName, r.dept_leader AS deptLeader, r.state AS state, " +
            "  r.received_date AS receivedDate, r.required_completion_date AS requiredDate, " +
            "  r.actual_finishing_date AS actualDate, r.verification_date AS verifyDate, " +
            "  r.analysis_and_corrective_measures AS measures, r.verification_of_corrective_measures AS verification " +
            "FROM qs_divide_rectification_record r " +
            "LEFT JOIN qs_audit_divide d ON r.divide_id = d.divide_id " +
            "LEFT JOIN qs_audit_active a ON d.active_id = a.active_id " +
            "<where>" +
            "<if test='state != null and state != \"\"'> AND r.state = #{state} </if>" +
            "<if test='deptName != null and deptName != \"\"'> AND d.dept_name LIKE CONCAT('%',#{deptName},'%') </if>" +
            "</where>" +
            "ORDER BY r.required_completion_date DESC" +
            "</script>")
    List<NonconformityVo> nonconformityList(@Param("state") String state, @Param("deptName") String deptName);

}
