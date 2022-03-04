package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/12/6 18:17
 * @Copyright © 河南交科院
 */
@Mapper
@Component
public interface TeamMapper extends BaseMapper {

    /**
     * 获取部门下的用户信息
     * @param teamId
     * @return
     */
    List<SysUserEntity> getUsersByTid(String teamId);

    /**
     * 查询用户所在的团队ID
     * @param userId
     * @return
     */
    List<Long> getUserTeamIds(Long userId);
}
