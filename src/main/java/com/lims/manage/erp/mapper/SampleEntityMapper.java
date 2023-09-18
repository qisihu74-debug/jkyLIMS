package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.entity.SampleCirculationRecord;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Mapper
public interface SampleEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SampleEntity record);

    int insertSelective(SampleEntity record);

    SampleEntity selectByPrimaryKey(Integer id);

    List<SampleEntity> selectSampleListGroup(Long entrustmentId);

    /**
     * 通过委托单位id 获取样品信息集合。
     */
    List<SampleEntity> selectSampleListObtain(Integer companyId);

    /**
     * 根据样品id 获取配合比下样品集合。
     * @param id
     * @return
     */
    List<SampleEntity> selectByPid(Integer id);

    /**
     * 查询样品信息
     *
     * @param record
     * @return
     */
    List<SampleEntity> selectSampleList(SampleEntity record);

    /**
     * 查询样品编号、名称--按组
     * @param record
     * @return
     */
    List<SampleEntrustAddVo> selectSampleListTop(SampleEntity record);

    /**
     * 新增委托--按照组添加委托样品
     * @param record
     * @return
     */
    List<SampleEntrustAddVo> selectSampleListByGroup(SampleEntity record);

    /**
     * 查询子样品信息
     * @param codes
     * @return
     */
    List<SampleEntity> getGroupNode(@Param("codes") List<String> codes);

    /**
     * 查询样品组基本信息
     *
     * @param insertFlag
     * @return
     */
    SampleDetailVo getSampleGroupInfo(String insertFlag);

    /**
     * 根据查询条件查询样品列表
     *
     * @param paramVo
     * @return
     */
    List<SampleDetailVo> selectSampleList2(SampleEntity paramVo);

    /**
     * 样品下检测依据
     */
    List<JudgmentBasisVo> selectTestStandardList(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    /**
     * 查询未分配的检测项
     * @param sampleId
     * @param entrustmentId
     * @return
     */
    List<JudgmentBasisVo> getCheckItemNoDistribution(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    /**
     * 根据检测项id 查询 默认匹配部门信息
     */
    @Select("SELECT\n" +
            "t1.name\n" +
            "FROM\n" +
            "test_team as t1\n" +
            "LEFT JOIN test_check_item_team_rel as t2 ON t1.id = t2.team_id\n" +
            "WHERE t2.check_item_id = #{checkItemId} ORDER BY t1.sort ASC")
    List<String> getTeamNameStrings(Integer checkItemId);

    List<LabelValueVo> getTestingRoomList(Integer checkItemId);

    /**
     * 查询检测项关联团队信息
     * @param checkItemId
     * @return
     */
    List<TestTeam> getTestingRoomInfoList(Integer checkItemId);


    /**
     * 检测依据信息
     *
     * @param sampleId
     * @return
     */
    List<SampleItemEntity> selectSampleCheckItem(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);


    int updateByPrimaryKeySelective(SampleEntity record);

    int updateByPrimaryKey(SampleEntity record);

    /**
     * 更新样品基础信息
     *
     * @param record
     * @return
     */
    int updateSampleInfo(SampleEntity record);

    /**
     * 更新附件和别名 根据样品id
     * @param record
     * @return
     */
    int updateSampleInfoFileUrl(SampleEntity record);

    /**
     * 查询样品标签信息
     *
     * @param sampleId
     * @return
     */
    SampleDetailVo getSampleTagInfo(Integer sampleId);

    /**
     * 通过pid 查询样品标签信息集合。
     *
     * @param sampleId
     * @return
     */
    List<SampleDetailVo> getSampleTagInfoPidList(Integer sampleId);

    /**
     * 删除test_entrusted_sample_details_rel
     *
     * @return
     */
    int removeSamplesId(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    /**
     * 保存test_entrusted_sample_details_rel
     *
     * @return
     */
    int addSampleEntity(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    List<Integer> getSampleBasisSet(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    List<JudgmentBasisVo> getSampleBasisList(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    /**
     * 查询样品公用信息
     *
     * @param paramVo
     * @return
     */
    List<SamplePublicInfoVo> getSamplePublicInfos(SampleDetailVo paramVo);

    List<SamplePublicInfoVo> getSamplePublicInfos1(SampleDetailVo paramVo);

    List<SamplePrivateInfoVo> getSamplePrivateInfos1(List<String> insertFlag);

    /**
     * 查询当前年份最大样品编号
     *
     * @param year
     * @return
     */
    Integer getMaxNumber(String year);

    /**
     * 查询原始记录模板表头样品信息
     *
     * @param sampleId
     * @return
     */
    TemplateSampleVo getOriginalSampleInfo(Integer sampleId);

    /**
     * 修改样品是否使用
     * @param sampleId
     * @param isUse
     * @return
     */
    @Update("update test_sample set is_use=#{isUse} where id = #{sampleId}")
    void updateSampleUse(@Param("sampleId") Integer sampleId, @Param("isUse") Integer isUse);

    List<ConcreteSampleVo> getSamplesByEntrustID(Long entrustId);

    /**
     * 更新样品状态
     * @param sampleId
     * @param state
     */
    @Update("update test_sample set state=#{state} where id = #{sampleId}")
    void updateSampleState(@Param("sampleId") Integer sampleId, @Param("state") Integer state);

    /**
     * 通过委托单id 获取样品信息集合。
     */
    List<SampleEntity> selectSampleSet(Long entrustmentId);

    /**
     * 查询全部团队信息
     * @return
     */
    List<TestTeam> getAllRoomInfoList();

    List<SampleCirculationRecord> getRecords(@Param("sampleId") Integer sampleId,@Param("type") int type);

    @Select("SELECT DISTINCT\n" +
            "\tt2.sampler\n" +
            "FROM\n" +
            "\ttest_entrusted_sample_details_rel t1\n" +
            "LEFT JOIN test_task t2 ON t1.entrustment_id = t2.entrustment_id\n" +
            "WHERE\n" +
            "\tt1.sample_id =#{sampleId}")
    String getSampler(@Param("sampleId") Integer sampleId);

    @Select("SELECT sample_id\n" +
            "FROM\n" +
            "\ttest_sample_circulation_record\n" +
            "WHERE\n" +
            "\tsample_id = #{sampleId} and status=#{status}")
    List<Integer> getExist(@Param("sampleId") Integer sampleId, @Param("status") Integer status);

    @Update("update test_sample set is_save=#{state},save_time=#{saveTime} where id = #{sampleId}")
    void updateIsSave(Integer sampleId, Integer state,Integer saveTime);

    @Insert("insert into test_sample_circulation_record(sample_id,status,time,operator_id,operator_name)" +
            " values(#{record.sampleId},#{record.status},#{record.time},#{record.operatorId},#{record.operatorName})")
    void insertRecord(@Param("record") SampleCirculationRecord record);

    /**
     * 新增样品流转记录
     *
     * @param sampleCirculationRecord
     * @return
     */
    int saveSampleCirculationRecord(SampleCirculationRecord sampleCirculationRecord);

    @Select("SELECT\n" +
            "\tt1.is_save\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_entrusted_sample_details_rel t2 ON t1.id = t2.entrustment_id\n" +
            "WHERE\n" +
            "\tt2.sample_id =#{sampleId}")
    String getEntrustIsSaveBySampleId(@Param("sampleId") Integer sampleId);

    @Select("SELECT distinct sample_id\n" +
            "FROM\n" +
            "\ttest_sample_circulation_record\n" +
            "WHERE\n" +
            "\tsample_id = #{sampleId} and (status = '3' or status = '4')")
    List<Integer> checkExist(@Param("sampleId") Integer sampleId);

    /**
     * 根据样品id 及状态 查询是否存在 存在即update
     * status =2
     *
     * @param sampleIds
     * @return
     */
    List<Integer> sampleStausDisint(@Param("sampleIds") List<Integer> sampleIds);

    /**
     * 批量处理 样品状态
     * @param operatorId
     * @param operatorName
     * @param time
     * @param sampleIds
     * @return
     */
    int updateStausDisint(@Param("operatorId")Long operatorId,@Param("operatorName")String operatorName,
                          @Param("time") Date time, @Param("sampleIds") List<Integer> sampleIds);

    /**
     *
     * @param entrustmentId
     * @return 通过委托单ID 获取已经绑定样品集合
     */
    @Select("SELECT sample_id FROM test_entrusted_sample_details_rel WHERE entrustment_id = #{entrustmentId}")
    List<Integer> getSampleIsUsed(@Param("entrustmentId")Long entrustmentId);

    /**
     * 查询样品 留样列表与出入库列表
     * @param sampleOutPutVo
     * @return
     */
    List<SampleOutPutVo> sampleOutPutList(SampleOutPutVo sampleOutPutVo);
    /**
     * 查询样品 留样列表与出入库列表  : 加入视图
     * @param sampleOutPutVo
     * @return
     */
    List<SampleOutPutVo> sampleV_1(SampleOutPutVo sampleOutPutVo);
    /**
     * 查询技术负责人
     * @return
     */
    @Select("SELECT\n" +
            "\tt3.user_id as value,\n" +
            "\tt3.name as label\n" +
            "FROM\n" +
            "\tsys_role AS t1\n" +
            "\tLEFT JOIN sys_user_role AS t2 ON t1.role_id = t2.role_id \n" +
            "\tLEFT JOIN sys_user as t3 ON t2.user_id = t3.user_id\n" +
            "WHERE\n" +
            "\tt1.role_name = \"技术负责人\";")
    List<LabelValueVo> getApprover();

    /**
     * 更新检测项 复核状态。
     * @param excelInsertVo
     * @return
     */
    int updateItemReview(ExcelInsertVo excelInsertVo);

    @Select("select DISTINCT taskPublisher from sample_v_1 where sampleId=#{sampleId}")
    String getTaskPublisher(@Param("sampleId") int sampleId);

    @Select("SELECT DISTINCT\n" +
            "\tt1.sampler,\n" +
            "\tt1.sample_receiving_time As sampleReceivingTime\n" +
            "FROM\n" +
            "\ttest_task t1\n" +
            "LEFT JOIN test_entrusted_task_rel t2 ON t1.id = t2.task_id\n" +
            "LEFT JOIN test_entrusted_sample_details_rel t3 ON t2.entrust_id = t3.entrustment_id\n" +
            "WHERE t3.sample_id=#{sampleId}")
    ReceiveSampleParamVo getSampleTaker(@Param("sampleId") int sampleId);

    /**
     * 查询视图的分页展示
     * @param sampleOutPutVo
     * @return
     */
    List<SampleOutPutVo> selectPageVo(SampleOutPutVo sampleOutPutVo);

    /**
     * 查询条数
     * @param sampleOutPutVo
     * @return
     */
    Integer selectCount(SampleOutPutVo sampleOutPutVo);

    List<SampleCirculationRecord> selectSampleCirculationRecordList(@Param("list")List<SampleOutPutVo> list);

    /**
     * 查询视图的分页展示-留样列表
     * @param sampleOutPutVo
     * @return
     */
    List<SampleOutPutVo> selectPageVo3(SampleOutPutVo sampleOutPutVo);

    /**
     * 查询条数-留样列表
     * @param sampleOutPutVo
     * @return
     */
    Integer selectCount3(SampleOutPutVo sampleOutPutVo);

    /**
     * 通过委托单id 获取样品信息集合。
     */
    List<SampleEntity> selectAllState(@Param("list")List<Long> list);

    @Select("select id from test_sample where sample_code = #{code} limit 1")
    int getIdByCode(@Param("code") String code);

    @Update("update test_sample set sample_retention_period=#{parseInt} where sample_code = #{code}")
    void updateDayByCode(@Param("code") String code, @Param("parseInt") int parseInt);

    @Select("\n" +
            "SELECT DISTINCT GROUP_CONCAT( IFNULL( tb1.sample_code, \"——\" ) SEPARATOR ',') As sampleCode,\n" +
            "GROUP_CONCAT( IFNULL( tb1.report_code, \"——\" ) SEPARATOR ',') As reportCode,\n" +
            "GROUP_CONCAT( IFNULL( tb1.alias_name, \"——\" ) SEPARATOR ',') As aliasName,\n" +
            " tb1.entrustment_no As entrustNo,\n" +
            " tb1.acceptance_date As acceptanceDate,\n" +
            " tb1.order_time As orderTime,\n" +
            " tb1.task_code As taskCode,\n" +
            "tb1.request_date AS requestDate\n" +
            "FROM\n" +
            "(SELECT DISTINCT\n" +
            "\tt1.entrustment_no,\n" +
            "\tt1.acceptance_date,\n" +
            "\tt3.sample_code,\n" +
            "\tt3.alias_name,\n" +
            "\tt4.order_time,\n" +
            "\tt4.task_code,\n" +
            "\tt5.report_code,\n" +
            "\tt1.request_date\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_entrusted_sample_details_rel t2 ON t1.id = t2.entrustment_id\n" +
            "LEFT JOIN test_sample t3 ON t2.sample_id = t3.id\n" +
            "LEFT JOIN test_task t4 ON t1.id = t4.entrustment_id\n" +
            "LEFT JOIN test_report_record t5 ON t1.id=t5.entrustment_id\n" +
            "WHERE t1.state!= 144 AND t1.state>=1 \n" +
            "UNION ALL\n" +
            "SELECT DISTINCT\n" +
            "\tt1.entrustment_no,\n" +
            "\tt1.acceptance_date,\n" +
            "\tt3.sample_code,\n" +
            "\tt3.alias_name,\n" +
            "\tt4.order_time,\n" +
            "\tt4.task_code,\n" +
            "\tt5.report_code,\n" +
            "\tt1.request_date\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_entrusted_sample_details_rel t2 ON t1.id = t2.entrustment_id\n" +
            "LEFT JOIN test_sample t3 ON t2.sample_id = t3.id\n" +
            "LEFT JOIN test_task t4 ON t1.id = t4.entrustment_id\n" +
            "LEFT JOIN test_report_record t5 ON t1.id=t5.entrust_id\n" +
            "WHERE t1.state!= 144 AND t1.state>=1 \n" +
            "UNION ALL\n" +
            "SELECT DISTINCT\n" +
            "\tt1.entrustment_no,\n" +
            "\tt1.acceptance_date,\n" +
            "\tt3.sample_code,\n" +
            "\tt3.alias_name,\n" +
            "\tt4.order_time,\n" +
            "  t4.task_code,\n" +
            "\tt5.report_code,\n" +
            "\tt1.request_date\n" +
            "FROM\n" +
            "\ttest_entrusted_info t1\n" +
            "LEFT JOIN test_entrusted_sample_details_rel t2 ON t1.id = t2.entrustment_id\n" +
            "LEFT JOIN test_sample t3 ON t2.sample_id = t3.id\n" +
            "LEFT JOIN test_task t4 ON t1.id = t4.entrustment_id\n" +
            "LEFT JOIN test_report_record_mid t5 ON t1.id=t5.entrust_id\n" +
            "WHERE t1.state!= 144 AND t1.state>=1 AND t5.report_code is not null) tb1\n" +
            "GROUP BY tb1.task_code\n")
    List<TzBean> exportWtTz();

    @Select("SELECT time from test_sample_circulation_record WHERE sample_id =#{sampleId} AND status=#{state}")
    Date getDate(@Param("sampleId") Integer sampleId, @Param("state") Integer state);

    @Select("select sample_retention_period from test_sample where id=#{sampleId}")
    int getDaysById(@Param("sampleId") Integer sampleId);

    /**
     * 查询样品信息
     * @param codes
     * @return
     */
    List<SampleEntity> getGroupNodes(@Param("codes") List<String> codes);

    @Select("SELECT\n" +
            "\t*\n" +
            "FROM\n" +
            "\ttest_sample_area\n" +
            "ORDER BY\n" +
            "\tid")
    List<Node> retentionSampleAreaList();
}
