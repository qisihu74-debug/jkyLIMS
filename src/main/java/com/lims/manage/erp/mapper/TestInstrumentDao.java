package com.lims.manage.erp.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.vo.TestInstrumentVo;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import org.apache.ibatis.annotations.Param;
import com.lims.manage.erp.entity.TestInstrument;

/**
 * 仪器设备(TestInstrument)表数据库访问层
 *
 * @author makejava
 * @since 2022-02-25 10:05:48
 */
public interface TestInstrumentDao extends BaseMapper<TestInstrument> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestInstrument> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestInstrument> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestInstrument> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestInstrument> entities);


IPage<TestInstrumentVo> getPageList(IPage<TestInstrumentVo> page, @Param(Constants.WRAPPER) Wrapper<TestInstrument> queryWrapper);
}

