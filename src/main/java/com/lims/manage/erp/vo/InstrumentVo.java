package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.entity.InstrumentUseGroup;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/4/4 14:46
 * app端 扫码仪器
 */
@Data
public class InstrumentVo {
    /**
     * 仪器id
     */
    private Long id;
    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    /**
     * 设备状态
     */
    private String deviceState;
    /**
     * 环境温度
     */
    private String environmentTemperature;
    /**
     * 环境湿度
     */
    private String ambientHumidity;
    /**
     * 使用人
     */
    private String user;

    private List<CheckItemInfoVo> checkItemInfoList = new ArrayList<>();
    /**
     * 设备使用记录
     */
    private List<InstrumentRecordListVo> instrumentRecordListVos = new ArrayList<>();

    private String taskCode;
    private Long taskId;
    private Long taskIds;

    /**
     * 0生成仪器使用记录，1不需要生成（不展示）
     */
    private Integer isShow;
    /**
     * 并线数量（不能并行的仪器默认为0，并行的仪器给出数量）
     */
    private Integer parallel;
    /**
     * 组队样品数量
     */
    private Integer sampleSize;

    private InstrumentUseGroup instrumentUseGroup;

    //0：正常记录；1：插单
    private Integer recordType;//记录类型

}
