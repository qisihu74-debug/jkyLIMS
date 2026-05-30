package com.lims.manage.erp.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestInstrumentAppraisalRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备仪器检定记录表(TestInstrumentAppraisalRecord)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-01 11:45:04
 */
public interface TestInstrumentAppraisalRecordDao extends BaseMapper<TestInstrumentAppraisalRecord> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestInstrumentAppraisalRecord> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestInstrumentAppraisalRecord> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestInstrumentAppraisalRecord> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestInstrumentAppraisalRecord> entities);

}

