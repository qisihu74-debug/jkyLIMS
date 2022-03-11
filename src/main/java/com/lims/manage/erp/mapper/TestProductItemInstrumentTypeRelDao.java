package com.lims.manage.erp.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.lims.manage.erp.entity.TestProductItemInstrumentTypeRel;

/**
 * 检测项使用的设备(TestProductItemInstrumentTypeRel)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-08 17:13:36
 */
public interface TestProductItemInstrumentTypeRelDao extends BaseMapper<TestProductItemInstrumentTypeRel> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestProductItemInstrumentTypeRel> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestProductItemInstrumentTypeRel> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestProductItemInstrumentTypeRel> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestProductItemInstrumentTypeRel> entities);

}

