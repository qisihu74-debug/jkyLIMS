package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.EntrustFileTableEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import lombok.Data;

import java.util.List;
@Data
public class TaskDetailInfoVo {
    private Long taskId;//任务主键
    private String taskCode;//任务编号
    private String orderer;//下单人
    private String orderTime;//下单日期
    private String requiredCompletionTime;//完成日期
    private String cost;//本单费用
    private String samplingMethod;//取样方式
    private String checkPurpose;//检验目的
    private String presentInformation;//提供资料
    /**
     * 委托编号
     */
    private String entrustmentNo;
    /**
     * 委托id
     */
    private Long entrustmentId;
    /**
     * 中间报告的委托单id存储字段
     */
    private Long entrustId;
    /**
     * 判定依据
     */
    private String judgmentBasis;
    /**
     *  报告报告用章
     */
    private String sealType;
    /**
     * 委托单报告用章
     */
    private String sealTypeTicket;
    private String fileUrl;//下载附件
    /**
     * url
     */
    private String[] array;
    private List<SampleDetailVo> sampleDetailList;//样品信息

    /**
     * 用章类型数组
     *  sealTypeArray
     */
    private String[] sealTypeArray;
    /**
     * 原材样品信息
     */
    private List<TestSampleEntity> nodeSample;

    /**
     * reportCode
     * 报告编号
     * @param taskId
     */
    private String reportCode;

    /**
     * 委托单附件集合
     */
    List<EntrustFileTableEntity> fileArrays;

    /**
     * 委托单作废原因
     */
    private String invalidReason;

    /**
     * 委托编号
     */
    private String entrustmentNostr;


    public TaskDetailInfoVo(Long taskId) {
        this.taskId = taskId;
    }

    public TaskDetailInfoVo() {
    }
}
