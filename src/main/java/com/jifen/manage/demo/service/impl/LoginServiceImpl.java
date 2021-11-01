package com.jifen.manage.demo.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jifen.manage.demo.entity.User;
import com.jifen.manage.demo.entity.UserRole;
import com.jifen.manage.demo.mapper.LoginMapper;
import com.jifen.manage.demo.service.LoginService;
import com.jifen.manage.demo.util.GenID;
import com.jifen.manage.demo.util.MinIoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author gjl
 */

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper loginMapper;

    @Override
    public User getAdmin(String nick){
        return loginMapper.getUserByName(nick);
    }

    @Override
    public User getUser(String adminId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_code",adminId);
        User user = loginMapper.getUserByCode(adminId);

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(User user) {
        long id = GenID.getID();
        user.setId(id);
        UserRole userRole = new UserRole();
        userRole.setUserId(id);
        userRole.setRoleId(Long.parseLong(user.getUserType()));
        //上传文件
        MultipartFile idPositiveFile = user.getIdPositiveFile();
        MultipartFile idObverseFile = user.getIdObverseFile();
        if (user.getBusinessFile() != null){
            MultipartFile businessFile = user.getBusinessFile();
            String upload = MinIoUtil.upload("营业执照目录", businessFile);
            user.setBusinessLicens(upload);
        }
        String upload = MinIoUtil.upload("身份证正面目录", idPositiveFile);
        user.setIdentificationPositive(upload);
        String upload1 = MinIoUtil.upload("身份证反面目录", idObverseFile);
        user.setIdentificationObverse(upload1);
        loginMapper.insertUser(user);
        loginMapper.insertUserRole(userRole);
    }

    @Override
    public List<User> adminList() {
        QueryWrapper queryWrapper = new QueryWrapper();
        return loginMapper.selectList(queryWrapper);
    }

    @Override
    public User getUserByName(String userName) {
        return loginMapper.getUserByName(userName);
    }

    @Override
    public User getUserById(String identification) {
        return loginMapper.getUserById(identification);
    }

    @Override
    public User getUserByMobile(String mobile) {
        return loginMapper.getUserByMobile(mobile);
    }
}
