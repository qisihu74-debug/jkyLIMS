package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.EntrustRemittanceRegistrationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface EntrustRemittanceRegistrationMapper extends BaseMapper<EntrustRemittanceRegistrationEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(EntrustRemittanceRegistrationEntity record);

    int insertSelective(EntrustRemittanceRegistrationEntity record);

    EntrustRemittanceRegistrationEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(EntrustRemittanceRegistrationEntity record);

    int updateByPrimaryKey(EntrustRemittanceRegistrationEntity record);

    /**
     * 通过委托单id 和 委托单号 查询是否存在
     *
     * @param entrustId
     * @param entrustmentNo
     * @return
     */
    @Select("SELECT\n" +
            "\tid \n" +
            "FROM\n" +
            "\ttest_entrusted_info \n" +
            "WHERE\n" +
            "\tid = #{entrustId} \n" +
            "\tAND IFNULL( CONCAT( entrust_category_type, entrustment_no ), entrustment_no ) = #{entrustmentNo}")
    List<Long> selectEntrustedId(@Param("entrustId") Long entrustId, @Param("entrustmentNo") String entrustmentNo);
}