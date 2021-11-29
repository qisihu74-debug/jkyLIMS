package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/29 11:41
 * 委托公司Dao层
 */
@Component
@Mapper
public interface TestCompanyDao extends BaseMapper<TestCompanyEntity> {

    /**
     * TypeId = 1 委托单位 TypeId = 2 见证单位
     * @param TypeId
     * @return
     */
    List<LabelValueVo> selectEntrustCompanyList(Integer TypeId);
    /**
     *  返回委托基础信息处理。
      * @return
     */
    List<TestInitDataEntity> selectEntrustBasis();
    /**
     * 返回团队信息
     * @return
     */
    List<LabelValueVo> selectTestTeam();

    /**
     * 依据委托单位ID 或者 见证单位ID。
     * @param companyId
     * @return
     */
    List<TestCustomerJsonEntity> selectPeopleInformation(Integer companyId);
}
