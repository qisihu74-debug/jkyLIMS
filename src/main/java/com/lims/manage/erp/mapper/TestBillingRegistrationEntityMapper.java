package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.vo.ClientOrderdetailVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TestBillingRegistrationEntityMapper extends BaseMapper<TestBillingRegistrationEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestBillingRegistrationEntity record);

    int insertSelective(TestBillingRegistrationEntity record);

    TestBillingRegistrationEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestBillingRegistrationEntity record);

    int updateByPrimaryKey(TestBillingRegistrationEntity record);

    /**
     * 通过委托单号 集合 获取数据
     *
     * @param list
     * @return
     */
    List<ClientOrderdetailVo> selectEntrustNoList(@Param("list") List<TestBillingRegistrationEntity> list);

    /**
     * 更新 委托单中 isInvoice = "是"
     *
     * @param entrustId
     * @return
     */
    @Update("update test_entrusted_info  set is_invoice = '是' where id  = #{entrustId}")
    int updateEntrustIsInvoice(@Param("entrustId") Long entrustId);

}