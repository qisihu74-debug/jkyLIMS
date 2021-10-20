package com.jifen.manage.demo.service;

import com.jifen.manage.demo.entity.StepsInfoVo;

import java.util.List;

public interface DingService {

    /**
     * 查询某天全部员工的运动步数
     * @param date
     * @param tokenUrl
     * @param deptUrl
     * @param userUrl
     * @param stepUrl
     * @param userInfoUrl
     * @param appKey
     * @param appsecret
     * @return
     */
    List<StepsInfoVo> getAllUserSteps(String date, String tokenUrl, String deptUrl, String userUrl, String stepUrl, String userInfoUrl, String appKey, String appsecret);

}
