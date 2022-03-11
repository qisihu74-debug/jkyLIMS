package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
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

    /**
     * 根据任务单id 获取 部门id 查询部门下人员姓名
     * @param taskId
     * @return
     */
    @Select("SELECT\n" +
            "t3.name\n" +
            "FROM\n" +
            "test_task as t1\n" +
            "LEFT JOIN test_user_team_rel as t2 ON t1.dept_id = t2.team_id\n" +
            "LEFT JOIN sys_user as t3 ON t3.user_id = t2.user_id\n" +
            "WHERE t1.id = #{taskId}")
    List<String> getTaskIdUserName(Long taskId);
}
