package com.jifen.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jifen.manage.demo.entity.FunctionEntity;
import com.jifen.manage.demo.entity.User;
import com.jifen.manage.demo.entity.UserRole;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 */

@Component
@Mapper
public interface LoginMapper extends BaseMapper {

    /**
     * 插入用户角色关系表
     */
    void insertUserRole(UserRole userRole);

    /**
     * 插入用户表
     * @param user
     * @return
     */
    void insertUser(User user);

    /**
     * 获取角色下的菜单列表
     * @param id
     * @return
     */
    List<FunctionEntity> getFunctionsById(Long id);

    /**
     * 根据用户表示获取用户信息
     * @param adminId
     * @return
     */
    User getUserByCode(String adminId);

    /**
     * 根据用户名查找
     * @param userName
     * @return
     */
    User getUserByName(String userName);

    /**
     * 根据身份证查询
     * @param identification
     * @return
     */
    User getUserById(String identification);

    /**
     * 根据手机号查询
     * @param mobile
     * @return
     */
    User getUserByMobile(String mobile);
}
