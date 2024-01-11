package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.InternalAudit;
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
 * @date 2024-01-02 14:52
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface InternalAuditDao extends BaseMapper<InternalAudit> {

    /**
     * 查看内审角色看到的数据
     * @return
     */
    @Select("SELECT DISTINCT\n" +
            "\ttb1.* \n" +
            "FROM\n" +
            "\t(\n" +
            "\tSELECT\n" +
            "\t\tt1.* \n" +
            "\tFROM\n" +
            "\t\ttest_internal_audit t1\n" +
            "\t\tLEFT JOIN test_internal_audit_info t2 ON t1.id = t2.audit_id \n" +
            "\tWHERE\n" +
            "\t\tt2.auditor_id = #{userId} UNION ALL\n" +
            "\tSELECT\n" +
            "\t\t* \n" +
            "\tFROM\n" +
            "\t\ttest_internal_audit \n" +
            "\tWHERE\n" +
            "\taudit_leader_id = #{userId} \n" +
            "\t) tb1 order by tb1.operate_date")
    List<InternalAudit> getListByNsRole(@Param("userId") Long userId);
}
