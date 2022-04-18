package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.vo.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

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
     * 查询任务列表——并设置分页
     *
     * @param paramVo
     * @return
     */
    PageInfo getTaskListTwo(TaskListParamVo paramVo, String [] deptIds);

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
     * 返回 团队成员姓名
     */
    List<LabelValueTeamVo> getTeamUserName(Long UserLong);

    /**
     * 返回 团队成员姓名
     */
    TeamVo getTeamUserNameTwo(Long UserLong);

    /**
     * 判断任务单 状态 state ==0 是空闲
     */
    Boolean getJudgmentTaskList(Long id);

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
    XWPFDocument downloadEntrust(TaskDetailInfoVo taskDetailInfoVo, InputStream object);

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
    String passorno(Integer itemId, Integer state,String opinion);

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
}
