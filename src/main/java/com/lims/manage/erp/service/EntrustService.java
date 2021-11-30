package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.vo.LabelValueVo;

import java.util.List;
import java.util.Map;

public interface EntrustService{
    /**
     * 新增委托
     * @param vo
     * @return
     */
    Boolean addEntrust(EntrustAddVo vo);

    Map<String, List<LabelValueVo>> returnEntrustData();

    List<TestCustomerJsonEntity> returnTestCustomerEntityList(Integer companyId);

    /**
     * 新增委托单位信息
     * @param testCompanyEntity
     * @return
     */
    Boolean addCompanyData(TestCompanyJsonEntity testCompanyEntity);

    List<SampleEntity> getSampleDataList(SampleEntity sampleEntity);
}
