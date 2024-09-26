package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.CameraInfo;
import com.lims.manage.erp.entity.HkDoor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-09-26 11:02
 * @Copyright © 河南交科院
 */
public interface HkCameraDao extends BaseMapper<CameraInfo> {
    List<HkDoor> cameraList(@Param("name") String name, @Param("position") String position, @Param("state") String state);
}
