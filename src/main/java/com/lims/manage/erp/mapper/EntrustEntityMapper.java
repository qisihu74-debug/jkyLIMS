package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.HistoryEntrustDataVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface EntrustEntityMapper extends BaseMapper {
    /**
     * 获取最大委托单编号
     * @return
     */
    Integer selectMaxNo();

    EntrustAddVo selectByKeyId(Long id);

    /**
     *
     * @param id
     * @return
     */
    String getEntrustTestType(Long id);

    /**
     * 通过委托编号 获取委托单id 是否存在
     * @param code
     * @return
     */
    EntrustAddVo getByData(Integer code);



    /**
     * 保存委托单下样品信息
     * @param list
     */
    void BatchSaveEntrustSample(List<EntrustSampleEntity> list);

    /**
     * 保存委托单样品，判定依据信息
     * @param list1
     */
    void BatchSaveSampleStandard(@Param("list1") List<EntrustSampleEntity> list1);

    /**
     * 通过委托单id 获取样品依据信息集合
     * @param entrustmentId
     * @return
     */
    @Select("SELECT\n" +
            "t2.code as judgmentBasis\n" +
            "FROM\n" +
            "test_entrusted_sample_standard_rel as t1 \n" +
            "LEFT JOIN test_standard_file as t2 ON t1.standard_id = t2.id\n" +
            "WHERE t1.entrustment_id = #{entrustmentId} and t2.code is not null\n")
    List<String> getSampleStandard(Long entrustmentId);

    /**
     * 保存委托样品下检测项信息
     * @param sampleItemList
     */
    void BatchSaveEntrustSampleItem(@Param("sampleItemList") List<SampleItemEntity> sampleItemList);

    /**
     * 缴费记录新增
     * @param pamentEntity
     */
    void saveEntrustPayRecord(EntrustPamentEntity pamentEntity);

    /**
     * 委托历史 查询。
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity);

    /**
     * 历史委托查询 state不为0 state不为144
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryListRelease_of(EntrustHistoryEntity entrustHistoryEntity);
    /**
     * 委托单任务待发布列表
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryTaskEntity> selectEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity);

    /**
     * 新增委托信息
     * @param basisInfo
     */
    void insertEntrustInfo(EntrustEntity basisInfo);

    /**
     * 通过单位和类型以及联系人和手机号  查看联系人是否存在
     * @param testCompanyJsonEntity
     * @return
     */
    String GetDelegateInformation(TestCompanyJsonEntity testCompanyJsonEntity);

    /**
     * 修改委托信息
     * @param basisInfo
     * @return
     */
    int updateEntrustInfo(EntrustEntity basisInfo);

    /**
     * 根据委托编号 获取 存储的样品id 集合
     */
    List<Integer> getSampleIdSet(Long entrustmentId);

    /**
     * 通过委托单id 获取缴费记录
     */
    String getTestEntrustedPaymentRecordInfoPrice(Long entrustmentId);
    /**
     *  通过委托单id 支付方式
     */
    String getTestEntrustedInfoMethodName(Long entrustmentId);
    /**
     * 通过委托单id 获取联系地址
     */
    String getEntrustingParty(Long entrustmentId);

    /**
     * 查询样品id 是否存在
     * @param entrustmentId
     * @return
     */
    @Select("SELECT count(*) FROM test_entrusted_sample_details_rel WHERE entrustment_id= #{entrustmentId}")
    Integer countSampleDetailsRel(Long entrustmentId);
    /**
     * 删除样品id
     */
    int removeTestEntrustedSampleDetailsRel(Long entrustmentId);

    /**
     * 查询依据id 是否存在
     * @param entrustmentId
     * @return
     */
    @Select("SELECT count(*) FROM test_entrusted_sample_standard_rel WHERE entrustment_id = #{entrustmentId}")
    Integer countSampleStandardRel(Long entrustmentId);
    /**
     * 删除判定依据id
     */
    int removeTestEntrustedSampleStandardRel(Long entrustmentId);
    /**
     * 删除缴费
     */
    int removeTestEntrustedPaymentRecordInfo(Long entrustmentId);

    /**
     * 判断样品下检测依据
     * @param entrustmentId
     * @return
     */
    @Select("SELECT count(*) FROM test_entrusted_sample_checkitem_rel WHERE  entrust_id = #{entrustmentId}")
    Integer countSampleCheckitemRel(Long entrustmentId);
    /**
     * 样品下检测依据
     */
    int removeTestEntrustedSampleCheckitemRel(Long entrustmentId);


    /**
     * 获取产品下的标准文件名称
     * @param productId
     * @return
     */
    List<String> getStatndardByPId(Integer productId);

    /**
     * 查询委托下的样品ID
     * @param entrustmentId
     * @return
     */
    List<Integer> getSampleId(Long entrustmentId);

    /**
     * 查询委托单位上一次工程名称、工程部位
     * @param name
     * @return
     */
    HistoryEntrustDataVo getHistoryData(String name);


    /**
     * 通过单位 和 类型 返回联系人和联系电话
     * @param name
     * @param type
     * @return
     */
    List<TestCompanyJsonEntity> getCompanyJsonEntityList(@Param(value = "name") String name, @Param(value = "type")Integer type);

    TestCompanyJsonEntity getCompanyJsonEntitydata(@Param(value = "name") String name, @Param(value = "type")Integer type);

    /**
     * 通过单位名称和类型 获取主键
     * @param name
     * @param type
     * @return
     */
    @Select("\tSELECT\n" +
            "\tcompany_id\n" +
            "\tFROM\n" +
            "\ttest_company\n" +
            "\tWHERE\n" +
            "\tcompany_name = #{name} \n" +
            "\tAND type = #{type} \n" +
            "\tLIMIT 1")
    Integer getCompanyId(@Param(value = "name") String name, @Param(value = "type")Integer type);

    /**
     * 通过委托单位id 和type 获取公司名称。
     * @param companyId
     * @param type
     * @return
     */
    @Select("SELECT\n" +
            "company_name\n" +
            "FROM\n" +
            "test_company\n" +
            "WHERE\n" +
            "company_id = #{companyId}\n" +
            "AND type = #{type}\n" +
            "LIMIT 1")
    String getCompanyNameId(@Param(value = "companyId") Integer companyId, @Param(value = "type")Integer type);

    /**
     * 根据检测项ID查询可以做的团队
     * @param checkItemId
     * @return
     */
    List<LabelValueVo> getDept(Integer checkItemId);


    /**
     * 进行效验单位是否存在
     * @param companyName
     * @param type
     * @return
     */
    @Select("SELECT\n" +
            "company_name\n" +
            "FROM\n" +
            "test_company\n" +
            "WHERE company_name = #{companyName} and type = #{type} LIMIT 1")
    String getCompanyName(String companyName,Integer type);

    /**
     * 遍历 获取检测项id 和价格。
     *
     */
    @Select("SELECT\n" +
            "\tc.check_item_id,\n" +
            "\tc.check_price as unitPrice \n" +
            "FROM\n" +
            "\t(\n" +
            "\t\tSELECT\n" +
            "\t\t\ta.check_item_id,\n" +
            "\t\t\ta.check_price,\n" +
            "\t\tIF (\n" +
            "\t\t\tFIND_IN_SET(a.check_item_pid ,@pids) > 0,\n" +
            "\n" +
            "\t\tIF (\n" +
            "\t\t\tlength(@pids) - length(\n" +
            "\t\t\t\tREPLACE (@pids, a.check_item_pid, '')\n" +
            "\t\t\t) > 1,\n" +
            "\n" +
            "\t\tIF (\n" +
            "\t\t\tlength(@pids) - length(REPLACE(@pids, a.check_item_id, '')) > 1 ,@pids ,@pids := concat(@pids, ',', a.check_item_id)\n" +
            "\t\t) ,@pids := concat(@pids, ',', a.check_item_id)\n" +
            "\t\t),\n" +
            "\t\t0\n" +
            "\t\t) AS 'plist',\n" +
            "\n" +
            "\tIF (\n" +
            "\t\tFIND_IN_SET(a.check_item_id ,@pids) > 0,\n" +
            "\t\t@pids,\n" +
            "\t\t0\n" +
            "\t) AS ischild\n" +
            "\tFROM\n" +
            "\t\t(\n" +
            "\t\t\tSELECT\n" +
            "\t\t\t  r.check_price,\n" +
            "\t\t\t\tr.check_item_id,\n" +
            "\t\t\t\tr.check_item_pid\n" +
            "\t\t\tFROM\n" +
            "\t\t\t\ttest_product_item r\n" +
            "\t\t) a,\n" +
            "\t\t(SELECT @pids := #{checkItemId}) b\n" +
            "\t) c\n" +
            "WHERE\n" +
            "\tc.ischild != 0 ")
    List<SampleItemEntity> getyItemList(Long checkItemId);

    /**
     * 判断样品是否存在
     * @param sampleId
     * @return
     */
    @Select("select id from test_sample where id = #{sampleId}")
    Long getMesBySampleId(int sampleId);

    @Select("select entrustment_id from test_entrusted_sample_details_rel where sample_id = #{sampleId}")
    Long getEntrustIdBySampleId(int sampleId);

    @Select("select name from test_init_data where type=12")
    String getMessage();
}