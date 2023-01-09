package com.lims.manage.erp.mapper;

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

    @Select("SELECT\n" +
            "\tSTATUS,\n" +
            "\ttime,\n" +
            "\toperator_name\n" +
            "FROM\n" +
            "\ttest_sample_circulation_record\n" +
            "WHERE\n" +
            "\tsample_id = #{sampleId}")
    List<SampleCirculationRecord> getRecords(@Param("sampleId") Integer sampleId);

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

    @Update("update test_sample set is_save=#{state} where id = #{sampleId}")
    void updateIsSave(Integer sampleId, Integer state);

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

    @Select("SELECT sample_id\n" +
            "FROM\n" +
            "\ttest_sample_circulation_record\n" +
            "WHERE\n" +
            "\tsample_id = #{sampleId} and status = '3' or status = '4'")
    List<Integer> checkExist(@Param("sampleId") Integer sampleId);
}
