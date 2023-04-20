package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestProductItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    List<Long> getItemsByTemplateLikeUrl(@Param("url") String url);

    @Select({"<script>",
            " SELECT ",
            " coordinate",
            " FROM test_product_item WHERE check_item_id in ",
            "<foreach item='item' index='index' collection='items' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<String> getContentByIds(@Param("items") List<Long> items);

    /**
     * 通过检测项主键 获取样品是否关联产品Excel附件
     * @param itemId
     * @return
     */
    @Select("SELECT\n" +
            "\tt1.product_excel_url \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_details_rel AS t1\n" +
            "\tLEFT JOIN test_entrusted_sample_checkitem_rel AS t2 ON t1.sample_id = t2.sample_id \n" +
            "WHERE\n" +
            "\tt2.id = #{itemId}")
    String getProductExcelUrl(@Param("itemId") Integer itemId);
}

