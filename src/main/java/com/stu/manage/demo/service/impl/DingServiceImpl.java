package com.stu.manage.demo.service.impl;

import com.dingtalk.api.response.OapiHealthStepinfoListbyuseridResponse;
import com.stu.manage.demo.entity.StepsInfoVo;
import com.stu.manage.demo.service.DingService;
import com.stu.manage.demo.util.AccessTokenSingleton;
import com.stu.manage.demo.util.DingUtils;
import com.stu.manage.demo.util.ListUtil;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dingService")
public class DingServiceImpl implements DingService {
    @Override
    public List<StepsInfoVo> getAllUserSteps(String date, String tokenUrl, String deptUrl, String userUrl, String stepUrl, String userInfoUrl, String appKey, String appsecret) {
        List<StepsInfoVo> result= Lists.newArrayList();
        String token = AccessTokenSingleton.getInstance().getToken(tokenUrl, appKey, appsecret);
        //获取部门ID
        List<Long> allDeptId = DingUtils.getAllDeptId(token, deptUrl);
        //根据部门ID获取用户ID
        List<String> userIds = Lists.newArrayList();
        for (int i = 0; i < allDeptId.size(); i++) {
            List<String> userId = DingUtils.getUserId(token, userUrl, allDeptId.get(i));
            userIds.addAll(userId);
        }
        //根据用户ID获取用户步数
        List<String> userIdsStr = Lists.newArrayList();
        if(!userIds.isEmpty() && userIds.size()>50){
            List<List<String>> lists = ListUtil.fixedGrouping(userIds, 50);
            for (int i = 0; i <lists.size() ; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < lists.get(i).size(); j++) {
                    if(j!=lists.get(i).size()){
                        stringBuilder.append(lists.get(i).get(j)+",");
                    }else{
                        stringBuilder.append(lists.get(i).get(j));
                    }
                }
                userIdsStr.add(stringBuilder.toString());
            }
        }

        for (int i = 0; i < userIdsStr.size(); i++) {
            List<OapiHealthStepinfoListbyuseridResponse.BasicStepInfoVo> userSteps = DingUtils.getUserSteps(token, stepUrl, userIdsStr.get(i), date);
            for (int j = 0; j < userSteps.size(); j++) {
                StepsInfoVo vo = new StepsInfoVo();
                vo.setSteps(userSteps.get(j).getStepCount());
                vo.setUserId(userSteps.get(j).getUserid());
                result.add(vo);
            }
        }
        return result;
    }
}
