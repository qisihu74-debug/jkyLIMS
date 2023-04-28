package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/task/")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    TestDetectionService testDetectionService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;

    /**
     * 查询任务详情——废弃
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/getTaskDetailInfo")
    public Result getTaskDetailInfo(Long taskId) {
        if (taskId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success("查询任务详情成功！", taskService.getTaskDetailInfo(taskId));
        }
    }

    /**
     * 查询任务详情——线上使用
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/getTaskDetailInfo_two")
    public Result getTaskDetailInfo_two(Long taskId) {
        if (taskId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            if (userInfo == null) {
                return ResultUtil.error("token 已过期！");
            }

            // 根据账号 查询有可能包含多个科室 以及下级科室信息
            String dept = taskService.getDeptIds(userInfo.getUserId());
            // 科室id集合
            String[] deptIds = new String[]{};
            if (dept != null) {
                deptIds = dept.split(",");
            }
            return ResultUtil.success("查询任务详情成功！", taskService.getTaskDetailInfoTwo(taskId, deptIds));
        }
    }

    /**
     * 试验开始下 查询任务详情（检测项无价格不展示）——线上使用
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/getTaskTestDetails")
    public Result getTaskTestDetails(Long taskId) {
        if (taskId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            if (userInfo == null) {
                return ResultUtil.error("token 已过期！");
            }

            // 根据账号 查询有可能包含多个科室 以及下级科室信息
            String dept = taskService.getDeptIds(userInfo.getUserId());
            // 科室id集合
            String[] deptIds = new String[]{};
            if (dept != null) {
                deptIds = dept.split(",");
            }
            return ResultUtil.success("查询任务详情成功！", taskService.getTaskTestDetails(taskId, deptIds));
        }
    }

    /**
     * 副团长抢单
     *
     * @param taskTestEntity
     * @return
     */
    @PostMapping("postGrabASingle")
    public Result postGrabASingle(@RequestBody TaskTestEntity taskTestEntity) {
/*        if (ShiroUtils.getUserInfo() != null) {
            // 抢单人
            Long strLong = ShiroUtils.getUserInfo().getUserId();
            String str1 = String.valueOf(strLong);
            taskTestEntity.setReceiver(str1);
        }
        Boolean taskStatus = taskService.getJudgmentTaskList(taskTestEntity.getId());
        if (taskStatus) {
            Boolean flag = taskService.postGrabASingle(taskTestEntity);
            if (flag) {
                return ResultUtil.success("抢单成功");
            }
            return ResultUtil.error(678, "抢单失败！");
        }*/
        return ResultUtil.error(678, "当前任务单已经被抢！");
    }

    /**
     * 领取任务单
     *
     * @param taskTestEntity
     * @return
     */
    @PostMapping("postGrabASingle_two")
    public Result postGrabASingle_two(@RequestBody TaskTestEntity taskTestEntity) {
        if (ShiroUtils.getUserInfo() != null) {
            // 抢单人
            Long strLong = ShiroUtils.getUserInfo().getUserId();
            String str1 = String.valueOf(strLong);
            taskTestEntity.setReceiver(str1);
        } else {
            return ResultUtil.error(201, "token已过期！");
        }
        if (taskTestEntity.getInspector() == null || taskTestEntity.getRecorder() == null || taskTestEntity.getReviewer() == null
                || taskTestEntity.getReportProducer() == null || taskTestEntity.getSampler() ==null) {
            return ResultUtil.error(201, "缺少必填参数！");
        }
        // 委托单144 则不能执行任务单
        if(taskService.judgeTaskStatus(taskTestEntity.getId())){
            return ResultUtil.error(678, "领取失败！任务单已废弃！！！");
        }
        Boolean taskStatus = taskService.getJudgmentTaskList(taskTestEntity.getId());
        if (taskStatus) {
            Boolean flag = taskService.postGrabASingleTwo(taskTestEntity);
            if (flag) {
                return ResultUtil.success("领取成功");
            }
            return ResultUtil.error(678, "领取失败！");
        }
        return ResultUtil.error(678, "当前任务单已经被领！");
    }

    /**
     * 批量领取任务
     * @param batchReceiveTaskVo
     * @return
     */
    @PostMapping("batchPostGrabASingle")
    public Result batchPostGrabASingle(@RequestBody BatchReceiveTaskVo batchReceiveTaskVo) {
        if (ShiroUtils.getUserInfo() != null) {
            // 抢单人
            Long strLong = ShiroUtils.getUserInfo().getUserId();
            String str1 = String.valueOf(strLong);
            batchReceiveTaskVo.setReceiver(str1);
        } else {
            return ResultUtil.error(201, "token已过期！");
        }
        if (batchReceiveTaskVo.getInspector() == null || batchReceiveTaskVo.getRecorder() == null || batchReceiveTaskVo.getReviewer() == null
                || batchReceiveTaskVo.getReportProducer() == null || batchReceiveTaskVo.getSampler() ==null) {
            return ResultUtil.error(201, "缺少必填参数！");
        }
        List<Long> ids = batchReceiveTaskVo.getId();
        List<TaskTestEntity> batchUpdate = Lists.newArrayList();

        if(CollectionUtils.isEmpty(ids)){
            return ResultUtil.error(678, "请选择要领取的任务单！");
        }else{
            java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
            for (Long id : ids) {
                // 委托单144 则不能执行任务单
                if(taskService.judgeTaskStatus(id)){
                    return ResultUtil.error(678, "批量领取失败！任务单已废弃！！！");
                }
                Boolean taskStatus = taskService.getJudgmentTaskList(id);
                //查询样品外观描述
                List<String> sampleOutward = taskService.getSampleOutward(id);
                StringBuilder outward = new StringBuilder();
                if(!CollectionUtils.isEmpty(sampleOutward)){
                    for (int i = 0; i < sampleOutward.size(); i++) {
                        outward.append(sampleOutward.get(i));
                        if(i!=sampleOutward.size()-1){
                            outward.append("/");
                        }
                    }
                }
                if (taskStatus) {
                    //构造修改对象，状态1==（领样/任务单领取）
                    TaskTestEntity entity = new TaskTestEntity(id,batchReceiveTaskVo,1,currentDate,outward.toString());
                    batchUpdate.add(entity);
                }else{
                    return ResultUtil.error(678, "选择的任务单中包含已领取任务单，请刷新页面后重新领取！");
                }
            }
        }
        Boolean flag = taskService.batchPostGrabASingle(batchUpdate);
        if (flag) {
            return ResultUtil.success("领取成功");
        }
        return ResultUtil.error(678, "领取失败！");
    }

    /**
     * 返回 团队姓名 前后端已废弃 （丁）
     *
     * @return
     */
    @RequestMapping("getTeamUserName")
    public Result getTeamUserName() {
        if (ShiroUtils.getUserInfo() != null) {
            // 抢单人
            List<LabelValueTeamVo> returnList = taskService.getTeamUserName(ShiroUtils.getUserInfo().getUserId());
            if (returnList != null && returnList.isEmpty()) {
                return ResultUtil.error(204, "数据为空！");
            }
            return ResultUtil.success(returnList);
        }
        return ResultUtil.error(502, "token过期！");
    }

    /**
     * 返回 团队姓名
     *
     * @return
     */
    @RequestMapping("getTeamUserName_two")
    public Result getTeamUserName_two() {
        if (ShiroUtils.getUserInfo() != null) {
            // 领取人
            TeamVo returnList = taskService.getTeamUserNameTwo(ShiroUtils.getUserInfo().getUserId());
            return ResultUtil.success(returnList);
        }
        return ResultUtil.error(502, "token过期！");
    }


    /**
     * 查询任务列表
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/getTaskList")
    public Result getTaskInfo(@RequestBody TaskListParamVo paramVo) {
        if (paramVo == null  || paramVo.getPageNum() == null || paramVo.getPageSize() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            if (userInfo == null) {
                return ResultUtil.error("token 已过期！");
            }
            // 根据账号 查询有可能包含多个科室 以及下级科室信息
            String dept = taskService.getDeptIds(userInfo.getUserId());
            // 科室id集合
            String[] deptIds = new String[]{};
            if (dept != null) {
                deptIds = dept.split(",");
            } else {
                return ResultUtil.error("账号使用人未配置科室人员");
            }
            return ResultUtil.success("查询任务列表成功！", taskService.getTaskList(paramVo, deptIds));
        }
    }

    /**
     * 查询检测列表
     * 根据科室进行展示数据
     *
     * @param paramVo
     * @return
     */
    @PostMapping(value = "/getTaskList_two")
    public Result getTaskInfo_two(@RequestBody TaskListParamVo paramVo) {

        if (paramVo == null || paramVo.getPageNum() == null || paramVo.getPageSize() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            if (userInfo == null) {
                return ResultUtil.error("token 已过期！");
            }
            // 根据账号 查询有可能包含多个科室 以及下级科室信息
            String dept = taskService.getDeptIds(userInfo.getUserId());
            // 科室id集合
            String[] deptIds = new String[]{};
            if (dept != null) {
                deptIds = dept.split(",");
            } else {
                return ResultUtil.error("账号使用人未配置科室人员");
            }
            paramVo.setReviewer(userInfo.getUserId().toString());
            paramVo.setInspector(userInfo.getUserId().toString());
            return ResultUtil.success("查询任务列表成功！", taskService.getTaskListTwo(paramVo, deptIds));
        }
    }

    /**
     * 查询领样列表
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/getSampleList")
    public Result getSampleList(@RequestBody TaskListParamVo paramVo) {
        if (paramVo == null || paramVo.getPageNum() == null || paramVo.getPageSize() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success("查询领样列表成功！", taskService.getSampleList(paramVo));
        }
    }

    /**
     * 领样
     *
     * @param paramVo
     * @return
     */
    @RequestMapping("/receiveSample")
    public Result receiveSample(@RequestBody ReceiveSampleParamVo paramVo) {
        if (paramVo == null || paramVo.getSampler() == null || "".equals(paramVo.getSampler())
                || paramVo.getTaskId() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            // 领样人姓名与任务单ID  效验是否属于同一部门
            if (taskService.isIntendedEffectReceive(paramVo.getTaskId(), paramVo.getSampler())) {
                taskService.receiveSample(paramVo);
                return ResultUtil.success("领样成功！");
            }
            return ResultUtil.error("领样失败！领样人姓名不属于此任务单下团队成员");
        }
    }

