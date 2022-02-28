package com.lims.manage.erp.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import com.lims.manage.erp.entity.TestTeam;

/**
 * 团队管理(TestTeam)表数据库访问层
 *
 * @author makejava
 * @since 2022-02-23 09:14:46
 */
public interface TestTeamDao extends BaseMapper<TestTeam> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestTeam> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestTeam> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestTeam> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestTeam> entities);

IPage<TestTeam> selectPage(TestTeam testTeam);

}

