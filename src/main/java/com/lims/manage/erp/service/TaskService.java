package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ReqTaskPool;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TestTaskPool;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

public interface TaskService {

    /**
     * 查询任务详情
     *
     * @return
     */
    TaskDetailInfoVo getTaskDetailInfo(Long taskId);

    /**
     * 查询任务详情 二次
     *
     * @return
     */
    TaskDetailInfoVo getTaskDetailInfoTwo(Long taskId,String [] deptIds);

    /**
     * 试验开始下 查询任务详情（检测项无价格不展示）——线上使用
     *
     * @return
     */
    TaskDetailInfoVo getTaskTestDetails(Long taskId,String [] deptIds);

    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskList(TaskListParamVo paramVo);

    /**
     * 根据人员id 获取部门集合id集合
     * @param userId
     * @return
     */
    String getDeptIds(Long userId);

    /**
     * 查询检测列表——并设置分页
     *
     * @param paramVo
     * @return
     */
    PagingToolVo getTaskListTwo(TaskListParamVo paramVo, String [] deptIds);

    /**
     * 查询检测列表——并设置分页 - 视图
     *
     * @param paramVo
     * @return
     */
    PageInfo<TaskListVo> getTaskListTwoShow(TaskListParamVo paramVo, String [] deptIds);

    /**
     * 查询领样列表
     *
     * @param paramVo
     * @return
     */
    PageInfo getSampleList(TaskListParamVo paramVo);

    /**
     * 领样
     *
     * @param paramVo
     * @return
     */
    int receiveSample(ReceiveSampleParamVo paramVo);

    /**
     * 领样人姓名与任务单ID  效验是否属于同一部门
     * @param taskId 任务单ID
     * @param sampler 领样人姓名
     * @return true 成立  flase不成立
     */
    Boolean isIntendedEffectReceive(Long taskId,String sampler);

//    List<TaskDetailInfoVo> getTaskDetailInfo();

    /**
     * 副团长抢单并 派发 团队人员 操作
     */
    Boolean postGrabASingle(TaskTestEntity taskTestEntity);

    /**
     * 副团长抢单并 派发 团队人员 操作
     */
    Boolean postGrabASingleTwo(TaskTestEntity taskTestEntity);

    /**
     * 批量领取任务单
     * @param taskTestEntity
     * @return
     */
    Boolean batchPostGrabASingle(List<TaskTestEntity> taskTestEntity);

    /**
     * 返回 团队成员姓名
     */
    List<LabelValueTeamVo> getTeamUserName(Long UserLong);

    /**
     * 返回 团队成员姓名
     */
    TeamVo getTeamUserNameTwo(Long UserLong);

    /**
     * 返回 团队姓名 通过委托单id下样品名称是否匹配进行过滤
     */
    TeamVo getEntrustTeamUserName(Long UserLong,Long entrustId);

    /**
     * 判断任务单 状态 state ==0 是空闲
     */
    Boolean getJudgmentTaskList(Long id);

    List<String> getSampleOutward(Long taskId);

    /**
     * 查询原始记录表头信息
     * @param taskId
     * @param sampleId
     * @param checkItemId
     * @return
     */
    OriginalRecordDataVo getOriginalData(Long taskId, Integer sampleId, Integer checkItemId,Integer idItem);

    /**
     * 查询原始记录模板
     *
     * @param checkItemId
     * @return
     */
    String getOriginalTemplate(Integer checkItemId);
    String getOriginalTemplateUrl(Integer checkItemId);

    /**
     * 填充数据
     */
    XWPFDocument downloadEntrust(TaskDetailInfoVo taskDetailInfoVo, InputStream object,Boolean status);

    /**
     * 上传原始记录
     *
     * @param paramVo
     * @param file
     * @return
     */
    int uploadOriginalRecord(OriginalRecordParamVo paramVo, MultipartFile file);

    /**
     *  通过id集合 上传原始记录文件
     */
    Boolean uploadingBatch(List<Integer> ids, MultipartFile file);

