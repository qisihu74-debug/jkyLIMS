package com.jifen.manage.demo.service.impl;

import com.dingtalk.api.response.OapiHealthStepinfoListbyuseridResponse;
import com.dingtalk.api.response.OapiUserListsimpleResponse;
import com.google.common.collect.Maps;
import com.jifen.manage.demo.service.DingService;
import com.jifen.manage.demo.entity.StepsInfoVo;
import com.jifen.manage.demo.util.AccessTokenSingleton;
import com.jifen.manage.demo.util.DingUtils;
import com.jifen.manage.demo.util.ListUtil;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("dingService")
public class DingServiceImpl implements DingService {
    @Override
    public List<StepsInfoVo> getAllUserSteps(String date, String tokenUrl, String deptUrl, String userUrl, String stepUrl, String userInfoUrl, String appKey, String appsecret) {
        List<StepsInfoVo> result= Lists.newArrayList();
        String token = AccessTokenSingleton.getInstance().getToken(tokenUrl, appKey, appsecret);
        //获取部门ID
        List<Long> allDeptId = DingUtils.getAllDeptId(token, deptUrl);
        //根据部门ID获取用户ID
        List<OapiUserListsimpleResponse.ListUserSimpleResponse> userInfos = Lists.newArrayList();
        Map<String,String> map = Maps.newHashMap();
        for (int i = 0; i < allDeptId.size(); i++) {
            List<OapiUserListsimpleResponse.ListUserSimpleResponse> allUserNameAndId = DingUtils.getAllUserNameAndId(token, userUrl, allDeptId.get(i));
            userInfos.addAll(allUserNameAndId);
            for (int j = 0; j < allUserNameAndId.size(); j++) {
                map.put(allUserNameAndId.get(j).getUserid(),allUserNameAndId.get(j).getName());
            }
        }
        //根据用户ID获取用户步数
        List<String> userIdsStr = Lists.newArrayList();
        if(!userInfos.isEmpty() && userInfos.size()>50){
            List<List<OapiUserListsimpleResponse.ListUserSimpleResponse>> lists = ListUtil.fixedGrouping(userInfos, 50);
            for (int i = 0; i <lists.size() ; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < lists.get(i).size(); j++) {
                    if(j!=lists.get(i).size()){
                        stringBuilder.append(lists.get(i).get(j).getUserid()+",");
                    }else{
                        stringBuilder.append(lists.get(i).get(j).getUserid());
                    }
                }
                userIdsStr.add(stringBuilder.toString());
            }
        }

        for (int i = 0; i < userIdsStr.size(); i++) {
            List<OapiHealthStepinfoListbyuseridResponse.BasicStepInfoVo> userSteps = DingUtils.getUserSteps(token, stepUrl, userIdsStr.get(i), date);
            for (int j = 0; j < userSteps.size(); j++) {
                StepsInfoVo vo = new StepsInfoVo();
                vo.setDate(date);
                vo.setSteps(userSteps.get(j).getStepCount());
                vo.setUserId(userSteps.get(j).getUserid());
                vo.setName(map.get(userSteps.get(j).getUserid()));
                result.add(vo);
            }
        }
        return result;
    }
}
