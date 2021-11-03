package com.jifen.manage.demo.service;

import com.jifen.manage.demo.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author gjl
 */

public interface LoginService {

    /**
     * 查询管理员对应的密码
     *
     * @param nick
     * @return String
     */
    User getAdmin(String nick);

    /**
     * 获取用户对象
     * @param adminId
     * @return
     */
    User getUser(String adminId);

    /**
     * 注册账号
     * @param user
     */
    void save(User user, MultipartFile idPositiveFile, MultipartFile idObverseFile, MultipartFile businessFile);

    List<User> adminList();

    /**
     * 根据用户名查找
     * @param userName
     * @return
     */
    User getUserByName(String userName);

    User getUserById(String identification);

    User getUserByMobile(String mobile);

}