    /**
     * 通过ids集合 获取集合 效验原始记录文件
     * @param ids
     * @return
     */
    Boolean effectDataSet(List<Integer> ids);

    /**
     * @param itemId
     * @return
     */
    ReviewVo getReviewInfo(Integer itemId);

    /**
     * 通过或驳回
     *
     * @param itemId
     * @param state
     * @return
     */
    String passorno(Integer itemId, Integer state,String opinion,Long userId);

    /**
     * 删除附件
     *
     * @param itemId
     * @param
     * @return
     */
    String passorno_delete(Integer itemId);

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

    /**
     * 批量 通过或驳回
     *
     * @return
     */
    String batchReview(TaskStatsVo taskStatsVo);


    ZipOutputStream packagingWorkbookZip(List<TaskIdEntity> dataEntitys, HttpServletResponse response, Long taskId) throws IOException;

    OutputStream packagingWorkbookXls(List<TaskIdEntity> dataEntitys, HttpServletResponse response) throws IOException;

    /**
     * 根据url 返回附件信息
     *
     * @param urls
     * @param response
     * @return
     * @throws IOException
     */
    void packagingUrlSWorkbookXls(List<String> urls, HttpServletResponse response) throws IOException;

    /**
     * 检测询任务列表——并设置分页
     *
     * @param paramVo
     * @return
     */
    PagingToolVo getTaskList(TaskListParamVo paramVo, String[] deptIds);

    /**
     * 检测询任务列表——并设置分页-视图
     *
     * @param paramVo
     * @return
     */
    PageInfo<TaskListVo> getTaskListShow(TaskListParamVo paramVo, String [] deptIds);

    /**
     * 根据任务单id 判断 委托状态不等于 144
     */
    Boolean judgeTaskStatus(Long id);

    /**
     * 查询设备仪器使用人下拉列表
     * @param userId
     * @return
     */
    List<LabelValueVo> getDeviceUser(Long userId);

    Long getEntrustIdByTaskId(Long taskId);

    /**
     * 返回原始记录
     *  list 检测项主键
     *  checkReview 类型（中间复核 或 最终复核）
     * @return
     */
    XSSFWorkbook getOriginalRecordAttachment(ExcelInsertVo excelInsertVo) throws IOException;

    /**
     * 通过任务单id 验证 报告state =7 盖章，返回 true 否则 返回false
     * @param taskId
     * @return
     */
    Boolean getVerifyReportState(Long taskId);

    /**
     * 填充数据
     */
    XWPFDocument downloadEntrustNew(TaskDetailInfoVo taskDetailInfoVo, InputStream object) throws IOException;

    /**
     * 验证用户信息
     * @return
     */
    String verifyUserInformation(Long userId);

    PageInfo<TestTaskPool> taskHall(ReqTaskPool bean);

    PageInfo<TestTaskPool> myTaskList(ReqTaskPool bean);

    /**
     * 根据任务单id 判断 任务单状态 == 实验完成
     */
    Boolean judgeTaskEndTest(Long id, ExcelInsertVo excelInsertVo);

    /**
     * 返回 辅助人员 用户信息
     */
    Result getAuxiliaryPersonnelInformation();

    /**
     * 获取当前团队成员信息
     *
     * @return
     */
    Result getTeamMemberInformation();

    /**
     * 比较任务单创建时间：区分团队信息是否拆分
     * 验证任务单信息：通过委托单id 和 部门id 得出是领取，还是修改
     *
     * @param entrustId
     * @return
     */
    Result compareTaskListCreationInformation(Long entrustId, Integer teamId);

    /**
     * 解决插单问题：
     *
     * @param olCode：B2407
     * @param taskCode：DHY2407-005
     * @return
     */
    Result getplugOperation(String olCode, String taskCode);

    List<Integer> selectList(String toString, String toString1);

    Long getIdByCode(String toString);

    List<Integer> selectList1(String toString);
}
