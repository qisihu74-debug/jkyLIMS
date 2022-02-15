package com.lims.manage.erp.service;

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
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    List<TaskListVo> getTaskList(TaskListParamVo paramVo);

    /**
     * 查询领样列表
     *
     * @param paramVo
     * @return
     */
    List<ReceiveSampleListVo> getSampleList(TaskListParamVo paramVo);

    /**
     * 领样
     *
     * @param paramVo
     * @return
     */
    int receiveSample(ReceiveSampleParamVo paramVo);

//    List<TaskDetailInfoVo> getTaskDetailInfo();

    /**
     * 副团长抢单并 派发 团队人员 操作
     */
    Boolean postGrabASingle(TaskTestEntity taskTestEntity);

    /**
     * 返回 团队成员姓名
     */
    List<LabelValueTeamVo> getTeamUserName(Long UserLong);

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
    OriginalRecordDataVo getOriginalData(Long taskId, Integer sampleId, Integer checkItemId);

    /**
     * 查询原始记录模板
     *
     * @param checkItemId
     * @return
     */
    String getOriginalTemplate(Integer checkItemId);

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
    int passorno(Integer itemId, Integer state,String opinion);

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
