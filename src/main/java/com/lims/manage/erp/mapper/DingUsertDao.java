package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.vo.UserInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/11/22 10:52
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface DingUsertDao extends BaseMapper<DingUserEntity> {
    /**
     * 更新用户手机，邮箱等信息
     * @param vo
     * @return
     */
    Boolean updateDingUserInfo(UserInfoVo vo);

    /**
     * 获取人员信息
     */
    List<DingUserEntity> getAllUser();

    /**
     * 根据条件 获取人员信息
     */
    List<DingUserEntity> getAllUserTerm(@Param(value = "search") String search);

    /**
     * 修改人员信息
     * @param personEntity
     * @return
     */
    int updatePerson(DingUserEntity personEntity);

    /**
     * 实现人员删除
     * @param userid
     * @return
     */
    int deletePerson(String userid);
}
