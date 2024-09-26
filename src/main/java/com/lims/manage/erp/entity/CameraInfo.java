package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-26 10:49
 * @Copyright © 河南交科院
 */
@Data
@TableName("hk_camera")
public class CameraInfo {
    @TableId
    private String indexCode;

    private String regionIndexCode;

    private String name;

    private String parentIndexCode;

    private int cameraType;

    private int chanNum;

    private String dacIndexCode;

    private String capability;

    private String channelType;

    private String decodeTag;

    private String resourceType;

    private String createTime;

    private String updateTime;

    private int sort;

    private int disOrder;

    private String cameraRelateTalk;

    private int transType;

    private String treatyType;

    private String recordLocation;

    private int cascadeType;

    private String regionName;

    private String regionPath;

    private String regionPathName;
}
