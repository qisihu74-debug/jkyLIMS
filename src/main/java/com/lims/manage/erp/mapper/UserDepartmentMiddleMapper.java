package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.UserDepartmentMiddleEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDepartmentMiddleMapper extends BaseMapper<UserDepartmentMiddleEntity> {
    int insert(UserDepartmentMiddleEntity record);

    int insertSelective(UserDepartmentMiddleEntity record);

    @Update("UPDATE sys_user_department_middle  set user_id = null      WHERE ding_user_id = #{dingUserId}")
    int updateUserIsNull(@Param("dingUserId") String dingUserId);
}