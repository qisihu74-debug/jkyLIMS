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
import com.lims.manage.erp.vo.VisitorVo;

import java.util.ArrayList;
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
        jsonBody.put("protocol", "rtsp");
        //jsonBody.put("protocol", "hls");
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
        for (ResourceInfo resourceInfo : resourceInfos) {
            resourceInfo.setResourceType("door");
        }
        JSONObject jsonBody = JSONObject.parseObject(JSON.toJSONString(hkGrantDoorReq));
        Map<String, Object> returnMap = publicHkInterface(jsonBody, path);
        return returnMap;
    }

    /**
     * 访客预约v2
     *
     * @return
     */
    public static Map<String, Object> visitorModeV2(String path, VisitorVo visitorVo) {
        // 访客ID
//        path = "9527";

        JSONObject jsonBody = new JSONObject();
        // 被访人唯一标识
//        jsonBody.put("receptionistId", "64f7364122464542b7a6daf22b89a3a4");
        // 更换为
        jsonBody.put("receptionistId", visitorVo.getReceptionistId());

        // 预计来访时间
        String startISOStr = DateUtil.getISO8601TimestampFromDateStr(visitorVo.getVisitStartTime());
        System.out.println("ISOStr = " + startISOStr);
        jsonBody.put("visitStartTime", startISOStr);

        // 预计离开时间
        String endISOStr = DateUtil.getISO8601TimestampFromDateStr(visitorVo.getVisitEndTime());
        System.out.println("ISOStr = " + endISOStr);
        jsonBody.put("visitEndTime", endISOStr);

        // 访客信息
        JSONObject visitorInfo1 = new JSONObject();
        // 访客姓名:
        visitorInfo1.put("visitorName", visitorVo.getVisitorName());
        //  访客性别 1-男, 2-女
        visitorInfo1.put("gender", visitorVo.getGender());
        // 联系电话:
        visitorInfo1.put("phoneNo", visitorVo.getPhoneNo());
        // 添加访客信息
        List<Object> visitorInfoList = new ArrayList<>();
        visitorInfoList.add(visitorInfo1);

        // 权限集合封装对象
        // 是否使用默认权限组(1：使用；非1：不使用)
        JSONObject permissionSet = new JSONObject();
        permissionSet.put("defaultPrivilegeGroupFlag", "1");

        jsonBody.put("visitorPermissionSet", permissionSet);
//        String[] arrays = null;
//        jsonBody.put("privilegeGroupIds","");

        jsonBody.put("visitorInfoList", visitorInfoList);
        System.out.println("输出" + jsonBody.toJSONString());
        Map<String, Object> returnMap = publicHkInterface(jsonBody, path);
        System.out.println("============" + JSON.toJSONString(returnMap));
        return returnMap;
    }

    /**
     * 预约免登记
     *
     * @return
     */
    public static Map<String, Object> visitorV1Registration(String path, HkDoorReq hkDoorReq) {
        // 访客ID
//        path = "9527";

        JSONObject jsonBody = new JSONObject();
        // 被访人唯一标识
        jsonBody.put("receptionistId", "64f7364122464542b7a6daf22b89a3a4");

        // 预计来访时间
        String startISOStr = DateUtil.getISO8601TimestampFromDateStr("2024-12-17 09:00:00");
        System.out.println("来访时间 = " + startISOStr);
        jsonBody.put("visitStartTime", startISOStr);

        // 预计离开时间
        String endISOStr = DateUtil.getISO8601TimestampFromDateStr("2024-12-19 23:59:59");
        System.out.println("离开时间 = " + endISOStr);
        jsonBody.put("visitEndTime", endISOStr);

        // 访客信息
        JSONObject visitorInfo1 = new JSONObject();
        // 访客姓名:
        visitorInfo1.put("visitorName", "孙玉好");
        //  访客性别 1-男, 2-女
        visitorInfo1.put("gender", "1");
        // 联系电话:
        visitorInfo1.put("phoneNo", "17737713506");
        // 添加访客信息
        List<Object> visitorInfoList = new ArrayList<>();
        visitorInfoList.add(visitorInfo1);
        jsonBody.put("visitorInfo", visitorInfoList);
        System.out.println("输出" + jsonBody.toJSONString());
        Map<String, Object> returnMap = publicHkInterface(jsonBody, path);
        System.out.println("============" + JSON.toJSONString(returnMap));
        return returnMap;
    }

    /**
     * 生成访客动态二维码内容
     *
     * @return
     */
    public static Map<String, Object> respondVisitorAuthQcode(String path, HkDoorReq hkDoorReq) {
        JSONObject jsonBody = new JSONObject();
        // 访客记录id
        jsonBody.put("orderId", "9b131148bb7811ef8b9d77ce93d08d7b");
        // 时长
        jsonBody.put("duration", "8");
        // 时间单位，1：小时；2：分钟
        jsonBody.put("unit", "1");
        // 二维码使用次数
        jsonBody.put("frequency", "4");
        Map<String, Object> returnMap = publicHkInterface(jsonBody, path);
        System.out.println("生成访客动态二维码内容 ============" + JSON.toJSONString(returnMap));
        return returnMap;
    }


    /**
     * 已预约登记
     *
     * @return
     */
    public static Map<String, Object> visitorOrderRegister(String path, VisitorVo visitorVo) {
        JSONObject jsonBody = new JSONObject();
        // 访客记录id
//        jsonBody.put("orderId", "d6719b19cc3e499a8db73860a481548f");
        jsonBody.put("orderId", visitorVo.getReceptionistId());
        // 预计离开时间
        String endISOStr = DateUtil.getISO8601TimestampFromDateStr(visitorVo.getVisitEndTime());
        jsonBody.put("visitEndTime", endISOStr);
        System.out.println("ISOStr = " + endISOStr);

        // 权限集合封装对象
        // 是否使用默认权限组(1：使用；非1：不使用)
        JSONObject permissionSet = new JSONObject();
        permissionSet.put("defaultPrivilegeGroupFlag", "1");

        jsonBody.put("visitorPermissionSet", permissionSet);
        // 是否使用预约时的权限，1：是
        jsonBody.put("isUseOrderAuth", "1");

        Map<String, Object> returnMap = publicHkInterface(jsonBody, path);
        System.out.println("已预约登记 ============" + JSON.toJSONString(returnMap));
        return returnMap;
    }

    /**
     * 查询访客权限组
     *
     * @param path
     * @return
     */
    public static Map<String, Object> visitorPrivilegeGroup(String path) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);
        Map<String, Object> returnMap = publicHkInterface(jsonBody, path);
        return returnMap;
    }

    /**
     * 查询访客预约记录v2
     *
     * @param receptionistId
     * @param visitorName
     * @param phoneNo
     * @return
     */
    public static Map<String, Object> visitorAppointmentRecords(String path, String receptionistId, String visitorName, String phoneNo) {
        JSONObject jsonBody = new JSONObject();
        if (StringUtils.isNotEmpty(receptionistId)) {
            jsonBody.put("receptionistId", receptionistId);
        }
        if (StringUtils.isNotEmpty(visitorName)) {
            jsonBody.put("visitorName", visitorName);
        }
        if (StringUtils.isNotEmpty(phoneNo)) {
            jsonBody.put("phoneNo", phoneNo);
        }
        jsonBody.put("pageNo", 1);
        jsonBody.put("pageSize", 1000);

        Map<String, Object> returnMap = publicHkInterface(jsonBody, path);
        return returnMap;
    }


    public static void main(String[] args) {
//        Map<String, Object> events = camerasPreviewURLs(null, "02b2785f6a954b2cae25528e2db83a22");
//        Map<String, Object> events1 = cameraSearch("/api/resource/v2/camera/search");
//        System.out.println("============" + JSON.toJSONString(events));
        Map<String, Object> xiangyingObject = visitorModeV2("/api/visitor/v2/appointment", null);
//        System.out.println("============" + JSON.toJSONString(xiangyingObject));
    }
}
