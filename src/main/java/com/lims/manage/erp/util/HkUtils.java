package com.lims.manage.erp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.lims.manage.erp.entity.DoorDetailReq;
import com.lims.manage.erp.entity.HkDoorReq;
import com.lims.manage.erp.entity.HkGrantDoorReq;
import com.lims.manage.erp.entity.PersonDoorReq;
import com.lims.manage.erp.entity.ResourceInfo;

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
    static {
        // 代理API网关nginx服务器ip端口
        ArtemisConfig.host = "192.168.150.10";
        // 秘钥appkey
        ArtemisConfig.appKey = "28884631";
        // 秘钥appSecret
        ArtemisConfig.appSecret = "uFYpLT7a5Ssyz2aInxkf";
    }
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
        String result =ArtemisHttpUtil.doPostStringArtemis(path,jsonBody.toJSONString(),null,null,"application/json",head);
        return DataTypeConversionUtil.getStringToMap(result);
    }


    /**
     * 获取监控点预览取流URL
     * @param id 设备编号
     * @return
     */
    public static Map<String,Object> camerasPreviewURLs(String id){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("cameraIndexCode", id);
        jsonBody.put("protocol", "hls");
        Map<String,Object> returnMap=publicHkInterface(jsonBody,"/api/video/v1/cameras/previewURLs");
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
    public static Map<String,Object> cameraSearch(){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);
        Map<String,Object> returnMap=publicHkInterface(jsonBody,"/api/resource/v2/acsDevice/search");
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
     * 查询门禁点事件v2
     * @return
     */
    public static Map<String,Object> doorEvents(String path, DoorDetailReq doorDetailReq){
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(doorDetailReq));
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 获取门禁设备在线状态
     * @return
     */
    public static Map<String,Object> doorGetState(String path){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);
        Map<String,Object> returnMap=publicHkInterface(jsonBody,path);
        return returnMap;
    }

    /**
     * 获取门禁事件的图片（每个门禁事件的图片）
     * @return
     */
    public static Map<String,Object> doorPictures(String path){
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);
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
        Map<String, Object> events = doorSearch("/api/resource/v2/door/search");
        System.out.println("============"+JSON.toJSONString(events));

    }
}