//    /**
//     * 下载原始记录
//     *
//     * @param taskId
//     * @param sampleId
//     * @param checkItemId
//     * @param itemId
//     * @param response
//     */
//    @RequestMapping(value = "/downloadOriginalRecord")
////    @CrossOrigin()
//    public void downloadOriginalRecord(Long taskId,
//                                       Integer sampleId,
//                                       Integer checkItemId,
//                                       Integer itemId,
//                                       HttpServletResponse response) {
//        OriginalRecordDataVo originalData = taskService.getOriginalData(taskId, sampleId, checkItemId,itemId);
//        Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
//        result.put("result", originalData);
//        //从文件服务器获取文件流
//        String originalTemplate = taskService.getOriginalTemplateUrl(checkItemId);
//        if(originalTemplate==null){
//            log.error(checkItemId+"\t无原始记录模板为null");
//        }
//        String[] split = originalTemplate.split("/");
//        String[] split1 = split[4].split("\\?");
//        XLSTransformer transformer = new XLSTransformer();
////        InputStream fileStream = MinIoUtil.getFileStream("original-record-template", originalTemplate);
//        InputStream fileStream = MinIoUtil.getFileStream("file-resources", split1[0]);
//        org.apache.poi.ss.usermodel.Workbook workbook = null;
//        try {
//            workbook = transformer.transformXLS(fileStream, result);
//            response.reset();
//            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//            response.setContentType("application/x-msdownload");
//            response.setCharacterEncoding("UTF-8");
//            String fileName2 = URLEncoder.encode(split1[0], "UTF-8");
//            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName2);
//            OutputStream outputStream = response.getOutputStream();
//            workbook.write(outputStream);
//            outputStream.close();
//        } catch (IOException | InvalidFormatException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 预览原始记录
     *
     * @param taskId
     * @param sampleId
     * @param checkItemId
     * @param itemId
     * @param response
     */
    /*@RequestMapping(value = "/previewOriginalRecord")
//    @CrossOrigin()
    public void previewOriginalRecord(Long taskId,
                                       Integer sampleId,
                                       Integer checkItemId,
                                       Integer itemId,
                                       HttpServletResponse response) {
        OriginalRecordDataVo originalData = taskService.getOriginalData(taskId, sampleId, checkItemId,itemId);
        Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
        result.put("result", originalData);
        //从文件服务器获取文件流
        String originalTemplate = taskService.getOriginalTemplateUrl(checkItemId);
        if(originalTemplate==null){
            log.error(checkItemId+"\t无原始记录模板为null");
        }
        String[] split = originalTemplate.split("/");
        String[] split1 = split[4].split("\\?");
        XLSTransformer transformer = new XLSTransformer();
//        InputStream fileStream = MinIoUtil.getFileStream("original-record-template", originalTemplate);
        InputStream fileStream = MinIoUtil.getFileStream("file-resources", split1[0]);
        Workbook workbook = null;
        try {
            workbook = transformer.transformXLS(fileStream, result);
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i=1;i<numberOfSheets;i++) {
                workbook.removeSheetAt(1);
            }
            ExcelConvertPdf.excelConvertPdf(workbook,response.getOutputStream(),response);
        } catch (Exception e) {
            log.error("原始记录转换pdf预览失败:{}",e);
        }
    }*/

    /**
     * 下载任务通知单 废弃 页面已经不使用
     * 下载doc 任务单信息
     * @param taskId
     * @param response
     */
    @RequestMapping("downloadEntrust")
    public void downloadEntrust(Long taskId, HttpServletResponse response) {
        String fileName = "taskOrder11.docx";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_task_template, fileName);
            TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfoTwo(taskId,null);
            XWPFDocument document = taskService.downloadEntrust(taskDetailInfo, object);
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            //document.close();
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
        }
    }

    /**downloadOriginalRecord
     * 下载任务通知单——二次开发 丁 线上使用中
     * 变更需求 world 转 pdf。
     *
     * @param taskId
     * @param response
     */
    @GetMapping("downloadEntrust_two")
    public void downloadEntrust_two(Long taskId, HttpServletResponse response) {
        String fileName = "";
        // 3月13日前
        String str1 = "taskOrder11.docx";
        // 2023年3月13 零点后
        String str2 = "taskOrder20.docx";
        // 获取任务单下单时间 进行比较
        TaskTestEntity taskDetails = taskMapper.getTaskOrderTime(taskId);
        if(taskDetails!=null && taskDetails.getOrderTime()!=null){
            Date date = null;
            //实现将字符串转成⽇期类型
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse("2023-03-12 23:59:59");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 截止至 2023-03-12 23:59:59 后 任务单附件为  String str2 = "taskOrder20.docx"
             if(date.getTime() < taskDetails.getOrderTime().getTime()){
                 fileName = str2;
             }
             else {
                 fileName = str1;
             }
        }
        String url = "";
        String downloadFileName = "任务单编号";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_task_template, fileName);
            TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfoTwo(taskId, null);
            XWPFDocument doc = taskService.downloadEntrust(taskDetailInfo, object);
            //相应pdf
            ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
            InputStream inputStream = FileAndFolderUtil.parseOut(b1);
            //TODO 设置签名信息
            /** 设置文件下载名 （任务单号+检测项名）**/
            /** 不同文件的MimeType参考后续链接 **/
            downloadFileName = taskDetailInfo.getTaskCode();
