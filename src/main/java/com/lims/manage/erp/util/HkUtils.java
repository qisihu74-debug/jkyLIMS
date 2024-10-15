package com.lims.manage.erp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.util.Data;
import com.google.api.client.util.Lists;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.lims.manage.erp.entity.DoorDetailReq;
import com.lims.manage.erp.entity.DoorStateReq;
import com.lims.manage.erp.entity.HkDoorReq;
import com.lims.manage.erp.entity.HkGrantDoorReq;
import com.lims.manage.erp.entity.PersonDoorReq;
import com.lims.manage.erp.entity.ResourceInfo;
import com.lims.manage.erp.result.ResultUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc 海康工具类
 * @date 2024-09-09 16:14
 * @Copyright © 河南交科院
 */
public class HkUtils {
    /**
     * 能力开放平台的网站路径
     */
    private static final String ARTEMIS_PATH = "/artemis";
    /**
     * 通用海康接口
     * 调用POST请求类型(application/json)接口*
     * @return
     */
    public static Map<String,Object> publicHkInterface(JSONObject jsonBody,String url){
        final String getCamsApi = ARTEMIS_PATH +url;
        Map<String, String> path = new HashMap<String, String>(2);
        path.put("https://", getCamsApi);
        // post请求application/json类型参数
        Map<String,String> head = new HashMap<>();
        head.put("tagId","frs");
        head.put("domainId","auto");
        head.put("userId","admin");
        System.out.println("请求路径："+path);
        System.out.println("请求参数："+jsonBody.toJSONString());
        System.out.println("请求头信息："+JSON.toJSONString(head));
        String result =ArtemisHttpUtil.doPostStringArtemis(path,jsonBody.toJSONString(),null,null,"application/json",head);
        return DataTypeConversionUtil.getStringToMap(result);
    }


