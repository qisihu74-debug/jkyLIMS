package com.lims.manage.erp.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestInstrumentType;
import org.apache.ibatis.annotations.Param;

/**
 * 仪器大类(TestInstrumentType)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-01 09:14:39
 */
public interface TestInstrumentTypeDao extends BaseMapper<TestInstrumentType> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestInstrumentType> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestInstrumentType> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestInstrumentType> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestInstrumentType> entities);

}

