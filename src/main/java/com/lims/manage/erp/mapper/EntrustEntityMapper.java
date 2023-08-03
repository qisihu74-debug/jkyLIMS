package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustHistoryTaskEntity;
import com.lims.manage.erp.entity.EntrustPamentEntity;
import com.lims.manage.erp.entity.EntrustSampleEntity;
import com.lims.manage.erp.entity.ReportEditReq;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.ClientOrderdetailVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.EntrustCategoryVo;
import com.lims.manage.erp.vo.EntrustSampleInfoVo;
import com.lims.manage.erp.vo.HistoryEntrustDataVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TaskCodeVo;
import com.lims.manage.erp.vo.TaskPriceVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
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
    @Select("SELECT DISTINCT\n" +
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
     * 批量更新委托单下的检测项
     * @param list
     */
    void batchUpdateEntrustSampleItem(@Param("list") List<SampleItemEntity> list);

    /**
     * 批量修改任务单价格
     * @param list
     */
    void batchUpdateTaskPrice(@Param("list") List<TaskPriceVo> list);

    /**
     * 批量删除委托单下的检测项
     * @param list
     */
    void batchDeleteEntrustSampleItem(@Param("list") List<SampleItemEntity> list);

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
     * 委托历史 查询--加任务编号。
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustTaskHistoryList(EntrustHistoryEntity entrustHistoryEntity);

    /**
     * 历史委托查询 state不为0 state不为144
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryListRelease_of(EntrustHistoryEntity entrustHistoryEntity);
    /**
     * 历史委托查询 state不为0 state不为14--加任务编号
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryEntity> selectEntrustHistoryTaskListRelease_of(EntrustHistoryEntity entrustHistoryEntity);
    /**
     * 委托单任务待发布列表
     * @param entrustHistoryEntity
     * @return
     */
    List<EntrustHistoryTaskEntity> selectEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity);

    /**
     * 任务发布列表的样品信息
     * @param entrustId
     * @return
     */
    List<EntrustSampleInfoVo> getEntrustSampleInfos(Long entrustId);

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
            "\tc.check_price as unitPrice,c.check_item_name \n" +
            "FROM\n" +
            "\t(\n" +
            "\t\tSELECT\n" +
            "\t\t\ta.check_item_id,\n" +
            "\t\t\ta.check_price,a.check_item_name,\n" +
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
            "\t\t\t\tr.check_item_pid,r.check_item_name\n" +
            "\t\t\tFROM\n" +
            "\t\t\t\ttest_product_item r\n" +
            "\t\t) a,\n" +
            "\t\t(SELECT @pids := #{checkItemId}) b\n" +
            "\t) c\n" +
            "WHERE\n" +
            "\tc.ischild != 0 ")
    List<SampleItemEntity> getyItemList(Long checkItemId);

    /**
     * 通过检测项主键递归查询。
     * @param checkItemId
     * @return
     */
    List<SampleItemEntity> getItemRecursionList(Long checkItemId);

    /**
     * 判断样品是否存在
     * @param sampleId
     * @return
     */
    @Select("select id from test_sample where id = #{sampleId}")
    Long getMesBySampleId(int sampleId);

    @Select("select entrustment_id from test_entrusted_sample_details_rel where sample_id = #{sampleId} LIMIT 1")
    Long getEntrustIdBySampleId(int sampleId);

    @Select("select name from test_init_data where type=12")
    String getMessage();

    @Select("select entrustment_id from test_report_record where report_code=#{reportCode}")
    Long getEntrustIdByCode(@Param("reportCode") String reportCode);

    @Select("select cast(sum(price) AS decimal(11,2)) from test_entrusted_payment_record_info where entrustment_id=#{entrustmentId}")
    String getRecordCountById(@Param("entrustmentId") Long entrustmentId);

    List<String> getTaskCode(Long id);

    /**
     * 查询当前可出报告科室
     * @param entrustmentId
     * @return
     */
    List<LabelValueVo> getReportTeams(Long entrustmentId);

    /**
     * 统计未分配委托单
     * @param state
     * @return
     */
    Integer selectCountUnallocated(@Param(value = "state") Integer state,@Param(value = "deptIds") List<Long> deptIds);

    /**
     * 查询该委托下所有的样品id
     * @param entrustmentId
     * @return
     */
    List<Integer> getAllSampleIdentrustmentId(Long entrustmentId);

    @Select("select report_edit_url from test_entrusted_sample_details_rel where entrustment_id=#{entrustmentId} and completion_status=1")
    List<String> getAllReportEditUrlByEntrustId(@Param("entrustmentId") Long entrustmentId);

    /**
     * 修改委托下样品委托单位
     * @return
     */
    int updateSampleCompany(@Param("list") List<TestSampleEntity> list);

    /**
     * 查询委托单当前状态
     * @param entrustmentId
     * @return
     */
    Integer getEntrustStateNow(Long entrustmentId);

    /**
     * 查询委托单下样品的检测项信息
     * @param sampleId
     * @param entrustId
     * @return
     */
    List<SampleItemEntity> getAllOldCheckItemInfo(@Param("sampleId") Integer sampleId,@Param("entrustId")Long entrustId);

    /**
     * 查询当前委托的报告状态
     * @param entrustmentId
     * @return
     */
    String getReportState(Long entrustmentId);

    /**
     * 查询当前委托的任务数量
     * @param entrustmentId
     * @return
     */
    Integer getReportStateTaskNum(Long entrustmentId);

    /**
     * 查询当前委托报告数据主键ID
     * @param entrustmentId
     * @return
     */
    Long getReportId(Long entrustmentId);

    /**
     * 删除委托单下 样品与检测项关联的 详情id
     * @param id
     * @return
     */
    Integer deleteEntrustedSampleCheckitemRel(@Param("id") Integer id);

    @Select("select id from test_entrusted_info where id=#{entrustId}")
    Long checkEntrustId(@Param("entrustId") Long entrustId);

    /**
     * 根据委托单id和样品id获取待出报告的参数
     * @param entrustId
     * @param sampleId
     */
    @Select("SELECT DISTINCT\n" +
            "\trrd.check_item_id,rrd.check_item_name,rrd.sample_id,rrd.coordinate,rrd.check_result,rrd.specs_content,rrd.judge_result\n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel escr\n" +
            "LEFT JOIN test_report_record_detail rrd ON escr.task_id = rrd.task_id\n" +
            "WHERE\n" +
            "\tescr.entrust_id = #{entrustId}\n" +
            "AND escr.sample_id = #{sampleId} And escr.sample_id=rrd.sample_id")
    List<ReportRecordDetailEntity> getItemIdByEntrustIdAndSampleId(@Param("entrustId") Long entrustId, @Param("sampleId") int sampleId);

    /**
     * 通过委托单id 查询任务单信息
     */
    List<TaskTestEntity> selectTaskTestEntityList(@Param("entrustmentId") Long entrustmentId);

    /**
     * 客户委托详情
     */
    List<ClientOrderdetailVo> selectClientOrderdetailVoList(ClientOrderdetailVo clientOrderdetailVo);

    /**
     * 通过委托单id集合 获取委托单位信息。
     */
    List<TestCompanyJsonEntity> getCompanyList(@Param("companyIds") Integer[] companyIds);

    /**
     * 查询委托下的任务和团队信息
     * @param entrustId
     * @return
     */
    List<TaskCodeVo> getTaskAndTeam(Long entrustId);
