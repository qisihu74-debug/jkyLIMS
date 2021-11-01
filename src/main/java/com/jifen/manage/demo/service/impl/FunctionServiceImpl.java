package com.jifen.manage.demo.service.impl;

import com.jifen.manage.demo.mapper.LoginMapper;
import com.jifen.manage.demo.service.FunctionService;
import com.jifen.manage.demo.entity.FunctionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.service.impl
 * @desc
 * @date 2021/9/22 10:22
 * @Copyright © 河南交科院
 */
@Service
public class FunctionServiceImpl implements FunctionService {
    @Autowired
    private LoginMapper mapper;

    @Override
    public List<FunctionEntity> getFunctionsById(Long id) {
        List<FunctionEntity> functionsById = mapper.getFunctionsById(id);
        return functionsById;
    }
}
