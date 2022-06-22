package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TeamTreeStructureEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
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
     * 获取当前用户所在科室id
     * @param userId
     * @return
     */
    @Select("select team_id from test_technicist where user_id = #{userId} and t2.del_flag = 0")
    Long getTeamIdByUid(@Param("userId") Long userId);

    /**
     * 获取当前科室下的下级科室
     * @param teamId
     * @return
     */
    @Select("WITH RECURSIVE td AS (\n" +
                   "                SELECT * FROM test_team WHERE id = #{teamId} \n" +
                   "                UNION ALL \n" +
                   "                SELECT c.* FROM test_team c ,td WHERE c.pid = td.id\n" +
                   "            ) SELECT * FROM td ORDER BY td.id")
    List<TeamTreeStructureEntity> getChirds(Long teamId);

    /**
     * 获取team下所有userId
     * @param teamId
     * @return
     */
    @Select("SELECT u.user_id \n" +
            "        FROM\n" +
            "            test_team t\n" +
            "            LEFT JOIN test_technicist tr\n" +
            "        ON t.id = tr.team_id\n" +
            "            LEFT JOIN sys_user u ON tr.user_id = u.user_id\n" +
            "        WHERE\n" +
            "            t.id = #{teamId}")
    List<Long> getUsersByTeamId(@Param("teamId") Long teamId);

    /**
     * 获取多个team下的ids
     * @param list
     * @return
     */
//    @Select("SELECT\n" +
//            "\tu.user_id\n" +
//            "FROM\n" +
//            "\ttest_team t\n" +
//            "LEFT JOIN test_user_team_rel tr ON t.id = tr.team_id\n" +
//            "LEFT JOIN sys_user u ON tr.user_id = u.user_id\n" +
//            "WHERE\n" +
//            "\tt.id IN \n" +
//            "< foreach collection = \"list\" item = \"id\" INDEX = \"index\" OPEN = \"(\" CLOSE = \")\" SEPARATOR = \",\" > #{id}\n" +
//            "\t</ foreach >\n" +
//            "AND u.user_id IS NOT NULL")
    List<Long> getUsersByTeams(@Param("list") List<Long> list);

    /**
     * 根据任务单id 获取 部门id 查询部门下人员姓名
     * @param taskId
     * @return
     */
    @Select("SELECT\n" +
            "t3.name\n" +
            "FROM\n" +
            "test_task as t1\n" +
            "LEFT JOIN test_technicist as t2 ON t1.dept_id = t2.team_id\n" +
            "LEFT JOIN sys_user as t3 ON t3.user_id = t2.user_id\n" +
            "WHERE t1.id = #{taskId}")
    List<String> getTaskIdUserName(Long taskId);

    /**
     * 查询所有部门id
     */
    List<TeamTreeStructureEntity> getDeptAll();
}
