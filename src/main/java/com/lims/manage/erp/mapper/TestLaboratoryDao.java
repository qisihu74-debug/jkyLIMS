package com.lims.manage.erp.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import org.apache.ibatis.annotations.Param;
import com.lims.manage.erp.entity.TestLaboratory;

/**
 * 实验室管理(TestLaboratory)表数据库访问层
 *
 * @author makejava
 * @since 2022-02-25 10:08:36
 */
public interface TestLaboratoryDao extends BaseMapper<TestLaboratory> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestLaboratory> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestLaboratory> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestLaboratory> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestLaboratory> entities);

IPage<TestLaboratoryVo> getListPage(IPage<TestLaboratoryVo> page,@Param(Constants.WRAPPER) Wrapper<TestLaboratory> queryWrapper);

}

