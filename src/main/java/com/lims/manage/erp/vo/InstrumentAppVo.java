package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.entity.InstrumentUseGroup;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: DLC
 * @Date: 2023/4/4 14:46
 * app端 仪器详情
 */
@Data
public class InstrumentAppVo {
    /**
     * 仪器id
     */
    private Long id;
    /**
     * 仪器记录id
     */
    private Long recordId;
    /**
     * 检测项id
     */
    private Long escRelId;
    /**
     * 管理编号
     */
    private String code;
    /**
     * 仪器设备名称
     */
    private String name;
    /**
     * 规格型号
     */
    private String model;
    /**
     *  生产厂家
     */
    private String manufacturer;
    /**
     * 机身出厂编号
     */
    private String serialNumber;
    /**
     * 检定/校准单位
     */
    private String calibrationCorporation;
    /**
     * 最新检定/校准日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date appraisalDate;
    /**
     * 检定/校准失效日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;
    /**
     * 使用人
     */
    private String user;
    /**
     * 设备状态
     */
    private String deviceState;
    /**
     * 记录档案
     */
    private Map<String,Object> recordFile = new HashMap<>();
    /**
     * 任务单id
     */
    private Long taskId;
    /**
     * 0生成仪器使用记录，1不需要生成（不展示）
     */
    private Integer isShow;
    /**
     * 并线数量（不能并行的仪器默认为0，并行的仪器给出数量）
     */
    private Integer parallel;
    /**
     * 设备组队信息
     */
    private List<InstrumentUseGroup> groupInfo;
}
