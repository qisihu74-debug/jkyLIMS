package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCustomerEntity;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.TestCustomerVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/29 16:53
 * 委托公司下人员信息
 */
@Component
@Mapper
public interface TestCustomerDao extends BaseMapper<TestCompanyEntity> {
    /**
     * 动态新增 公司下联系人信息
     * @param testCustomerEntity
     * @return
     */
    int insertTestCustomer(TestCustomerEntity testCustomerEntity);

    /**
     * 通过公司id 效验联系人 是否存在
     */
    List<TestCustomerVo> getTestCustomerClientList(TestCustomerEntity testCustomerClientEntity);

    /**
     * 查询通过id 获取员工列表
     *
     * @param companyIds
     * @return
     */
    List<TestCustomerEntity> selectCustomerList(@Param("companyIds") List<Integer> companyIds);

    /**
     * 通过委托单及人员信息 查询委托单信息列表
     *
     * @param entity
     * @return
     */
    List<EntrustAddVo> selectEntrustList(TestCustomerEntity entity);

}
