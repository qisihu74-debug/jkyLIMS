package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestProductItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

    /**
     * 获取检测项下的子检测项
     * @param ids
     * @return
     */
    List<Long> getChirldsByIds(@Param("ids") Set<Long> ids);

    /**
     * 根据报告模板url获取该报告模板中检测项ids
     * @param url
     * @return
     */
    @Select("SELECT DISTINCT \n" +
            "\tpt.check_item_id \n" +
            "FROM\n" +
            "\ttest_report_template rt\n" +
            "\tLEFT JOIN test_product_item pt ON rt.id = pt.report_model_id \n" +
            "WHERE rt.del_flag=0 and pt.del_flag=0 and  \n" +
            "\trt.report_file_uri = #{url}")
    List<Long> getItemsByTemplateUrl(@Param("url") String url);
}

