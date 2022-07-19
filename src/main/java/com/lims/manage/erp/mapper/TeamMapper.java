package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TeamTreeStructureEntity;
import com.lims.manage.erp.entity.TestCheckItemTeamRel;
import com.lims.manage.erp.entity.TestTeam;
import org.apache.ibatis.annotations.Mapper;
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
    @Select("select team_id from test_technicist where user_id = #{userId} and del_flag = 0")
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
    @Select("SELECT * FROM test_team where status = 0 and del_flag = 0")
    List<TeamTreeStructureEntity> getAllTeams();
    @Select("WITH RECURSIVE td AS (\n" +
            "                SELECT * FROM test_team WHERE id = #{teamId} \n" +
            "                UNION ALL \n" +
            "                SELECT c.* FROM test_team c ,td WHERE c.pid = td.id\n" +
            "            ) SELECT id FROM td ORDER BY td.id")
    List<Long> getNodeTeamId(Long teamId);

    /**
     * 查询2级团队
     * @return
     */
    List<TeamTreeStructureEntity> getAllSecondTeam();
    List<Long> getAllSecondTeamId();

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
     * 获取团队下所有子集团队下技术人员集合
     * @param id
     * @return
     */
    @Select("SELECT DISTINCT\n" +
            "\tuus.user_id As userId,\n" +
            "\tuus. NAME As name\n" +
            "FROM\n" +
            "\ttest_technicist tte\n" +
            "LEFT JOIN sys_user uus ON tte.user_id = uus.user_id\n" +
            "WHERE\n" +
            "\ttte.team_id IN (\n" +
            "\t\tSELECT\n" +
            "\t\t\ttb1.id\n" +
            "\t\tFROM\n" +
            "\t\t\t(\n" +
            "\t\t\t\tWITH RECURSIVE td AS (\n" +
            "\t\t\t\t\tSELECT\n" +
            "\t\t\t\t\t\t*\n" +
            "\t\t\t\t\tFROM\n" +
            "\t\t\t\t\t\ttest_team\n" +
            "\t\t\t\t\tWHERE\n" +
            "\t\t\t\t\t\tid = #{id}\n" +
            "\t\t\t\t\tUNION ALL\n" +
            "\t\t\t\t\t\tSELECT\n" +
            "\t\t\t\t\t\t\tc.*\n" +
            "\t\t\t\t\t\tFROM\n" +
            "\t\t\t\t\t\t\ttest_team c,\n" +
            "\t\t\t\t\t\t\ttd\n" +
            "\t\t\t\t\t\tWHERE\n" +
            "\t\t\t\t\t\t\tc.pid = td.id\n" +
            "\t\t\t\t) SELECT\n" +
            "\t\t\t\t\t*\n" +
            "\t\t\t\tFROM\n" +
            "\t\t\t\t\ttd\n" +
            "\t\t\t) tb1\n" +
            "\t\tWHERE\n" +
            "\t\t\ttb1.id != #{id}\n" +
            "\t)")
    List<TestTeam> getIdsByTeamId(@Param("id") Long id);

    /**
     * 查询所有部门id
     */
    List<TeamTreeStructureEntity> getDeptAll();

    /**
     * 通过mysql 寻找顶级部门 为空 则就是顶级部门
     */
    @Select(" SELECT\n" +
            " tb1.id,tb1.pid \n" +
            "FROM\n" +
            " (\n" +
            "  WITH RECURSIVE cte AS (\n" +
            "   SELECT\n" +
            "    a.id,\n" +
            "    a.pid,\n" +
            "    a. NAME\n" +
            "   FROM\n" +
            "    test_team a\n" +
            "   WHERE\n" +
            "    a.id = #{deptId}\n" +
            "   UNION ALL\n" +
            "    SELECT\n" +
            "     k.id,\n" +
            "     k.pid,\n" +
            "     k. NAME\n" +
            "    FROM\n" +
            "     test_team k\n" +
            "    INNER JOIN cte c ON c.pid = k.id\n" +
            "  ) SELECT\n" +
            "   id,\n" +
            "   NAME,\n" +
            "   pid\n" +
            "  FROM\n" +
            "   cte\n" +
            " ) tb1\n" +
            "WHERE\n" +
            " tb1.id != #{deptId}")
    List<TestTeam> getTopDepartment(Long deptId);

    /**
     *
     * @param deptId
     * @return
     */
    @Select("SELECT \n" +
            "  tb1.CODE \n" +
            "FROM \n" +
            "  (\n" +
            "    WITH RECURSIVE cte AS (\n" +
            "      SELECT \n" +
            "        a.id, \n" +
            "        a.pid, \n" +
            "        a.CODE, \n" +
            "        a.NAME \n" +
            "      FROM \n" +
            "        test_team a \n" +
            "      WHERE \n" +
            "        a.id = #{deptId} \n" +
            "      UNION ALL \n" +
            "      SELECT \n" +
            "        k.id, \n" +
            "        k.pid, \n" +
            "        k.CODE, \n" +
            "        k.NAME \n" +
            "      FROM \n" +
            "        test_team k \n" +
            "        INNER JOIN cte c ON c.pid = k.id\n" +
            "    ) \n" +
            "    SELECT \n" +
            "      id, \n" +
            "      NAME, \n" +
            "      CODE, \n" +
            "      pid \n" +
            "    FROM \n" +
            "      cte\n" +
            "  ) tb1 \n" +
            "WHERE \n" +
            "  tb1.id != #{deptId}")
    String getTopDepartmentCode(Long deptId);

    /**
     * 该团队存在团队检测项 ： 检测项 与所属部门验证。 存在 或不存在
     */
    List<TestCheckItemTeamRel> getDepartmentList(@Param("deptIds")List<Long> deptIds);

    /**
     * 获取用户的团队ID
     * @param userId
     * @return
     */
    Long getTeamIdByUserId(Long userId);

    /**
     * 获取部门名称
     */
    @Select("SELECT name FROM test_team WHERE id = #{deptId} LIMIT 1")
    String getTeamIdByName(@Param("deptId") Integer deptId);
}
