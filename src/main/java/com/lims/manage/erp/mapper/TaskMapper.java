package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/12/6 17:47
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface TaskMapper extends BaseMapper {

    /**
     * 获取最大的任务编号
     *
     * @return
     */
    Integer selectMaxNo();

    Integer selectMaxNoByCode(String code);

    String getTeamCode(Long deptId);

    /**
     * 更新委托单状态
     *
     * @param entrustmentId
     */
    void updateEntrustById(Long entrustmentId,Integer state);

    /**
     * 根据委托单id 修改 test_task 下 report_complete =2
     */
    int updateTestTaskReportComplete(Long entrustmentId);
    /**
     * 保存任务单
     *
     * @param entity
     */
    void save(TaskEntity entity);

    /**
     * 保存任务单--分配任务
     * @param entity
     */
    void save(TaskVo entity);
    /**
     * 保存任务单--分配任务
     * @param vos
     */
    void batchSave(@Param("vos")List<TaskVo> vos);

    /**
     * 查询任务详情
     *
     * @return
     */
    TaskDetailInfoVo getTaskDetailInfo(Long taskId);

    /**
     * 查询任务详情二次开发
     *
     * @return
     */
    TaskDetailInfoVo getTaskDetailInfoTwo(TaskListParamVo paramVo);

    /**
     * 根据检测项 主键 获取 仪器信息
     */
    List<TestInstrumentEntity>  getInstrumentEntityList(int idItem);

    /**
     * 根据任务id 获取所属部门信息
     * @param taskId
     * @return
     */
    @Select("SELECT dept_id FROM test_task WHERE id = #{taskId}")
    Long getTaskDept(Long taskId);

    /**
     * 修改任务信息
     */
    int updateTestTask(TaskTestEntity taskTestEntity);

    /**
     * 查询任务单详情
     */
    TaskTestEntity getTaskOrders(Long taskId);

    /**
     * 根据用户id 查询 团队 名
     */
    TaskTestTeamEntity selectTeamCode(Long userid);

    /**
     * 根据人员id 获取 部门信息 和 关联的下级部门信息
     */
    List<TeamTreeStructureEntity> getTeamDeptVo(Long userid);



    /**
     *  根据团队id 查询人员信息列表
     */
    List<LabelValueVo> getMemberInformation(@Param(value = "deptIds") Set<Long> deptIds);

    /**
     * 根据角色id 查询审批、签发人员信息
     * @param roleId
     * @return
     */
    List<LabelValueVo> getRoleInformation(Long roleId);


    /**
     * 根据团队id 返回 用户集合
     *
     * @param id
     * @return
     */
    List<LabelValueTeamVo> selectTeamList(Integer id);

    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskList(TaskListParamVo paramVo);

    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskListTwo(TaskListParamVo paramVo);
    /**
     * 查询任务列表 包含样品信息
     */
    List<TaskListVo> getTaskListContainsSample(TaskListParamVo paramVo);

    /**
     * 查询任务列表 并且 state>=1
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskListTwoGreater(TaskListParamVo paramVo);

    /**
     * 查询任务单关联的其他任务单号
     * @param taskId
     * @return
     */
    List<String> getCorrelationTask(Long taskId);

    /**
     * 查询领样列表
     *
     * @param paramVo
     * @return
     */
    List<ReceiveSampleListVo> getSampleList(TaskListParamVo paramVo);

    /**
     * 根据id 判断任务 state 状态
     *
     * @param id
     * @return
     */
    Integer getJudgmentTaskList(Long id);

    /**
     * 修改任务的领样人、领样时间
     *
     * @param paramVo
     * @return
     */
    Integer updateSampler(ReceiveSampleParamVo paramVo);

    /**
     * 查询原始记录基本停息
     *
     * @param taskId
     * @return
     */
    EntrustEntity getEntrustBaseInfo(Long taskId);

    /**
     * 查询检测项检测依据
     *
     * @param checkItemId
     * @param entrustId
     * @return
     */
    String getCheckBasis(Integer checkItemId, Long entrustId, Integer sampleId);


    /**
     * 查询判定依据
     *
     * @param sampleId
     * @param entrustId
     * @return
     */
    List<String> getJudgeBasis(Integer sampleId, Long entrustId);

    /**
     * 查询原始记录模板名称
     *
     * @param checkItemId
     * @return
     */
    String getOriginalTemplate(Integer checkItemId);
    /**
     * 查询原始记录模板文件url
     *
     * @param checkItemId
     * @return
     */
    String getOriginalTemplateUrl(Integer checkItemId);

    /**
     * 更新上传的原始记录文件地址
     *
     * @param originalUrl
     * @param entrustId
     * @param sampleId
     * @param checkItemId
     * @return
     */
    int updateOriginalFile(String originalUrl, Long entrustId, Integer sampleId, Integer checkItemId,String fileUrlStr,String fileName);

    /**
     * 通过id 更新上传的原始记录文件
     * @param list
     * @return
     */
    int updateTestEntrustedSampleCheckitemRel(List<SampleItemInstrumentEntity> list);

    /**
     * 查询复合数据
     *
     * @param itemId
     * @return
     */
    ReviewVo getReviewInfo(Integer itemId);

    /**
     * @param itemId
     * @param state
     * @return
     */
    int updateState(Integer itemId, Integer state,String opinion);

    /**
     * 查询任务下检测人、记录人、复核人、报告制作人信息
     * @param taskId
     * @return
     */
    PersonInfoVo getPersonInfo(Long taskId);

    /**
     * 修改人员信息
     * @param vo
     * @return
     */
    int updatePersonInfo(PersonInfoVo vo);

    int updateReportStatus(@Param("status") Integer status,@Param("taskId")Long taskId);

    /**
     * 查询所有任务报告的状态
     * @param entrustmentId
     * @return
     */
    List<Integer> getAllReportComplete(Long entrustmentId,Long taskId);

    /**
     * 根据任务id获取委托单id
     * @param id
     * @return
     */
    @Select("select entrustment_id from test_task where id = #{id}")
    Long getEntrustIdByTaskId(@Param("id") Long id);

    /**
     * 根据委托单id 和 deptId 获取任务单id
     * @param
     * @return
     */
    @Select("SELECT\n" +
            "\tid \n" +
            "FROM\n" +
            "\ttest_task \n" +
            "WHERE\n" +
            "\tentrustment_id = #{id} \n" +
            "\tAND dept_id = #{deptId}")
    Long getTestTaskId(@Param("id") Long id,@Param("deptId") Integer deptId);

    int batchUpdateCheckItem(@Param("list") List<CheckItemDeptVo> list);

    /**
     * 批量修改可出报告的科室
     * @param list
     * @return
     */
    int batchUpdateReportTeam(@Param("list") List<UpdateReportTeamVo> list);

    /**
     * 通过检测项id 获取test_task 任务单id
     * @param itemId
     * @return
     */
//    @Select("SELECT\n" +
//            "\tt1.id \n" +
//            "FROM\n" +
//            "\ttest_task AS t1\n" +
//            "\tLEFT JOIN test_entrusted_sample_checkitem_rel AS t2 ON t1.entrustment_id = t2.entrust_id \n" +
//            "WHERE\n" +
//            "\tt2.id = #{itemId} \n" +
//            "\tAND t1.dept_id  in (\n" +
//            "SELECT\n" +
//            "\tt2.dept_id \n" +
//            "FROM\n" +
//            "\ttest_task AS t1\n" +
//            "\tLEFT JOIN test_entrusted_sample_checkitem_rel AS t2 ON t1.entrustment_id = t2.entrust_id \n" +
//            "WHERE\n" +
//            "\tt2.id = #{itemId} \n" +
//            "\t)")
    @Select("select task_id from test_entrusted_sample_checkitem_rel where id = #{itemId}")
    Long getReturnTaskId(@Param("itemId")Integer itemId);

    @Select("select state from test_task where entrustment_id = #{entrustId}")
    List<String> getStateByEntrustId(Long entrustId);

    @Select("select inspector from test_task where entrustment_id = #{entrustId}")
    List<String> getInspectorByEntrustId(@Param("entrustId") Long id);

    /**
     * 收样列表查询子原材样品信息
     * @param sampleId
     * @return
     */
    List<SamplePrivateInfoVo> getNodeSampleList(Integer sampleId);

    /**
     * 统计未任务领取。
     */
    Integer selectCount(Integer state ,List<Long> deptIds);
}
