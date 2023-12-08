package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestReportTemplate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * (TestReportTemplate)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-02 16:22:06
 */
public interface TestReportTemplateDao extends BaseMapper<TestReportTemplate> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<TestReportTemplate> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<TestReportTemplate> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<TestReportTemplate> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<TestReportTemplate> entities);

    String getTypeByUrl(@Param("url") String url);

    @Select("select report_name from test_report_template where id=#{reportModelId}")
    String getNameById(@Param("reportModelId") Integer reportModelId);

    @Select("select IFNULL(MAX(id),0)+1 from test_report_template")
    Integer getMaxId();

    TestReportTemplate getDetail(Integer id);

    int insertRecord(TestReportTemplate reportTemplate);

    int deleteById(Integer id);

    List<TestReportTemplate> getRecordList(Integer pid);
}

