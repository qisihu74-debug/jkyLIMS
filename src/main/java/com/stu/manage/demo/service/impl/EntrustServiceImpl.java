package com.stu.manage.demo.service.impl;

import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.mapper.EntrustMapper;
import com.stu.manage.demo.service.EntrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntrustServiceImpl implements EntrustService {
    @Autowired
    private EntrustMapper entrustMapper;
    @Override
    public EntrustInfo onceMore(int entrustId) {
        return entrustMapper.onceMore(entrustId);
    }
}