//            response.setContentType("application/pdf");//下面三行是关键代码，处理乱码问题
//            response.setCharacterEncoding("utf-8");
//            response.setHeader("Content-Disposition", "attachment;filename=" + new String(downloadFileName.getBytes("utf-8"), "iso8859-1") + "." + "pdf");
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
//            url = MinIoUtil.upload("task-download", taskId + ".pdf", inputStream, "application/octet-stream");
        } catch (Exception ex) {
            log.info("导出失败：{}", ex);
        }
    }

    /**
     * 上传原始记录
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/uploadOriginalRecord")
    public Result uploadOriginalRecord(@RequestParam("json") String json, MultipartFile file) {
        OriginalRecordParamVo paramVo = JSON.parseObject(json, OriginalRecordParamVo.class);
        // 验证登录人userId 是否具备上传原始记录资格
        // 验证登录人信息 和部门 存入
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (testDetectionService.VerifyTheLogin(userInfo.getUserId(), paramVo.getTaskId()) == false) {
            return ResultUtil.error("登录人没有被派发检测资格");
        }
        int i = taskService.uploadOriginalRecord(paramVo, file);
        if (i > 0) {
            if (i == 2) {
                return ResultUtil.error("文件已存在");
            }
            return ResultUtil.success("上传原始记录成功！", i);
        } else {
            return ResultUtil.error(101, "上传原始记录失败！");
        }
    }
    /**
     * 批量上传原始记录 uploading_batch。
     */
    @PostMapping("/uploadingBatch")
    public Result uploadingBatch(String[] ids, MultipartFile file) {

        if (ids == null ) {
            return ResultUtil.error("缺少必填参数！");
        }
        if (file == null) {
            return ResultUtil.error("样品file文件为空！");
        }
        List<Integer> intIds = new ArrayList<>();
        for(int i=0;i<ids.length;i++){
            // 字符格式 转义后 int类型。
            intIds.add(Integer.valueOf(ids[i]));
        }
        if(taskService.effectDataSet(intIds)==false){
            return ResultUtil.error("文件已存在");
        }
        if (taskService.uploadingBatch(intIds, file) == true) {
            return ResultUtil.success("样品文件上传成功");
        }
        return ResultUtil.error("样品文件上传失败");
    }

    @RequestMapping("/review")
    public Result review(Integer itemId) {
        if (itemId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(taskService.getReviewInfo(itemId));
        }
    }

    /**
     * 删除附件post_select_instrument
     * @param itemId
     * @return
     */
    @RequestMapping("/passorno_delete")
    public Result passorno_delete(Integer itemId) {
        if (itemId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        return ResultUtil.success(taskService.passorno_delete(itemId));

    }

    /**
     * 驳回=4，通过=3，撤回=1
     * @param itemId
     * @param state
     * @param opinion
     * @return
     */
    @RequestMapping("/passorno")
    public Result passorno(Integer itemId, Integer state, String opinion) {
        if (itemId == null || state == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            // 验证登录人userId 是否具备操作资格
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            if (userInfo == null) {
                return ResultUtil.error("token 已过期！");
            }
            // 通过检测项主键 获取test_task Id
            Long taskId = taskMapper.getReturnTaskId(itemId);
            // 委托单144 则不能执行任务单
            if(taskService.judgeTaskStatus(taskId)){
                return ResultUtil.error(678, "操作失败！任务单已废弃！！！");
            }
            if (state == 1) {
                if (testDetectionService.VerifyTheLogin(userInfo.getUserId(), taskId) == false) {
                    return ResultUtil.error("登录人没有被派发检测资格");
                }
            }
            if (state == 3 || state == 4) {
                if (testDetectionService.reviewTheLogin(userInfo.getUserId(), taskId) == false) {
                    return ResultUtil.error("登录人没有被派发复核资格");
                }
            }
            return ResultUtil.success(taskService.passorno(itemId, state, opinion));
        }
    }

    @GetMapping("/getPersonInfo")
    public Result getPersonInfo(Long taskId) {
        PersonInfoVo personInfo = null;
        if (taskId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            personInfo = taskService.getPersonInfo(taskId);
            return ResultUtil.success(personInfo);
        }
    }

    @RequestMapping("/updatePersonInfo")
    public Result updatePersonInfo(@RequestBody PersonInfoVo vo) {
        if (vo.getTaskId() == null || vo.getInspector() == null || vo.getRecorder() == null || vo.getReviewer() == null || vo.getReportProducer() == null||vo.getSampler()==null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            int i = taskService.updatePersonInfo(vo);
            if (i > 0) {
                return ResultUtil.success("修改人员信息成功！", i);
            }
            return ResultUtil.error("修改人员信息失败！");
        }
    }

    /**
     * 预览原始记录
     * @param url
     * @return
     */
    @RequestMapping(value = "/previewOriginalRecord")
    public Result previewOriginalRecord(String url,HttpServletResponse response){
        if (StringUtils.isEmpty(url)){
            return ResultUtil.error("原始记录未上传！");
        }
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            String[] split = url.split("\\?");
            String[] strings = split[0].split("\\/");
            String fileName = strings[4];
            String[] names = fileName.split("\\.");
            File file = FileAndFolderUtil.getFile(split[0]);
            InputStream in = new FileInputStream(file);
            if ("xls".equals(names[1]) || "xlsx".equals(names[1])){
                Workbook workbook = new Workbook();
                workbook.loadFromStream(in);
                //获取第一个工作表
                Worksheet sheet = workbook.getWorksheets().get(0);
                //调用方法将Excel保存为图片
                String basePath = qiYueSuoEntity.getAutographPath()+names[0]+".png";
                String pdfPath = qiYueSuoEntity.getAutographPath()+names[0]+".pdf";
                sheet.saveToImage(basePath);
                log.info("临时图片保存成功:{}",basePath);
                //读取临时图片文件输出
                File file1 = new File(basePath);
                InputStream fileInputStream = new FileInputStream(file1);
                //图片流转pdf
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(pdfPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int data;
                try {
                    while((data = fileInputStream.read()) != -1) {
                        out.write(data);
                    }
                    out.close();
                } catch (IOException e) {
                    log.error("图片转pdf异常:{}",e);
                }
                //输出pdf
                File pdfFile = new File(pdfPath);
                InputStream pdfInput = new FileInputStream(pdfFile);
                int i = IOUtils.copy(pdfInput, outputStream);
                //删除临时文件
                FileAndFolderUtil.delete(basePath);
                FileAndFolderUtil.delete(pdfPath);
                log.info("临时转换的图片删除成功！");
                in.close();
                pdfInput.close();
                fileInputStream.close();
                outputStream.close();
                return ResultUtil.success("ok");
            }else if ("png".equals(names[1]) || "jpg".equals(names[1]) || "jpeg".equals(names[1])){
                byte[] data = new byte[in.available()];
                in.read(data);
                BASE64Encoder encoder = new BASE64Encoder();
                String encode = encoder.encode(data);
                return ResultUtil.success(encode);
            }else {
                int i = IOUtils.copy(in, outputStream);   // copy流数据,i为字节数
                in.close();
                outputStream.close();
                return ResultUtil.success("ok");
            }
        }catch (Exception e){
            log.error("预览原始记录模板失败:{}",e);
            return ResultUtil.error("预览原始记录模板失败");
        }
    }

    /**
     * 批量复核通过
     */
    /**
     * itemId 主键
     * state 驳回=4，通过=3，撤回=1
     * 备注 opinion
     * @return
     */
    @RequestMapping("/batchReview")
    public Result batchReview(@RequestBody TaskStatsVo taskStatsVo) {
        if(taskStatsVo.getTaskId()==null || taskStatsVo.getIntegers().length==0||taskStatsVo.getIntegers()==null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
            // 验证登录人userId 是否具备操作资格
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            if (userInfo == null) {
                return ResultUtil.error("token 已过期！");
            }
            // 验证该账号 是否具备检测资格。
            if (testDetectionService.reviewTheLogin(userInfo.getUserId(), taskStatsVo.getTaskId()) == false) {
                return ResultUtil.error("登录人没有被派发复核资格");
            }

            return ResultUtil.success(taskService.batchReview(taskStatsVo));
    }

    /**
     * 批量下载原始记录
     * 根据检测项id
     * @param taskStatsVo
     * @return
     */
    @RequestMapping("/batchDownloadOriginalRecord")
    public void batchDownloadOriginalRecord(@RequestBody TaskStatsVo taskStatsVo,HttpServletResponse response) throws IOException {
        if(taskStatsVo.getIntegers().length==0||taskStatsVo.getIntegers()==null){
            log.info("批量下载原始记录 integers = "+ taskStatsVo.getIntegers().toString());
        }
        // 效验 检测项url信息模板
        // 通过检测项id 获取 相应的 id关联信息。
        List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(taskStatsVo.getIntegers());
        // 判断 压缩数据=null 返回 null
        if(!CollectionUtils.isEmpty(dataEntitys)) {
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/zip");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode("原始记录.zip", "UTF-8"));
            ZipOutputStream zipOutputStream = taskService.packagingWorkbookZip(dataEntitys, response,null);
            zipOutputStream.flush();
        }
    }

    /**
     * 下载原始记录
     *
     * @param taskId
     * @param sampleId
     * @param checkItemId
     * @param itemId
     * @param response
     */
    @RequestMapping(value = "/downloadOriginalRecord")
//    @CrossOrigin()
    public void downloadOriginalRecord(Long taskId,
                                       Integer sampleId,
                                       Integer checkItemId,
                                       Integer itemId,
                                       HttpServletResponse response) throws IOException {
        Integer[] ids = new Integer[1];
        ids[0] = itemId;
        // 效验 检测项url信息模板
        // 通过检测项id 获取 相应的 id关联信息。
        List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(ids);
        // 判断 压缩数据=null 返回 null
        if(!CollectionUtils.isEmpty(dataEntitys)){
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/zip");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode("原始记录.zip", "UTF-8") );
            ZipOutputStream zipOutputStream = taskService.packagingWorkbookZip(dataEntitys,response,taskId);
            zipOutputStream.flush();
        }

    }

    /**
     * 复核：中间及最终检测项(返回pdf)
     *  list
     *  checkReview
     */
    @RequestMapping(value = "/checkItemReview")
//    public void checkItemReview(List<Integer> list,String checkReview){
    public void previewDownLoad(@RequestBody ExcelInsertVo excelInsertVo , HttpServletResponse response) throws Exception {
        String newFilePath = qiYueSuoEntity.getAutographPath() + GenID.getID() + ".xlsx";
        String path = qiYueSuoEntity.getAutographPath()+GenID.getID()+".pdf";
        // excel 转 pdf
        XSSFWorkbook wb = taskService.getOriginalRecordAttachment(excelInsertVo);
        FileOutputStream out = new FileOutputStream(newFilePath);
        wb.write(out);
        out.flush();//刷新
        out.close();//关闭
        InputStream out000 = new FileInputStream(newFilePath);
        //相应pdf
        ByteArrayOutputStream b1 = PDFHelper3.excel2pdf2(out000,path);
        InputStream inputStream = FileAndFolderUtil.parseOut(b1);
        ServletOutputStream outputStream = response.getOutputStream();
        int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
        inputStream.close();
        outputStream.close();
        // 删除附件
        FileAndFolderUtil.delete(newFilePath);
        FileAndFolderUtil.delete(path);
    }

    /**
     * 完成复核：中间检测项 及 最终复核
     *  list
     *  checkReview
     */
    @RequestMapping(value = "/finishCheckItemReview")
    public void finishCheckItemReview(@RequestBody ExcelInsertVo excelInsertVo){
       // 审核人id。
        Long  userId = null;
        // 判断复核数据类型。
    }



}
