package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCustomerEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

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

}
