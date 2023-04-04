package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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

    private List<CheckItemInfoVo> checkItemInfoList;

}
