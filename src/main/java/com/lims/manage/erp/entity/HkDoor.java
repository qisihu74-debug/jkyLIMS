package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-13 15:46
 * @Copyright © 河南交科院
 */
@Data
@TableName("hk_door")
public class HkDoor {
    /**
     * 门禁标识
     */
    @TableId
    private String indexCode;
    /**
     * 名称
     */
    private String name;
    /**
     * 类型 door
     */
    private String resourceType;
    /**
     * 门禁点编号
     */
    private String doorNo;
    /**
     *门序号
     */
    private Integer doorSerial;

    private String description;
    /**
     * 父级资源编号
     */
    private String parentIndexCode;
    /**
     * 所属区域
     */
    private String regionIndexCode;

    private String treatyType;
    /**
     * 通道类型，door：门禁点
     */
    private String channelType;
    /**
     * 通道号
     */
    private String channelNo;
    /**
     * 一级控制器id
     */
    private String controlOneId;
    /**
     * 二级控制器id
     */
    private String controlTwoId;
    /**
     * 读卡器1
     */
    private String readerInId;
    /**
     * 读卡器2
     */
    private String readerOutId;
    /**
     *
     */
    private Integer isCascade;

    private Integer sort;

    private Integer disOrder;
    /**
     * 区域名称
     */
    private String regionName;

    private String regionPath;

    private String regionPathName;
    @TableField(exist = false)
    private String laboratoryName;
}