    /**
     * 获取监控点预览取流URL
     * @param path 设备编号
     * @return
     */
    public static Map<String,Object> camerasPreviewURLs(String path,String cameraIndexCode){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("cameraIndexCode", cameraIndexCode);
//        jsonBody.put("protocol", "rtsp");
        jsonBody.put("protocol", "hls");
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 获取监控点回放取流URLv2
     * @param path
     * @param cameraIndexCode
     * @param starTime
     * @param endTime
     * @return
     */
    public static Map<String,Object> playbackURLs(String path,String cameraIndexCode,String starTime,String endTime){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("cameraIndexCode", cameraIndexCode);
        jsonBody.put("beginTime", starTime);
        jsonBody.put("endTime", endTime);
//        jsonBody.put("protocol", "rtsp");
        jsonBody.put("protocol", "hls");
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * API名称：
     * 查询监控点列表v2
     * 分组：
     * 视频资源接口
     * 提供方名称：
     * 资源目录服务
     * qps：
     * 描述：根据条件查询目录下有权限的监控点列表
     * @return
     */
    public static Map<String,Object> cameraSearch(String path){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 查询门禁点列表v2
     * @return
     */
    public static Map<String,Object> doorSearch(String path){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 查询门禁点状态
     * @return
     */
    public static Map<String,Object> doorState(String path, DoorStateReq doorStateReq){
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(doorStateReq));
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 查询门禁点事件v2
     * @return
     */
    public static Map<String,Object> doorEvents(String path, DoorDetailReq doorDetailReq){
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(doorDetailReq));
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 获取门禁事件的图片（每个门禁事件的图片）
     * @param path 请求路径
     * @param svrIndexCode 提供picUri处会提供此字段
     * @param picUri 图片相对地址
     * @return
     */
    public static Map<String,Object> doorPictures(String path,String svrIndexCode,String picUri){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("svrIndexCode", svrIndexCode);
        jsonBody.put("picUri", picUri);
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 获取人员列表（每个门禁事件的图片）
     * @return
     */
    public static Map<String,Object> personList(String path){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 任务单授权门禁权限(门禁和人员绑定-下发权限)
     *
     * @param bandPath
     * @param grantPath
     * @param indexCodes
     * @param stringMap  设置日期 开始日期 截止日期
     * @return
     */
    public static Boolean taskGrantDoor(String bandPath, String grantPath, List<String> personIds, List<String> indexCodes, Map<String, String> stringMap) {
        HkDoorReq hkDoorReq = new HkDoorReq();
        List<PersonDoorReq> personDatas = Lists.newArrayList();
        PersonDoorReq personDoorReq = new PersonDoorReq();
        personDoorReq.setIndexCodes(personIds);
        personDatas.add(personDoorReq);
        hkDoorReq.setPersonDatas(personDatas);

        List<ResourceInfo> resourceInfos = Lists.newArrayList();
        for (String string : indexCodes) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setResourceIndexCode(string);
            resourceInfos.add(resourceInfo);
        }
        hkDoorReq.setResourceInfos(resourceInfos);
        //设置日期 根据传参即可
        hkDoorReq.setStartTime(stringMap.get("startTime"));
        hkDoorReq.setEndTime(stringMap.get("endTime"));

        //设置固定参数
        List<PersonDoorReq> personDatas1 = hkDoorReq.getPersonDatas();
        for (PersonDoorReq personDoorReq1 : personDatas1) {
            personDoorReq1.setPersonDataType("person");
        }
        List<ResourceInfo> resourceInfos1 = hkDoorReq.getResourceInfos();
        for (ResourceInfo resourceInfo : resourceInfos1) {
            resourceInfo.setResourceType("door");
        }
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(hkDoorReq));
        Map<String,Object> returnMap=publicHkInterface(jsonBody,bandPath);
        if (returnMap != null){
            String msg = returnMap.get("msg").toString();
            if ("success".equals(msg)){
                //权限下发
                //构造参数
                HkGrantDoorReq hkGrantDoorReq = new HkGrantDoorReq();
                List<ResourceInfo> resourceInfoList = Lists.newArrayList();
                for (String string :indexCodes){
                    ResourceInfo resourceInfo = new ResourceInfo();
                    resourceInfo.setResourceIndexCode(string);
                    resourceInfoList.add(resourceInfo);
                }
                hkGrantDoorReq.setResourceInfos(resourceInfoList);

                if (hkGrantDoorReq.getTaskType() == 0){
                    hkGrantDoorReq.setTaskType(4);
                }
                List<ResourceInfo> reqResourceInfos = hkGrantDoorReq.getResourceInfos();
                for (ResourceInfo resourceInfo: reqResourceInfos){
                    resourceInfo.setResourceType("door");
                }
                JSONObject parseObject = JSONObject.parseObject(JSON.toJSONString(hkGrantDoorReq));
                Map<String,Object> map=publicHkInterface(parseObject,grantPath);
                if (map != null) {
                    String msg1 = map.get("msg").toString();
                    if ("success".equals(msg1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 添加权限配置(门禁和人员绑定)
     * @return
     */
    public static Map<String,Object> personBandDoor(String path, HkDoorReq hkDoorReq){
        //设置固定参数
        List<PersonDoorReq> personDatas = hkDoorReq.getPersonDatas();
        for (PersonDoorReq personDoorReq :personDatas){
            personDoorReq.setPersonDataType("person");
        }
        List<ResourceInfo> resourceInfos = hkDoorReq.getResourceInfos();
        for (ResourceInfo resourceInfo: resourceInfos){
            resourceInfo.setResourceType("door");
        }
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(hkDoorReq));
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 取消权限配置
     * @return
     */
    public static Map<String,Object> cancleBandDoor(String path, HkDoorReq hkDoorReq){
        //设置固定参数
        List<PersonDoorReq> personDatas = hkDoorReq.getPersonDatas();
        for (PersonDoorReq personDoorReq :personDatas){
            personDoorReq.setPersonDataType("person");
        }
        List<ResourceInfo> resourceInfos = hkDoorReq.getResourceInfos();
        for (ResourceInfo resourceInfo: resourceInfos){
            resourceInfo.setResourceType("door");
        }
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(hkDoorReq));
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 根据出入权限快速配置（人员授权门禁）
     * @return
     */
    public static Map<String,Object> personGrant(String path, HkGrantDoorReq hkGrantDoorReq){
        //构造参数
        if (hkGrantDoorReq.getTaskType() == 0){
            hkGrantDoorReq.setTaskType(4);
        }
        List<ResourceInfo> resourceInfos = hkGrantDoorReq.getResourceInfos();
        for (ResourceInfo resourceInfo: resourceInfos){
            resourceInfo.setResourceType("door");
        }
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(hkGrantDoorReq));
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }





    public static void main(String[] args) {
        Map<String, Object> events = camerasPreviewURLs(null,"02b2785f6a954b2cae25528e2db83a22");
        Map<String, Object> events1 = cameraSearch("/api/resource/v2/camera/search");
        System.out.println("============"+JSON.toJSONString(events));

    }
}
