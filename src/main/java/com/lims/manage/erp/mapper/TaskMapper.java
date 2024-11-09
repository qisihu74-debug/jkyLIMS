package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
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
     * 修改任务单 - 参数可以 set=null
     *
     * @param taskTestEntity
     * @return
     */
    int updateTaskEntity(TaskTestEntity taskTestEntity);

    /**
     * 批量修改任务信息
     *
     * @param list
     * @return
     */
    int batchUpdateTestTask(@Param("list") List<TaskTestEntity> list);

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
     * 查询任务列表 包含样品信息-视图
     */
    List<TaskListVo> getTaskListContainsSampleShow(TaskListParamVo paramVo);

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
    int updateState(Integer itemId, Integer state,String opinion,Long reviewedBySetUrl);

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

    /**
     * 通过任务单id 获取任务单中关联信息。
     */
    TaskListVo selectTaskListDetails(Long id);

    int batchReview(List<TaskStatsItemVo> list);

    /**
     * -- 通过任务单 效验检测项信息。
     */
    @Select("SELECT\n" +
            "\tstate \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel \n" +
            "WHERE\n" +
            "\ttask_id = #{taskId} and dept_id = #{deptId} and unit_price is not null ")
    List<Integer> selectCheckItemState(@Param(value = "taskId") Long taskId,@Param(value = "deptId") Integer deptId);

    /**
     * 获取有效数据 根据主键集合。
     */
    List<TaskIdEntity> selectconditionId(@Param(value = "array") Integer[] array);

    @Select("SELECT MIN(start_detection_time) from test_task where entrustment_id=#{id}")
    Date getStartTime(@Param("id") Long id);

    @Select("select MAX(end_detection_time) from test_task where entrustment_id=#{id}")
    Date getEndTime(Long id);

    /**
     * 查询委托下任务进度
     * @param entrustmentId
     * @return
     */
    List<TaskProgressVo> getTaskStateByEntrustId(Long entrustmentId);

    /**
     * 根据委托单ID查询分配的任务科室ID
     * @param entrustId
     * @return
     */
    @Select("SELECT dept_id FROM test_task WHERE entrustment_id = #{entrustId} and state != 144 LIMIT 1")
    Long getDeptByEntrustId(Long entrustId);

    /**
     * 查询样品的外观描述
     * @param taskId
     * @return
     */
    List<String> getSampleOutward(Long taskId);

    /**
     * 查询委托单ID
     * @param taskId
     * @return
     */
    Long getEntrustId(Long taskId);

    /**
     * 根据主键获取任务信息
     * @param taskId
     * @return
     */
    TaskTestEntity getTaskTestEntityById(Long taskId);

    /**
     * 根据任务单id 得到：委托单状态为144 返回 true
     * @param taskId
     * @return
     */
    @Select("SELECT\n" +
            "IF\n" +
            "\t( t2.state = 144, TRUE, FALSE ) \n" +
            "FROM\n" +
            "\ttest_task AS t1\n" +
            "\tLEFT JOIN test_entrusted_info AS t2 ON t1.entrustment_id = t2.id \n" +
            "WHERE\n" +
            "\tt1.id = #{taskId}")
    Boolean judgeTaskStatus(@Param("taskId") Long taskId);

    /**
     *  通过任务单id 获取所属下的样品名称
     * @param list
     * @return
     */
    List<TestEntrustedTaskRelVo> getSampleNames(@Param("list")  List<Long> list);

    /**
     * 通过委托单id 获取 任务单id列表
     * @param list
     * @return
     */
    List<TestEntrustedTaskRelVo> getTaskIds(@Param("list")  List<Long> list);

    /**
     * 获取任务单下单时间
     * @param taskId
     * @return
     */
    @Select("SELECT id,order_time FROM test_task WHERE id = #{taskId}")
    TaskTestEntity getTaskOrderTime(@Param("taskId") Long taskId);

    /**
     * 查询任务详情三次开发
     * @return
     */
    TaskDetailInfoVo getTaskDetailInfoThree(TaskListParamVo paramVo);

    /**
     * 通过任务单id 获取检测项列表
     * @param taskId
     * @return
     */
    @Select("SELECT\n" +
            "\tid AS itemId,\n" +
            "\tcheck_item_name AS checkItemName ,\n" +
            "\ttask_id as taskId,\n" +
            "\tstate as state,\n" +
            "\tend_time as endTime\n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel \n" +
            "WHERE\n" +
            "\ttask_id = #{taskId}")
    List<CheckItemInfoVo> getEntrustItemVos(@Param("taskId") Long taskId);

    /**
     * 新增检测任务列表 (根据检测人id 返回待任务单检测列表)
     */
    List<TaskListVo> detectionTaskList(@Param("search") String search, @Param("userId") Long userId);

    /**
     * 新增检测任务列表 (根据检测人id 返回待任务单检测列表) -- 排除 AND t1.receive_time IS NOT NULL and  <![CDATA[ t1.state < 4 ]]>
     */
    List<TaskListVo> detectionExcludeReceiveTimeTaskList(@Param("search") String search, @Param("userId") Long userId);

    /**
     * 当前任务列表 (根据设备id 返回列表)
     */
    List<TaskListVo> taskList(@Param("search") String search, @Param("instrumentId") Long instrumentId);

    List<TaskListParamVo> getUserSignatureUrls(@Param("list") List<Long> list);

    /**
     * 获取有效数据 根据主键集合。
     */
    List<TaskIdEntity> selectItems(@Param(value = "array") Integer[] array);

    /**
     * 根据任务单id 获取相应的 检测项
     */
    List<TaskIdEntity> selectItemPages(@Param(value = "taskId") Long taskId);

    @Select("\tSELECT\n" +
            "\t\tt3.state \n" +
            "\tFROM\n" +
            "\t\ttest_task AS t1\n" +
            "\t\tLEFT JOIN test_entrusted_info AS t2 ON t1.entrustment_id = t2.id\n" +
            "\t\tLEFT JOIN test_report_record AS t3 ON t3.entrustment_id = t2.id \n" +
            "\tWHERE\n" +
            "\tt1.id = #{taskId} and t3.state is not null")
    List<Integer> getVerifyReportState(@Param(value = "taskId") Long taskId);

    /**
     * 查看任务单详情
     * @param taskId
     * @return
     */
    TaskTestEntity selectTaskEntity(@Param("taskId")Long taskId);

    /**
     * 新增 废弃任务单信息 test_task_used
     * @param taskTestEntity
     * @return
     */
    int inserTasUsed(TaskTestEntity taskTestEntity);

    /**
     * 删除任务流转信息 根据entrustId
     * @param entrustId
     * @return
     */
    @Delete("DELETE FROM test_entrusted_task_rel  WHERE entrust_id in(#{entrustId})")
    int deleteTaskRel(@Param("entrustId")Long entrustId);

    /**
     * 删除任务流水号 根据entrustId
     * @param entrustId
     * @return
     */
    @Delete("DELETE FROM test_task_pool  WHERE entrustment_id in(#{entrustId})")
    int deleteTaskRelPool(@Param("entrustId")Long entrustId);

    /**
     * 根据委托单id 进行批量处理
     * 进行检测项的针对 dept_id update 操作
     * @param entrustmentId
     * @return
     */
    int batchUpdateItemState(@Param("entrustmentId")Long entrustmentId);

    /**
     * 删除任务单
     * @param taskId
     * @return
     */
    @Delete("DELETE FROM test_task  WHERE id = #{taskId}")
    int deleteTaskById(@Param("taskId")Long taskId);

    List<TestTaskPool> taskHall(@Param("item") ReqTaskPool item);

    List<TestTaskPool> taskHall1(@Param("item") ReqTaskPool item);

    List<TestTaskPool> myTaskList(@Param("item") ReqTaskPool bean);

    /**
     * 根据任务id获取委托单报告数量
     * @param id
     * @return
     */
    @Select("SELECT\n" +
            "\tt2.report_count \n" +
            "FROM\n" +
            "\ttest_task AS t1\n" +
            "\tLEFT JOIN test_entrusted_info AS t2 ON t1.entrustment_id = t2.id \n" +
            "WHERE\n" +
            "\tt1.id = #{id}")
    Integer getReportCountByTaskId(@Param("id") Long id);

    /**
     *  根据团队id 查询人员信息列表-拼接信息
     */
    List<LabelValueVo> getMemberInformationConcat(@Param(value = "deptIds") Set<Long> deptIds,@Param(value = "userIds") Set<Long> userIds);

    void updateReportStatusByEntrustId(@Param("status") Integer status,@Param("entrustId") Long entrustId);

    /**
     *  根据团队id 查询人员信息列表-拼接信息
     */
    List<LabelValueVo> getMemberInformationConcat1(@Param(value = "userIds") Set<Long> userIds);

    /**
     * 根据任务单id 获取样品名称
     * @param taskId
     * @return
     */
    @Select("SELECT DISTINCT\n" +
            "\tt2.alias_name \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel AS t1\n" +
            "\tLEFT JOIN test_sample AS t2 ON t1.sample_id = t2.id \n" +
            "WHERE\n" +
            "\tt1.task_id = #{taskId}")
    List<String> getTaskSamples(@Param("taskId")Long taskId);

    /**
     *  基础信息处理。
     * @return
     */
    List<TestInitDataEntity> selectEntrustBasis(Integer TypeId);

    /**
     * 批量更新任务单状态
     *
     * @param list
     * @return
     */
    int batchUpdateTestTaskState(@Param("list") List<TaskTestEntity> list);

    /**
     * 批量更新任务单操作时间
     */
    int bathUpdateTaskUpdateTime(@Param("taskIds") Set<Long> taskIds);

    /**
     * 批量更新委托单状态
     *
     * @param entrustIds
     * @param state
     * @return
     */
    int batchUpdateEntrustById(@Param("entrustIds") List<Long> entrustIds, @Param("state") Integer state);

    /**
     *  更新 test_task working_hours_id = 1
     * @param taskId
     * @return
     */
    int updateTaskWorkingHoursId(@Param("taskId")Long taskId);
    @Select("\tSELECT id FROM test_task WHERE state >=4 and create_time >= \"2023-11-10 00:00:00\" ORDER BY create_time desc  LIMIT 1000\n")
    List<Long> selectTaskIds();

    @Update("update test_entrusted_sample_checkitem_rel set state=3 where entrust_id=#{entrustId}")
    void updateStateByEntrustId(@Param("entrustId") Long entrustId);

    @Select("SELECT\n" +
            "\tt1.id,\n" +
            "\tt1.sn,\n" +
            "\tt2.entrustment_no,\n" +
            "\tt1.alias_name,\n" +
            "\tt1.price,\n" +
            "\tt1.task_flow_req,\n" +
            "\tt1.publisher,\n" +
            "\tt1.receive_date,\n" +
            "\tt1.publish_date,IFNULL( t1.task_list_status, 0 ) as task_list_status \n" +
            "FROM\n" +
            "\ttest_task_pool t1\n" +
            "\tLEFT JOIN test_entrusted_info t2 ON t1.entrustment_id = t2.id\n" +
            "\tWHERE t1.id>0")
    List<TestTaskPool> taskHallByAdm();


    /**
     *  返回全部科室人员信息
     */
    @Select("\tSELECT\n" +
            "\t\tt1.user_id as value,\n" +
            "\t\tt1.NAME as label,\n" +
            "\t\tt3.NAME AS text \n" +
            "\tFROM\n" +
            "\t\tsys_user t1\n" +
            "\t\tLEFT JOIN test_technicist AS t2 ON t1.user_id = t2.user_id\n" +
            "\t\tLEFT JOIN test_team AS t3 ON t2.team_id = t3.id\n" +
            "\t\tWHERE t3.NAME is not null")
    List<LabelValueVo> getAllTeamUser();

    /**
     * 获取团队成员信息及部门名称
     *
     * @param teamId
     * @return
     */
    List<LabelValueVo> selectTeamMemberInformation(Long teamId);

    /**
     * 返回全部科室人员信息
     */
    @Select("SELECT\n" +
            "\tt1.user_id AS \n" +
            "VALUE\n" +
            "\t,\n" +
            "\tt1.NAME AS label,\n" +
            "\tt3.NAME AS text \n" +
            "FROM\n" +
            "\tsys_user t1\n" +
            "\tLEFT JOIN test_technicist AS t2 ON t1.user_id = t2.user_id\n" +
            "\tLEFT JOIN test_team AS t3 ON t2.team_id = t3.id")
    List<LabelValueVo> getAllTeamNAMEUser();

    /**
     * 查询任务单中 已接任务单信息
     */
    Integer selectTaskTDetectionType(TaskListParamVo paramVo);

    @Update("update test_task set report_complete = #{state} WHERE entrustment_id = #{entrustId}")
    void updateTaskReportComplete(@Param("entrustId") Long entrustId, @Param("state") String state);

    /**
     * 获取所有含有工时的报告信息Id 返回
     *
     * @return
     */
    @Select("SELECT\n" +
            "\tid \n" +
            "FROM\n" +
            "\ttest_report_record \n" +
            "WHERE\n" +
            "\tentrustment_id IN ( SELECT entrustment_id FROM test_task WHERE id IN ( SELECT task_id FROM test_task_order_working_hours GROUP BY task_id ) )")
    List<Long> getreportIdszzzz();

    /**
     * 通过委托单ID 及部门id 获取任务单信息
     *
     * @param entrustId
     * @param teamId
     * @return
     */
    List<TaskVo> selectTaskCreateTimeList(@Param("entrustId") Long entrustId, @Param("teamId") Integer teamId);

    /**
     * 查询任务单号信息
     *
     * @param taskCode
     * @return
     */
    @Select("SELECT id,entrustment_id as entrustmentId,code,team_id as deptId,order_time as orderTime , task_code as taskCode FROM test_task WHERE  task_code like CONCAT( '%',#{taskCode}, '%') ORDER BY code desc limit 1")
    TaskVo selectTaskDetails(@Param("taskCode") String taskCode);

    /**
     * 查询任务单号信息
     *
     * @param taskCode
     * @return
     */
    @Select("SELECT id,entrustment_id as entrustmentId,code,team_id as deptId,order_time as orderTime , task_code as taskCode FROM test_task WHERE  task_code = #{taskCode}  limit 1")
    TaskVo selectTaskOneDetails(@Param("taskCode") String taskCode);

    /**
     * 更新旧单号信息
     *
     * @param taskVo
     * @return
     */
    int updateoldTaskEntity(TaskVo taskVo);

    /**
     * 通过任务单Id 获取关联仪器
     *
     * @param taskId
     * @return
     */
    @Select("SELECT DISTINCT\n" +
            "\tintrusment_id \n" +
            "FROM\n" +
            "\ttest_product_item_instrument_middle_rel \n" +
            "WHERE\n" +
            "\tid_item IN ( SELECT id FROM test_entrusted_sample_checkitem_rel WHERE task_id = #{taskId} ) UNION\n" +
            "SELECT DISTINCT\n" +
            "\tinstrument_id \n" +
            "FROM\n" +
            "\ttest_instrument_use_record \n" +
            "WHERE\n" +
            "\ttask_id = #{taskId}")
    List<Integer> getDistinctInstrumentIds(Long taskId);

    @Select("SELECT DISTINCT\n" +
            "\titem_id\n" +
            "FROM\n" +
            "\ttest_check_items_task_rel\n" +
            "WHERE\n" +
            "\tcheck_item_name LIKE CONCAT('%',#{itemName},'%')\n" +
            "AND task_id = (\n" +
            "\tSELECT\n" +
            "\t\tid\n" +
            "\tFROM\n" +
            "\t\ttest_task\n" +
            "\tWHERE\n" +
            "\t\ttask_code = #{taskCode}\n" +
            ")")
    List<Integer> selectItemIds(@Param("taskCode") String taskCode, @Param("itemName") String itemName);

    @Select("SELECT DISTINCT\n" +
            "\titem_id,check_item_name\n" +
            "FROM\n" +
            "\ttest_check_items_task_rel\n" +
            "\tWHERE task_id = (\n" +
            "\tSELECT\n" +
            "\t\tid\n" +
            "\tFROM\n" +
            "\t\ttest_task\n" +
            "\tWHERE\n" +
            "\t\ttask_code = #{taskCode}\n" +
            ")")
    List<TaskStatsItemVo> selectItemIds1(@Param("taskCode") String taskCode);

    @Select("SELECT\n" +
            "\t\tid\n" +
            "\tFROM\n" +
            "\t\ttest_task\n" +
            "\tWHERE\n" +
            "\t\ttask_code = #{taskCode}")
    Long getIdByCode(@Param("taskCode") String taskCode);

    /**
     * 通过检测项id 或委托单id 获取附件集合
     *
     * @param array 检测项id集合
     * @param array 委托单id
     * @return
     */
    List<String> getTaskRecordUrl(@Param(value = "array") Integer[] array, @Param(value = "entrustId") Long entrustId);

    /**
     * 通过样品id 获取 检测项坐标信息
     *
     * @param sampleId 样品id
     * @return
     */
    List<CheckItemInfoVo> getCheckItemTemplateItemPosition(@Param(value = "sampleId") int sampleId);

    /**
     * 通过sampleId 获取 检测项报告标识坐标信息
     *
     * @param sampleId
     * @return
     */
    @Select("SELECT\n" +
            "\titem_id AS itemId,\n" +
            "\t` report_item_position` AS opinion \n" +
            "FROM\n" +
            "\tda_product_dictionary \n" +
            "WHERE\n" +
            "\treport_model_id IN (\n" +
            "SELECT\n" +
            "\tt2.template_id \n" +
            "FROM\n" +
            "\ttest_sample AS t1\n" +
            "\tLEFT JOIN test_report_template_product_ref AS t2 ON t2.product_id = t1.product_id \n" +
            "WHERE\n" +
            "\tt1.id = #{sampleId} \n" +
            "\t)")
    List<CheckItemInfoVo> getDaProductDictionaryPosition(@Param(value = "sampleId") int sampleId);

    /**
     * 通过 sampleId 获取附件集合
     *
     * @param sampleId 样品id
     * @return
     */
    @Select("SELECT\n" +
            "\tt3.url \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel AS t1\n" +
            "\tLEFT JOIN test_task AS t2 ON t1.task_id = t2.id\n" +
            "\tLEFT JOIN da_task_record AS t3 ON t3.task_code = t2.task_code \n" +
            "WHERE\n" +
            "\t1 = 1 \n" +
            "\tAND t1.sample_id = #{sampleId} \n" +
            "\tAND t1.task_id IS NOT NULL \n" +
            "\tAND t3.url IS NOT NULL \n" +
            "GROUP BY\n" +
            "\tt2.task_code")
    List<String> getTaskRecordUrlBySampleId(@Param(value = "sampleId") int sampleId);
}
