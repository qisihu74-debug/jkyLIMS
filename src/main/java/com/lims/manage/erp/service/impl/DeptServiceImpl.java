package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingDeptVo;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2021/11/19 15:17
 * @Copyright © 河南交科院
 */
@Service
public class DeptServiceImpl  extends ServiceImpl<DeptDao, DingDeptEntity> implements DeptService {
    @Autowired
    private DeptDao deptDao;

    @Override
    public List<DingDeptVo> getAllDept() {
        return deptDao.getAllDept();
    }
}
