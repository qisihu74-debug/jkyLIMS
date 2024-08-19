package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.entity.HourCount;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.vo.Node;
import com.lims.manage.erp.vo.TaskStatisticsVo;
import com.lims.manage.erp.vo.TaskStatsVo;
import com.lims.manage.erp.vo.TestTeamVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队管理(TestTeamVo)表数据库访问层
 *
 * @author makejava
 * @since 2022-02-23 09:14:46
 */
public interface TestTeamDao extends BaseMapper<TestTeam> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestTeamVo> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestTeam> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestTeamVo> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestTeam> entities);

IPage<TestTeamVo> getListPage(IPage<TestTeamVo> page, @Param(Constants.WRAPPER) Wrapper<TestTeam> queryWrapper);

    /**
     * 获取所有团队信息
     * @return
     */
    @Select("SELECT\n" +
            "\tid,\n" +
            "\tpid,\n" +
            "NAME \n" +
            "FROM\n" +
            "\ttest_team \n" +
            "WHERE\n" +
            "\tdel_flag = 0 \n" +
            "ORDER BY\n" +
            "\tsort")
    List<Node> getTree();

    /**
     * 展示 人员信息及部门信息
     *
     * @return
     */
    List<TaskStatisticsVo> getEmployeesAndDepartments(TaskStatisticsVo taskStatisticsVo);


    /**
     * 展示 根据授权签字（role_id = 66） 带出用户信息
     *
     * @return
     */
    List<TaskStatisticsVo> getRoleUserInformation(TaskStatisticsVo taskStatisticsVo);

//    @Select({"<script>",
//            " SELECT DISTINCT ",
//            " id,name",
//            " FROM test_team WHERE id in ",
//            "<foreach item='item' index='index' collection='items' open='(' separator=',' close=')'>",
//            "#{item.pid}",
//            "</foreach>",
//            "</script>"})
//    List<TestTeam> getTeamsByPids(@Param("items") List<HourCount> items);
}

