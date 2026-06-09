package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.CustomerClaimAdminVo;
import com.lims.manage.erp.vo.CustomerClaimCandidateVo;
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
}
