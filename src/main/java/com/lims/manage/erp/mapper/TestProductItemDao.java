package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.ItemSheetRelEntity;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.vo.ExcelInsertVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
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

    /**
     * 更新产品excel附件
     * @param entrustmentId
     * @param sampleId
     * @param url
     * @return
     */
    @Update("UPDATE test_entrusted_sample_details_rel \n" +
            "SET product_excel_url = #{url} \n" +
            "WHERE\n" +
            "\tsample_id = #{sampleId} \n" +
            "\tAND entrustment_id = #{entrustmentId}")
    int updateProductExcelUrl(@Param("entrustmentId") Long entrustmentId,@Param("sampleId") Integer sampleId,@Param("url") String url);

    /**
     * 获取原生的 产品Excel Url
     * @param itemId
     * @return
     */
    @Select("SELECT \n" +
            "t5.url\n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel AS t1\n" +
            "\tLEFT JOIN test_sample AS t2 ON t1.sample_id = t2.id\n" +
            "\tLEFT JOIN product_report_original_rel as t4 ON t4.product_id = t2.product_id\n" +
            "\tLEFT JOIN test_report_original_template as t5 ON t4.report_original_id = t5.id\n" +
            "WHERE"+
            "\tt1.id = #{itemId} LIMIT 1")
    String getProductExcel(@Param("itemId") Integer itemId);

    /**
     * 通过检测项主键 产品Excel及产品附件
     * @param itemId
     * @return
     */
    @Select("SELECT\n" +
            "\tt1.report_edit_url as reportEditUrl, \n" +
            "\tt1.product_excel_url as productExcelUrl \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_details_rel AS t1\n" +
            "\tLEFT JOIN test_entrusted_sample_checkitem_rel AS t2 ON t1.sample_id = t2.sample_id \n" +
            "WHERE\n" +
            "\tt2.id = #{itemId}")
    ExcelInsertVo getExcelUrl(@Param("itemId") Integer itemId);

    /**
     * 更新报告excel附件
     * @param entrustmentId
     * @param sampleId
     * @param url
     * @return
     */
    @Update("UPDATE test_entrusted_sample_details_rel \n" +
            "SET report_edit_url = #{url} \n" +
            "WHERE\n" +
            "\tsample_id = #{sampleId} \n" +
            "\tAND entrustment_id = #{entrustmentId}")
    int updateReportExcelUrl(@Param("entrustmentId") Long entrustmentId,@Param("sampleId") Integer sampleId,@Param("url") String url);

    /**
     * 查询产品报告模板的filename
     * @param productId
     * @return
     */
    String getProductFileName(Integer productId);

    /**
     * 检测项已经绑定的报告模板sheet下标
     * @param checkItemId
     * @return
     */
    List<Integer> getSheetIndex(Integer checkItemId);

    /**
     * 批量插入检测项和模板原始记录下标
     * @param items
     * @return
     */
    int addItemSheetRel(@Param("items") List<ItemSheetRelEntity> items);

    /**
     * 删除检测项与原始记录旧关系
     * @param checkItemId
     * @return
     */
    int deleteItemSheetRel(Integer checkItemId);

    /**
     * 根据检测项主键 查询检测项的sheet下标
     * @param array
     * @return
     */
    List<ExcelInsertVo> selectItemSheetIndex(@Param(value = "array") Integer[] array);

    /**
     * 批量更新检测项数据 set edit_data = 1
     * @param array
     * @return
     */
    int updateBatchItemData(@Param(value = "array") Integer[] array , @Param(value = "testSet") String testSet ,
                            @Param(value = "recordSet") String recordSet);

    /**
     * 查询检测项 实验完成后 生成的pdf
     * @param itemId
     * @return
     */
    @Select("SELECT origin_url_pdf FROM test_entrusted_sample_checkitem_rel WHERE id  = #{itemId}")
    String selectItemOriginUrlPdf(@Param("itemId") Long itemId);

    @Update("update test_entrusted_sample_checkitem_rel set category = '电子章', qys_docment_id = #{documentId} WHERE id  = #{itemId}")
    void updateQysInfo(@Param("itemId") Long itemId, @Param("documentId") Long documentId);

    @Update("<script>" +
            "UPDATE test_entrusted_sample_checkitem_rel SET contract_id=#{contractId} WHERE id IN" +
            " <foreach item='item' collection='list' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    void updateContractIdByCodes(@Param("list") Set<Long> list, @Param("contractId") Long contractId);

    @Update("update test_entrusted_sample_checkitem_rel set sign_url=#{signUrl},qys_state=#{state},sealer=#{sealer},seal_time=#{sealTime} where contract_id=#{contractId}")
    void updateUrlAndStateByContractId(@Param("contractId") Long contractId, @Param("signUrl") String signUrl, @Param("state") String state,
                                       @Param("sealer") String sealer, @Param("sealTime") Date sealTime);

    int updateItemData(ExcelInsertVo excelInsertVo);

    /**
     * 查询检测项 详情数据
     * @param itemId
     * @return
     */
    ExcelInsertVo selectCheckDetails(@Param("itemId") Integer itemId);

    /**
     * 查询检测项 数组
     * @param array
     * @return
     */
    List<ExcelInsertVo> selectCheckList(@Param(value = "array") Long[] array);

    /**
     * 根据报告主键 获取原始记录主键列表
     *
     */
    @Select("SELECT t2.id FROM test_report_record as t1 \n" +
            "LEFT JOIN test_entrusted_sample_checkitem_rel as t2 ON t1.entrustment_id = t2.entrust_id\n" +
            "WHERE t1.id = #{recordId}\n" +
            "UNION ALL\n" +
            "SELECT t2.id FROM test_report_record_mid as t1 \n" +
            "LEFT JOIN test_entrusted_sample_checkitem_rel as t2 ON t1.entrust_id = t2.entrust_id\n" +
            "WHERE t1.id = #{recordId}\n" +
            "GROUP BY t2.id")
    List<Integer> selectGROUPBYItemId(@Param("recordId") Long recordId);

    /**
     * 通过检测项 获取 委托单下 所有样品id数据
     * @param itemId
     * @return
     */
    @Select("\t\t\t\tSELECT \n" +
            "\t\t\t\tt1.sample_id\n" +
            "\t\t\t\tFROM \n" +
            "\t\t\t\ttest_entrusted_sample_details_rel as t1 LEFT JOIN \n" +
            "\t\t\t\ttest_entrusted_sample_checkitem_rel as t2 ON t1.entrustment_id = t2.entrust_id \n" +
            "\t\t\t\tWHERE t2.id = #{itemId}\n" +
            "\t\t\t\tORDER BY t1.id asc;")
    List<Integer> selectCountSampleIds(@Param("itemId") Integer itemId);

    /**
     * 通过任务单id 获取检测项数据
     * @param taskId
     * @return
     */
    @Select("SELECT\n" +
            "\tid as itemId, \n" +
            "\tsample_id as sampleId \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel \n" +
            "WHERE\n" +
            "\ttask_id = #{taskId}")
    List<ExcelInsertVo> selectTaskIdItems(@Param("taskId") Long taskId);

}

