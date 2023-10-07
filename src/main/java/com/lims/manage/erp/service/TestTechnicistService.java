package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TechnicistCapacity;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.entity.TestTechnicist;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TestTechnicistVo;

import java.io.Serializable;
import java.util.List;

/**
 * 技术人员(TestTechnicistVo)表服务接口
 *
 * @author makejava
 * @since 2022-02-23 09:14:45
 */
public interface TestTechnicistService extends IService<TestTechnicist> {
    Result addTestTechnicist(TestTechnicist TestTechnicist);
    Result updTestTechnicist(TestTechnicist TestTechnicist);
    Result delTestTechnicist(List<Long> idList);
    IPage<TestTechnicistVo> getListPage(IPage<TestTechnicistVo> page, Wrapper<TestTechnicist> queryWrapper);
    List<SysUserEntity> getUserList();

    List<TechnicistCapacity> getTypeAndProductList(Integer id);

    List<TestProductType> getCapacityMessage(Serializable id);

    List<TestProductType> getProductTypeAndProduct();
}

