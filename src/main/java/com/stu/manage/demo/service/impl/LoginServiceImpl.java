package com.stu.manage.demo.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stu.manage.demo.entity.Login;
import com.stu.manage.demo.entity.LoginToken;
import com.stu.manage.demo.mapper.LoginMapper;
import com.stu.manage.demo.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 */

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper loginMapper;

    @Override
    public Login getAdmin(String name){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("admin_name",name);
        return loginMapper.selectOne(queryWrapper);
    }

    @Override
    public LoginToken getUser(String adminId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("admin_id",adminId);
        Login login = loginMapper.selectOne(queryWrapper);
        LoginToken loginToken = new LoginToken();
        loginToken.setAdminId(login.getAdminId());
        loginToken.setPassWord(login.getPassWord());
        return loginToken;
    }

    @Override
    public int save(Login login) {
        int insert = loginMapper.insert(login);
        return insert;
    }

    @Override
    public List<Login> adminList() {
        QueryWrapper queryWrapper = new QueryWrapper();
        return loginMapper.selectList(queryWrapper);
    }
}
