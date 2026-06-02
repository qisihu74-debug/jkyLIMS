package com.lims.manage.erp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 客户免登录查询（公开接口）专用只读查询
 */
@Mapper
public interface PublicQueryDao {

    /** 按 委托编号 + 委托电话 匹配委托（鉴权用）；排除作废(144)；编号可能重复，取最新一条 */
    @Select("SELECT id, entrustment_no AS entrustmentNo, entrust_company AS entrustCompany, " +
            "project_name AS projectName, DATE_FORMAT(acceptance_date,'%Y-%m-%d') AS acceptanceDate, " +
            "DATE_FORMAT(create_time,'%Y-%m-%d') AS createTime, state " +
            "FROM test_entrusted_info " +
            "WHERE entrustment_no = #{no} AND entrust_phone = #{phone} AND state <> 144 " +
            "ORDER BY id DESC LIMIT 1")
    Map<String, Object> matchEntrust(@Param("no") Integer no, @Param("phone") String phone);

    /** 委托进度聚合（样品/报告计数 + 报告审核/盖章/签发最大时间） */
    @Select("SELECT " +
            "(SELECT COUNT(DISTINCT sample_id) FROM test_entrusted_sample_checkitem_rel WHERE entrust_id=#{eid}) AS sampleCount, " +
            "(SELECT COUNT(*) FROM test_report_record WHERE entrustment_id=#{eid}) AS reportCount, " +
            "(SELECT COUNT(*) FROM test_report_record WHERE entrustment_id=#{eid} AND report_url IS NOT NULL) AS issuedCount, " +
            "DATE_FORMAT((SELECT MAX(verifyer_time) FROM test_report_record WHERE entrustment_id=#{eid}),'%Y-%m-%d') AS verifyTime, " +
            "DATE_FORMAT((SELECT MAX(seal_time) FROM test_report_record WHERE entrustment_id=#{eid}),'%Y-%m-%d') AS sealTime, " +
            "DATE_FORMAT((SELECT MAX(issuer_time) FROM test_report_record WHERE entrustment_id=#{eid}),'%Y-%m-%d') AS issuerTime")
    Map<String, Object> progressAgg(@Param("eid") Long eid);

    /** 委托下已出具(report_url 非空)的报告列表 */
    @Select("SELECT id AS reportId, report_code AS reportCode, report_type AS reportType, " +
            "DATE_FORMAT(report_time,'%Y-%m-%d') AS reportTime " +
            "FROM test_report_record WHERE entrustment_id=#{eid} AND report_url IS NOT NULL " +
            "ORDER BY report_time DESC")
    List<Map<String, Object>> reportList(@Param("eid") Long eid);

    /** 报告下载地址（优先盖章版） */
    @Select("SELECT COALESCE(NULLIF(seal_report_url,''), report_url) FROM test_report_record WHERE id=#{id}")
    String reportUrl(@Param("id") Long id);
}