//    测试信息： 获取委托列表
    List<ClientOrderdetailVo> getEntrustList(ClientOrderdetailVo clientOrderdetailVo);
    // 获取样品信息
    List<SampleEntity> getSampleList(@Param("entrustIds") List<Long> entrustIds);
    // 获取任务单列表
    List<TaskEntity> getTaskList(@Param("entrustIds") List<Long> entrustIds);
    // 获取报告列表
    List<ReportRecordEntity> getReportRecordList(@Param("entrustIds") List<Long> entrustIds);
    // 获取检测项列表
    List<SampleItemEntity> getSampleItemList(@Param("entrustIds") List<Long> entrustIds);

    @Update("update test_entrusted_info set audit_state=1,audit_date=#{date},audit_user=#{name} where id = #{id}")
    void acceptEntrust(@Param("id") Long id, @Param("date") Date date,@Param("name") String name);

    /**
     * 查询委托单是否客户委托
     * @param entrustId
     * @return
     */
    @Select("SELECT admin_id FROM `test_entrusted_info` WHERE id = #{entrustId}")
    String selectEntrustClientStatus(@Param("entrustId")Long entrustId);

    /**
     * 修改委托信息适用于 判断值非空
     * @param basisInfo
     * @return
     */
    int updateEntrustInfos(EntrustEntity basisInfo);

    /**
     * 效验委托单受理状态
     * @param entrustId
     * @return 1 已受理、0 未受理
     */
    @Select("SELECT audit_state FROM test_entrusted_info WHERE id = #{entrustId} LIMIT 1")
    Integer selectEntustAuditState(@Param("entrustId") Long entrustId);

    /**
     * 根据委托单位 返回报告人员信息：
     * @param entrustCompany
     * @return
     */
    HistoryEntrustDataVo getContactWayData(@Param("entrustCompany") String entrustCompany);

    /**
     * 返回收报告单位
     * @param entrustCompany
     * @return
     */
    String getReportReceivingUnit(@Param("entrustCompany") String entrustCompany);

    /**
     * 获取最大委托单编号 根据类型进行过滤
     * @return
     */
    EntrustCategoryVo selectEntrustMaxNo(@Param("categoryType") String categoryType,@Param("acceptanceDate") String acceptanceDate);

    /**
     * 通过委托编号 获取委托单id、根据类型进行过滤： 是否存在
     * @param code
     * @return
     */
    EntrustAddVo getByDataEntrustMaxNo(@Param("code")Integer code,@Param("categoryType") String categoryType);

    @Select("select DISTINCT id As id,entrustment_no As entrustmentNo,state As state from test_entrusted_info WHERE state != 144")
    List<EntrustAddVo> getAllEntrustIdBySearch();

    @Select("select DISTINCT id As id,entrustment_no As entrustmentNo,state As state from test_entrusted_info WHERE state != 144 and state=1 and entrustment_no > 2023010000")
    List<EntrustAddVo> getPublishEntrustIdBySearch();

    /**
     * 查询顶级部门
     * @return
     */
    @Select("SELECT\n" +
            "\tid as value,\n" +
            "\tname as label\n" +
            "FROM\n" +
            "\ttest_team \n" +
            "WHERE\n" +
            "\tpid = 0 \n" +
            "\tAND del_flag =0")
    List<LabelValueVo> getIssueDept();

    /**
     * 查询部门名称
     * @param deptId
     * @return
     */
    @Select("SELECT\n" +
            "\tid as value,\n" +
            "\tname as label\n" +
            "FROM\n" +
            "\ttest_team \n" +
            "WHERE\n" +
            "\tid = #{deptId}\n" +
            "LIMIT 1\t\n" +
            "\t")
    LabelValueVo getIssueDeptById(@Param("deptId") Integer deptId);

    /**
     * 删除样品流转状态 =0 根据样品id
     * @param sampleId
     * @return
     */
    @Delete("DELETE FROM test_sample_circulation_record WHERE sample_id = #{sampleId} and  status =0")
    int deleteTestSampleCirculationRecordById(@Param("sampleId") Integer sampleId);

    @Select("SELECT DISTINCT\n" +
            "\tt1.id AS id,\n" +
            "\tt1.entrustment_no AS entrustmentNo,\n" +
            "\tt1.state AS state,\n" +
            "t3.product_name\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_entrusted_sample_details_rel t2 ON t1.id=t2.entrustment_id\n" +
            "LEFT JOIN test_sample t5 ON t2.sample_id=t5.id\n" +
            "LEFT JOIN test_product t3 ON t5.product_id=t3.product_id\n" +
            "LEFT JOIN test_task t4 ON t1.id=t4.entrustment_id\n" +
            "WHERE\n" +
            "\tt1.state != 144\n" +
            "AND t1.state = 1\n" +
            "AND t3.product_name='土工布（织物类）'\n" +
            "AND 2023030000<t1.entrustment_no AND t1.entrustment_no<2023040000\n" +
            "AND t4.dept_id IN(231,\n" +
            "232,\n" +
            "233,\n" +
            "234,\n" +
            "235,\n" +
            "236)\n")
    List<EntrustAddVo> get7Infos();

    @Select("SELECT DISTINCT\n" +
            "\tt1.id AS id,\n" +
            "\tt1.entrustment_no AS entrustmentNo,\n" +
            "\tt1.state AS state,\n" +
            "t3.product_name\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_entrusted_sample_details_rel t2 ON t1.id=t2.entrustment_id\n" +
            "LEFT JOIN test_sample t5 ON t2.sample_id=t5.id\n" +
            "LEFT JOIN test_product t3 ON t5.product_id=t3.product_id\n" +
            "LEFT JOIN test_task t4 ON t1.id=t4.entrustment_id\n" +
            "WHERE\n" +
            "\tt1.state != 144\n" +
            "AND t1.state = 1\n" +
            "AND t3.product_name='突起路标'\n" +
            "AND 2023030000<t1.entrustment_no AND t1.entrustment_no<2023040000\n" +
            "AND t4.dept_id IN(237,\n" +
            "238,\n" +
            "239\n" +
            ")")
    List<EntrustAddVo> get8Infos();

    /**
     * 根据委托单id 获取任务单 列表
     * @param entrustId
     * @return
     */
    @Select("SELECT\n" +
            "\tid \n" +
            "FROM\n" +
            "\ttest_task \n" +
            "WHERE\n" +
            "\tentrustment_id = #{entrustId} and state != 144")
    List<Long> getEntrustTaskIds(@Param("entrustId")Long entrustId);

    /**
     * 通过委托单id 获取检测项与对应任务单id
     */
    @Select("SELECT\n" +
            "\tt2.id as itemId,\n" +
            "\tt2.check_item_name as checkItemName,\n" +
            "\tt2.task_id as taskId\n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel AS t2 \n" +
            "WHERE\n" +
            "\tt2.entrust_id = #{entrustId} and task_id is not null ")
    List<CheckItemInfoVo> getEntrustItemVos(@Param("entrustId")Long entrustId);

    @Select("SELECT\n" +
            "\tt2.acceptance_date \n" +
            "FROM\n" +
            "\ttest_task t1\n" +
            "\tLEFT JOIN test_entrusted_info t2 ON t1.entrustment_id = t2.id \n" +
            "WHERE\n" +
            "\tt1.id=#{taskId}")
    java.sql.Date getEntrustDateByTaskId(@Param("taskId") Long taskId);

    /**
     * 任务发布列表的样品信息
     * @param list
     * @return list中entrustId
     */
    List<EntrustSampleInfoVo> getEntrustSampleInfoIds(@Param("list") List<EntrustHistoryEntity> list);

    @Update("update test_entrusted_sample_details_rel set report_edit_url=#{url},report_type=#{reportType} where entrustment_id=#{entrustId} and sample_id=#{sampleId}")
    void updateUrlByEntrustIdAndSampleId(@Param("entrustId") Long entrustId, @Param("sampleId") Integer sampleId, @Param("url") String url,@Param("reportType") Integer reportType);

    void updateReportTypeAndStatus(@Param("list") List<ReportEditReq> list);

    @Select("SELECT\n" +
            "\tsample_id AS sampleId,\n" +
            "\treport_type AS reportType,\n" +
            "\tcompletion_status AS completionStatus\n" +
            "FROM\n" +
            "\ttest_entrusted_sample_details_rel\n" +
            "WHERE\n" +
            "\tentrustment_id =#{entrustId}")
    List<ReportEditReq> getStatusAndType(@Param("entrustId") Long entrustId);

    @Select("select report_type as reportType,completion_status As completionStatus from test_entrusted_sample_details_rel where entrustment_id=#{entrustId} ")
    List<ReportEditReq> getAllStatusAndTypeByEntrustId(@Param("entrustId") Long entrustId);

    @Select("select DISTINCT sample_id from test_entrusted_sample_checkitem_rel where task_id=#{taskId}")
    List<Integer> getSampelIdsByTaskId(@Param("taskId") Long taskId);

    @Select("select check_item_name from test_entrusted_sample_checkitem_rel where entrust_id=#{entrustId} and sample_id=#{sampleId} and state=3")
    List<String> getAllItems(@Param("entrustId") Long entrustId, @Param("sampleId") Integer sampleId);

    List<TaskCodeVo> getTaskAndTeamByIds(@Param("list") List<Long> list);
}
