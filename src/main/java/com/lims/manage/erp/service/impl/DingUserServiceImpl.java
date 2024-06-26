package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.mapper.DingUsertDao;
import com.lims.manage.erp.service.DingUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2021/11/22 10:51
 * @Copyright © 河南交科院
 */
@Service
public class DingUserServiceImpl extends ServiceImpl<DingUsertDao, DingUserEntity> implements DingUserService {
   @Autowired
   private DingUsertDao dingUsertDao;

    @Override
    public List<DingUserEntity> getInfo() {
        return dingUsertDao.getInfo();
    }
}
