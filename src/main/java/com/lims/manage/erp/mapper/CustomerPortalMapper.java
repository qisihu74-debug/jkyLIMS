package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.CustomerClaimAdminVo;
import com.lims.manage.erp.vo.CustomerClaimCandidateVo;
import com.lims.manage.erp.vo.CustomerReportVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface CustomerPortalMapper {

    @Select("<script>" +
            "SELECT c.company_id AS candidateCompanyId, " +
            "       c.company_name AS companyName, " +
            "       c.address AS companyAddress, " +
            "       tc.customer_id AS candidateCustomerId, " +
            "       tc.contacts AS contactName, " +
            "       tc.phone AS contactPhone, " +
            "       CASE " +
            "         WHEN #{mobile} IS NOT NULL AND #{mobile} != '' AND tc.phone = #{mobile} THEN '手机号精确匹配联系人' " +
            "         WHEN #{name} IS NOT NULL AND #{name} != '' AND tc.contacts LIKE CONCAT('%', #{name}, '%') THEN '联系人姓名模糊匹配' " +
            "         WHEN #{companyName} IS NOT NULL AND #{companyName} != '' AND c.company_name LIKE CONCAT('%', #{companyName}, '%') THEN '单位名称模糊匹配' " +
            "         ELSE '历史客户候选' " +
            "       END AS matchBasis " +
            "FROM test_company c " +
            "LEFT JOIN test_customer tc ON tc.company_id = c.company_id " +
            "WHERE 1 = 1 " +
            "  AND ( " +
            "    (#{mobile} IS NOT NULL AND #{mobile} != '' AND tc.phone = #{mobile}) " +
            "    OR (#{name} IS NOT NULL AND #{name} != '' AND tc.contacts LIKE CONCAT('%', #{name}, '%')) " +
            "    OR (#{companyName} IS NOT NULL AND #{companyName} != '' AND c.company_name LIKE CONCAT('%', #{companyName}, '%')) " +
            "  ) " +
            "ORDER BY CASE WHEN #{mobile} IS NOT NULL AND #{mobile} != '' AND tc.phone = #{mobile} THEN 0 ELSE 1 END, c.company_id DESC " +
            "LIMIT 20" +
            "</script>")
    List<CustomerClaimCandidateVo> selectClaimCandidates(@Param("name") String name,
                                                         @Param("mobile") String mobile,
                                                         @Param("companyName") String companyName);

    @Select("<script>" +
            "SELECT r.id, r.account_id AS accountId, " +
            "       a.mobile AS accountMobile, a.name AS accountName, " +
            "       r.candidate_company_id AS candidateCompanyId, c.company_name AS companyName, " +
            "       r.candidate_customer_id AS candidateCustomerId, tc.contacts AS contactName, tc.phone AS contactPhone, " +
            "       r.match_basis AS matchBasis, r.status, r.apply_remark AS applyRemark, " +
            "       r.review_remark AS reviewRemark, r.review_user_id AS reviewUserId, " +
            "       r.review_time AS reviewTime, r.create_time AS createTime " +
            "FROM cus_claim_request r " +
            "LEFT JOIN cus_account a ON a.account_id = r.account_id " +
            "LEFT JOIN test_company c ON c.company_id = r.candidate_company_id " +
            "LEFT JOIN test_customer tc ON tc.customer_id = r.candidate_customer_id " +
            "<where> " +
            "  <if test='status != null and status != \"\"'>AND r.status = #{status}</if> " +
            "</where> " +
            "ORDER BY r.create_time DESC, r.id DESC " +
            "LIMIT 200" +
            "</script>")
    List<CustomerClaimAdminVo> selectClaimReviewList(@Param("status") String status);

    @Select("<script>" +
            "SELECT r.id AS reportId, r.report_code AS reportCode, r.sample_name AS sampleName, " +
            "       r.report_type AS reportType, r.state AS reportState, " +
            "       DATE_FORMAT(r.report_time, '%Y-%m-%d') AS reportTime, " +
            "       DATE_FORMAT(r.seal_time, '%Y-%m-%d') AS sealTime, " +
            "       DATE_FORMAT(r.issuer_time, '%Y-%m-%d') AS issuerTime, " +
            "       e.id AS entrustId, e.entrustment_no AS entrustmentNo, " +
            "       e.entrust_company AS entrustCompany, e.project_name AS projectName " +
            "FROM test_report_record r " +
            "LEFT JOIN test_entrusted_info e ON e.id = r.entrustment_id " +
            "WHERE e.entrust_company_id = #{companyId} " +
            "  AND e.state &lt;&gt; 144 " +
            "  AND COALESCE(NULLIF(r.seal_report_url, ''), r.report_url) IS NOT NULL " +
            "  <if test='keyword != null and keyword != \"\"'> " +
            "    AND (r.report_code LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR r.sample_name LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR e.entrustment_no LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR e.project_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "  </if> " +
            "ORDER BY r.report_time DESC, r.id DESC " +
            "LIMIT 200" +
            "</script>")
    List<CustomerReportVo> selectCustomerReports(@Param("companyId") Integer companyId,
                                                 @Param("keyword") String keyword);

    @Select("SELECT r.id AS reportId, r.report_code AS reportCode, " +
            "       COALESCE(NULLIF(r.seal_report_url, ''), r.report_url) AS downloadUrl " +
            "FROM test_report_record r " +
            "LEFT JOIN test_entrusted_info e ON e.id = r.entrustment_id " +
            "WHERE r.id = #{reportId} " +
            "  AND e.entrust_company_id = #{companyId} " +
            "  AND e.state <> 144 " +
            "LIMIT 1")
    CustomerReportVo selectCustomerReportDownload(@Param("reportId") Long reportId,
                                                  @Param("companyId") Integer companyId);
}
