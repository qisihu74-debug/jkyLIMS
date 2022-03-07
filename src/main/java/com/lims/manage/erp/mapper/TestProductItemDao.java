package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestProductItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品检测项(TestProductItem)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-02 15:14:49
 */
public interface TestProductItemDao extends BaseMapper<TestProductItem> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestProductItem> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestProductItem> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestProductItem> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestProductItem> entities);

}

