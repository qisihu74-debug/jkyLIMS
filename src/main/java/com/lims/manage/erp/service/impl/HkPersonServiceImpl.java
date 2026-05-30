package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.HkPerson;
import com.lims.manage.erp.mapper.HkPersonDao;
import com.lims.manage.erp.service.HkPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-09-13 15:11
 * @Copyright © 河南交科院
 */
@Service
public class HkPersonServiceImpl extends ServiceImpl<HkPersonDao, HkPerson> implements HkPersonService {
    @Autowired
    private HkPersonDao hkPersonDao;

    @Override
    public PageInfo<HkPerson> personList(Integer pageNum, Integer pageSize, String name, String mobile, String state) {
        PageHelper.startPage(pageNum,pageSize);
        List<HkPerson> list = hkPersonDao.personList(name,mobile,state);
        PageInfo<HkPerson> pageInfo = new PageInfo(list);
        return pageInfo;
    }
}
