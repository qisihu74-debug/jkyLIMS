package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestProductType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品分类(TestProductType)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-02 10:03:09
 */
public interface TestProductTypeDao extends BaseMapper<TestProductType> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestProductType> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestProductType> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestProductType> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestProductType> entities);

}

