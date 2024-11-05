package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.mapper.TestTechnicistDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.PDFHelper3;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
import java.util.*;
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
    @Autowired
    private PageOfficeCopyService pageOfficeCopyService;
    @Autowired
    private TestProductItemDao testProductItemDao;
    Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private ReportService reportService;
    @Autowired
    private TestTechnicistDao testTechnicistDao;
    @Autowired
    private SysUserService userService;
    @Resource
    private TestTaskPoolService testTaskPoolService;
    /**
     * 受控文件信息
     */
    @Resource
    private TestControlledDocumentsService testControlledDocumentsService;

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
            // 获取授权签字人的用户列表
            String str = taskService.verifyUserInformation(strLong);
            if(StringUtils.isNotEmpty(str)){
                return ResultUtil.error(201, str);
            }
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
            // 获取授权签字人的用户列表
            String str = taskService.verifyUserInformation(strLong);
            if(StringUtils.isNotEmpty(str)){
                return ResultUtil.error(201, str);
            }
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
     * 任务单领取及修改时 返回 团队姓名
     * 通过委托单id获取任务单创建时间 比较数据 是否需要进行过滤
     *
     * @return
     */
    @RequestMapping("getEntrustTeamUserName")
    public Result getEntrustTeamUserName(Long entrustId) {
        if (ShiroUtils.getUserInfo() != null) {

            // 比较任务单创建时间：区分团队信息是否拆分
            Result taskVerificationInformation = taskService.compareTaskListCreationInformation(entrustId, null);

            if (taskVerificationInformation.getData() == null) {
                // 任务单不存在
                return taskService.getTeamMemberInformation();
            } else {
                // 提示信息
                String promptMessage = (String) taskVerificationInformation.getData();
                if (promptMessage.equals("newTask")) {
                    return taskService.getTeamMemberInformation();
                }
            }
            TeamVo returnList = new TeamVo();
            List<LabelValueVo> teamVos0 = taskMapper.getAllTeamUser();
            // 报告制作人、辅助人员、实习生，见习生。不考虑团队。
            List<LabelValueVo> reviewVo = taskMapper.getAllTeamNAMEUser();
            returnList.setTeamVo(teamVos0);
            returnList.setReviewVo(reviewVo);
            return ResultUtil.success(returnList);
        }
        return ResultUtil.error(502, "token过期！");
    }

    /**
     * 任务大厅-返回团队成员信息
     *
     * @return
     */
    @RequestMapping("getTeamMemberInformation")
    public Result getTeamMemberInformation() {

        return taskService.getTeamMemberInformation();
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
     * 查询检测任务列表
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
                //根据用户id查询
                Boolean flag = userService.checkSysAndAdmRole(userInfo.getUserId());
                if (flag) {
                    deptIds = null;
                } else {
                    return ResultUtil.error("账号使用人未配置科室人员");
                }
            }
            if (deptIds.length >= 1) {
                paramVo.setReviewer(userInfo.getUserId().toString());
                paramVo.setInspector(userInfo.getUserId().toString());
                //记录人
                paramVo.setRecorder(userInfo.getUserId().toString());
                // 签发人
                paramVo.setReceiver(userInfo.getUserId().toString());
                //报告制作人
                paramVo.setReportProducer(userInfo.getName());
                //领样人
                paramVo.setSampler(userInfo.getName());
                // 见习生：实习的新手
                paramVo.setProbationer(userInfo.getName());
                // 实习生
                paramVo.setInterns(userInfo.getName());
                // 辅助人员
                paramVo.setAuxiliaryPersonnel(userInfo.getName());
            }
            return ResultUtil.success("查询任务列表成功！", taskService.getTaskListShow(paramVo, deptIds));
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
                //根据用户id查询
                Boolean flag = userService.checkSysAndAdmRole(userInfo.getUserId());
                if(flag){
                    deptIds = null;
                }else {
                    return ResultUtil.error("账号使用人未配置科室人员");
                }
            }
            paramVo.setReviewer(userInfo.getUserId().toString());
            paramVo.setInspector(userInfo.getUserId().toString());
            //记录人
            paramVo.setRecorder(userInfo.getUserId().toString());
            //报告制作人
            paramVo.setReportProducer(userInfo.getName());
            //领样人
            paramVo.setSampler(userInfo.getName());
            // 见习生：实习的新手
            paramVo.setProbationer(userInfo.getName());
            // 实习生
            paramVo.setInterns(userInfo.getName());
            // 辅助人员
            paramVo.setAuxiliaryPersonnel(userInfo.getName());
            // 签发人
            paramVo.setReceiver(userInfo.getUserId().toString());
            return ResultUtil.success("查询任务列表成功！", taskService.getTaskListTwoShow(paramVo, deptIds));
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

    /**
     * ToDO:11月23日 下载docx 任务单信息 数组taskIds下载
     *
     * @param taskIds
     */
    @RequestMapping("downloadEntrust")
    public void downloadEntrust(Long[] taskIds)  {
        String fileName = "";
        // 3月13日前
        String str1 = "taskOrder11.docx";
        // 2023年3月13 零点后
        String str2 = "taskOrder20.docx";
        // 2023年07月01 使用
        String str3 = "taskOrder30升级版3.docx";
        for (int i = 0; i < taskIds.length; i++) {
            Long taskId = taskIds[i];
            // 获取任务单下单时间 进行比较
            TaskTestEntity taskDetails = taskMapper.getTaskOrderTime(taskId);
            Boolean status = false;
            if (taskDetails != null && taskDetails.getOrderTime() != null) {
                Date date = null;
                Date date2 = null;
                //实现将字符串转成⽇期类型
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    date = dateFormat.parse("2023-03-12 23:59:59");
                    date2 = dateFormat.parse("2023-07-31 23:59:59");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // 截止至 2023-03-12 23:59:59 后 任务单附件为  String str2 = "taskOrder20.docx"
                if ((date.getTime() < taskDetails.getOrderTime().getTime()) && (date2.getTime() > taskDetails.getOrderTime().getTime())) {
                    fileName = str2;
                } else if (date2.getTime() < taskDetails.getOrderTime().getTime()) {
                    fileName = str3;
                    status = true;
                } else {
                    fileName = str1;
                }
            }
            try {
                MinioClient client = MinIoUtil.minioClient;
                InputStream object = client.getObject(BucketsConst.buckets_task_template, fileName);
                TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfoTwo(taskId, null);
                XWPFDocument document = null;
                if (status == true) {
                    document = taskService.downloadEntrustNew(taskDetailInfo, object);
                } else {
                    document = taskService.downloadEntrust(taskDetailInfo, object, false);
                }
                String file = taskDetailInfo.getTaskCode() + ".docx";
                file = URLEncoder.encode(file, "UTF-8");
                File fileOut2 = new File("D:\\AAno\\" + file);
                OutputStream fileOut = new FileOutputStream(fileOut2.getPath());
                // 将源文件数组中的当前文件读入 FileInputStream 流中
                document.write(fileOut);
                fileOut.close();
            } catch (Exception ex) {
                log.info("导出失败：", ex.getMessage());
            }
        }
    }


//    /**downloadOriginalRecord
//     * 下载任务通知单——二次开发 丁 线上使用中
//     * 变更需求 world 转 pdf。
//     *
//     * @param taskId
//     * @param response
//     */
//    @GetMapping("downloadEntrust_two")
//    public void downloadEntrust_two(Long taskId, HttpServletResponse response) {
//        String fileName = "";
//        // 3月13日前
//        String str1 = "taskOrder11.docx";
//        // 2023年3月13 零点后
//        String str2 = "taskOrder20.docx";
//        // 2023年07月01 使用
//        String str3 = "taskOrder30升级版3.docx";
//        // 获取任务单下单时间 进行比较
//        TaskTestEntity taskDetails = taskMapper.getTaskOrderTime(taskId);
//        Boolean status = false;
//        if(taskDetails!=null && taskDetails.getOrderTime()!=null){
//            Date date = null;
//            Date date2 = null;
//            //实现将字符串转成⽇期类型
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            try {
//                date = dateFormat.parse("2023-03-12 23:59:59");
//                date2 = dateFormat.parse("2023-07-31 23:59:59");
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            // 截止至 2023-03-12 23:59:59 后 任务单附件为  String str2 = "taskOrder20.docx"
//            if((date.getTime() < taskDetails.getOrderTime().getTime()) && (date2.getTime() > taskDetails.getOrderTime().getTime())){
//                fileName = str2;
//            }else if(date2.getTime() < taskDetails.getOrderTime().getTime()){
//                fileName = str3;
//                status = true;
//            }
//             else {
//                 fileName = str1;
//             }
//        }
//        String url = "";
//        String downloadFileName = "任务单编号";
//        try {
//            MinioClient client = MinIoUtil.minioClient;
//            InputStream object = client.getObject(BucketsConst.buckets_task_template, fileName);
//            TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfoTwo(taskId, null);
//            XWPFDocument doc = null;
//            if(status == true){
//               doc = taskService.downloadEntrustNew(taskDetailInfo, object);
//            }else {
//                doc = taskService.downloadEntrust(taskDetailInfo, object,false);
//            }
//            //相应pdf
//            ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
//            InputStream inputStream = FileAndFolderUtil.parseOut(b1);
//            //TODO 设置签名信息
//            /** 设置文件下载名 （任务单号+检测项名）**/
//            /** 不同文件的MimeType参考后续链接 **/
//            String file = taskDetailInfo.getTaskCode()+".pdf";
//            file = URLEncoder.encode(file, "UTF-8");
//            response.setHeader("Content-Disposition", "inline;fileName=" + file + ";fileName*=UTF-8''" + file);
//            ServletOutputStream outputStream = response.getOutputStream();
//            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
//            inputStream.close();
//            outputStream.close();
////            url = MinIoUtil.upload("task-download", taskId + ".pdf", inputStream, "application/octet-stream");
//        } catch (Exception ex) {
//            log.info("导出失败：{}", ex);
//        }
//    }

    /**
     * downloadOriginalRecord
     * 下载任务通知单——二次开发 丁 线上使用中
     * 变更需求 world 转 pdf。
     *
     * @param taskId
     * @param response
     */
    @GetMapping("downloadEntrust_two")
    public void downloadEntrust_two(Long taskId, HttpServletResponse response) {

        String fileName = "";
        Boolean status = false;
        String bucketName = "";
        // 任务单 - 调用受控文件信息工具类 返回map信息
        try {
            HashMap<String, String> map = testControlledDocumentsService.returnBucketInformation(taskId, 58);
            if (CollectionUtil.isNotEmpty(map.keySet())) {
                // 桶名
                bucketName = map.get("bucketName");
                // 文件名
                fileName = map.get("content");
                // 状态信息 == 0 是最新的
                if (map.get("status").equals("0")) {
                    status = true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(bucketName, fileName);
            TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfoTwo(taskId, null);
            XWPFDocument doc = null;
            if (status == true) {
                doc = taskService.downloadEntrustNew(taskDetailInfo, object);
            } else {
                doc = taskService.downloadEntrust(taskDetailInfo, object, false);
            }
            //相应pdf
            ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
            InputStream inputStream = FileAndFolderUtil.parseOut(b1);
            //TODO 设置签名信息
            /** 设置文件下载名 （任务单号+检测项名）**/
            /** 不同文件的MimeType参考后续链接 **/
            String file = taskDetailInfo.getTaskCode() + ".pdf";
            file = URLEncoder.encode(file, "UTF-8");
            response.setHeader("Content-Disposition", "inline;fileName=" + file + ";fileName*=UTF-8''" + file);
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
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
            // 通过任务单id 查询 报告节点
            if (taskService.getVerifyReportState(taskId)) {
                return ResultUtil.error("操作失败，报告已经签发完成");
            }
            List<Integer> items = new ArrayList<>();
            items.add(itemId);
            Integer type = null;
            if (state == 1) {
                // 撤回操作 只有检测人才能操作
                type = 0;
            } else {
                // 复核人操作
                type = 2;
            }
            Result msg = testTaskPoolService.testDetectionTasks(taskId, items, 0);
            if (msg.getCode() == null) {
                return msg;
            }
            return ResultUtil.success(taskService.passorno(itemId, state, opinion, userInfo.getUserId()));
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
        Long userId = ShiroUtils.getUserInfo().getUserId();
        // 获取授权签字人的用户列表
        String str = taskService.verifyUserInformation(userId);
        if(StringUtils.isNotEmpty(str)){
            return ResultUtil.error(201, str);
        }
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
                Base64.Encoder encoder = Base64.getEncoder();
                String encode = encoder.encodeToString(data);
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
        if (taskStatsVo.getIntegers().length == 0 || taskStatsVo.getIntegers() == null) {
            log.info("批量下载原始记录 integers = " + taskStatsVo.getIntegers().toString());
        }
        // 通过检测项id 获取对应任务单附件有 则下载就行 da_task_record 读取 URL
        //  da_task_record 读取 URL 附件
        List<String> urls = taskMapper.getTaskRecordUrl(taskStatsVo.getIntegers(), null);
        if (CollectionUtil.isNotEmpty(urls)) {
            // 下载附件即可
            taskService.packagingUrlSWorkbookXls(urls, response);
        }
        // 效验 检测项url信息模板
        // 通过检测项id 获取 相应的 id关联信息。
        List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(taskStatsVo.getIntegers());
        // 判断 压缩数据=null 返回 null
        if (!CollectionUtils.isEmpty(dataEntitys) && CollectionUtil.isEmpty(urls)) {
            if (!CollectionUtils.isEmpty(dataEntitys)) {
                // 处理条数 == 1 不需要zip打包
                if (dataEntitys.size() == 1) {
                    response.reset();
                    response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                    response.setContentType("application/vnd.ms-excel");
                    response.setCharacterEncoding("UTF-8");
                    String[] split = dataEntitys.get(0).getFileUrl().split("/");
                    String[] split1 = split[4].split("\\?");
                    // 附件后缀名
                    String suffixName = "." + split1[0].split("\\.")[1];
                    if (suffixName.equals(".xls")) {
                        response.setContentType("application/vnd.ms-excel");
                        response.setCharacterEncoding("UTF-8");
                        response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode("原始记录" + dataEntitys.get(0).getCheckItemName() + suffixName, "UTF-8"));
                    } else {
                        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        response.setCharacterEncoding("UTF-8");
                        response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode("原始记录" + dataEntitys.get(0).getCheckItemName() + suffixName, "UTF-8"));
                    }
                    taskService.packagingWorkbookXls(dataEntitys, response);
                } else {
                    response.reset();
                    response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                    response.setContentType("application/zip");
                    response.setCharacterEncoding("UTF-8");
                    response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode("原始记录.zip", "UTF-8"));
                    ZipOutputStream zipOutputStream = taskService.packagingWorkbookZip(dataEntitys, response, null);
                    zipOutputStream.flush();
                }
            }
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
        Long[] longids = new Long[1];
        longids[0] = itemId.longValue();
        // 处理检测项数据。
        List<ExcelInsertVo> excelInsertlist = testProductItemDao.selectCheckList(longids);
        if (CollectionUtil.isNotEmpty(excelInsertlist)) {
            // 进行获取数据 进行查看 复核通过&&有印章 则展示数据
            ExcelInsertVo data = excelInsertlist.get(0);
            // 进行pdf 预览
            if (StringUtils.isNotEmpty(data.getContractId())) {
                byte[] bytes = reportService.downloadQysFile(null, Long.valueOf(data.getContractId()), data.getCheckItemName(), data.getCheckItemCode());
                response.reset();
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setContentType("application/zip");
                response.setCharacterEncoding("UTF-8");
                String fileName = data.getCheckItemCode() + "（" + data.getCheckItemName() + "）.zip";
                try {
                    response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
                    OutputStream outputStream = response.getOutputStream();
                    outputStream.write(bytes);
                    outputStream.close();
                    outputStream.close();
                } catch (Exception e) {
                    logger.error("下载契约锁报告文档失败:{}", e);
                }
            } else {

                // 通过检测项id 获取对应任务单附件有 则下载就行 da_task_record 读取 URL
                //  da_task_record 读取 URL 附件
                List<String> urls = taskMapper.getTaskRecordUrl(ids, null);
                if (CollectionUtil.isNotEmpty(urls)) {
                    // 下载附件即可
                    taskService.packagingUrlSWorkbookXls(urls, response);
                }

                // 下载原始记录模板
                // 效验 检测项url信息模板
                // 通过检测项id 获取 相应的 id关联信息。
                List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(ids);
                // 判断 压缩数据=null 返回 null
                if (!CollectionUtils.isEmpty(dataEntitys) && CollectionUtil.isEmpty(urls)) {
                    // 处理条数 == 1 不需要zip打包
                    if (dataEntitys.size() == 1) {
                        response.reset();
                        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

                        String[] split = dataEntitys.get(0).getFileUrl().split("/");
                        String[] split1 = split[4].split("\\?");
                        // 附件后缀名
                        String suffixName = "." + split1[0].split("\\.")[1];
                        if (suffixName.equals(".xls")) {
                            response.setContentType("application/vnd.ms-excel");
                            response.setCharacterEncoding("UTF-8");
                            response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode("原始记录" + dataEntitys.get(0).getCheckItemName() + suffixName, "UTF-8"));
                        } else {
                            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                            response.setCharacterEncoding("UTF-8");
                            response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode("原始记录" + dataEntitys.get(0).getCheckItemName() + suffixName, "UTF-8"));
                        }
                        taskService.packagingWorkbookXls(dataEntitys, response);
                    } else {
                        response.reset();
                        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
                        response.setContentType("application/zip");
                        response.setCharacterEncoding("UTF-8");
                        response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode("原始记录.zip", "UTF-8"));
                        ZipOutputStream zipOutputStream = taskService.packagingWorkbookZip(dataEntitys, response, taskId);
                        zipOutputStream.flush();
                    }
                }
            }
        }
    }

    /**
     * 复核：中间及最终检测项(返回pdf)
     *  list
     *  checkReview
     */
    @RequestMapping(value = "/checkItemReview")
    public void previewDownLoad(String list , HttpServletResponse response) throws Exception {
        String newFilePath = qiYueSuoEntity.getAutographPath() + GenID.getID() + ".xlsx";
        String path = qiYueSuoEntity.getAutographPath() + GenID.getID() + ".pdf";
        ExcelInsertVo excelInsertVo = new ExcelInsertVo();
        String[] items = list.split(",");
        Integer[] ids = new Integer[items.length];
        for (int j = 0; j < items.length; j++) {
            ids[j] = Integer.parseInt(items[j]);
        }
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
        if (!CollectionUtils.isEmpty(sheetItems)) {
            List<Integer> idList = new ArrayList<>();
            for (int i = 0; i < sheetItems.size(); i++) {
                ExcelInsertVo excelInsertVo1 = sheetItems.get(i);
                if (StringUtils.isNotEmpty(excelInsertVo1.getTestSetUrl()) && StringUtils.isNotEmpty(excelInsertVo1.getRecordSetUrl())) {
                    idList.add(excelInsertVo1.getItemId());
                }
            }
            excelInsertVo.setList(idList);
            // 判断检测数据不为空
            if (CollectionUtil.isNotEmpty(excelInsertVo.getList())) {
                // excel 转 pdf
                XSSFWorkbook wb = taskService.getOriginalRecordAttachment(excelInsertVo);
                FileOutputStream out = new FileOutputStream(newFilePath);
                wb.write(out);
                out.flush();//刷新
                InputStream out000 = new FileInputStream(newFilePath);
                //相应pdf
                ByteArrayOutputStream b1 = PDFHelper3.excel2pdf(out000, path);
                InputStream inputStream = FileAndFolderUtil.parseOut(b1);
                ServletOutputStream outputStream = response.getOutputStream();
                int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
                inputStream.close();
                outputStream.close();
                out000.close();
                b1.close();
                out.close();//关闭
                // 删除附件
                FileAndFolderUtil.delete(newFilePath);
                FileAndFolderUtil.delete(path);
            }
        } else {
            // 检测项无Sheet页

        }
    }

    /**
     * 完成复核：中间检测项 及 最终复核
     *  list
     *  checkReview
     */
    @RequestMapping(value = "/finishCheckItemReview")
    public Result finishCheckItemReview(@RequestBody ExcelInsertVo excelInsertVo) throws Exception {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 通过检测项主键 获取test_task Id
        Long taskId = taskMapper.getReturnTaskId(excelInsertVo.getList().get(0));
        // 委托单144 则不能执行任务单
        if (taskService.judgeTaskStatus(taskId)) {
            return ResultUtil.error(678, "操作失败！任务单已废弃！！！");
        }
        if (testDetectionService.reviewTheLogin(userInfo.getUserId(), taskId) == false) {
            return ResultUtil.error("登录人没有被派发复核资格");
        }
        List<Integer> items = new ArrayList<>();
        for (Integer itemd : excelInsertVo.getList()) {
            items.add(itemd);
        }
        Result msg = testTaskPoolService.testDetectionTasks(taskId, items, 2);
        if (msg.getCode() == null) {
            return msg;
        }
        // TODO: 2023年10月17日：发起复核前 判断任务单是否实验完成。效验数据为最终复核通过的
//        if(!taskService.judgeTaskEndTest(taskId,excelInsertVo)){
//            return ResultUtil.error(678, "操作失败！任务单未完成试验");
//        }
        // 判断复核数据类型。
        pageOfficeCopyService.finishCheckItemReview(excelInsertVo, userInfo.getUserId(), taskId);
        return ResultUtil.success(pageOfficeCopyService.CompleteTheReview(excelInsertVo));
    }

    /**
     * 根据报告单主键 进行对应的检测项预览
     * list
     * checkReview
     */
    @RequestMapping(value = "/reportCheckItemReview")
    public void reportCheckItemReview(String reportId, HttpServletResponse response) throws Exception {
        // 根据报告主键 获取所属检测项主键列表
        List<Integer> itemIds = testProductItemDao.selectGROUPBYItemId(Long.parseLong(reportId));
        if (CollectionUtil.isNotEmpty(itemIds)) {
            String newFilePath = qiYueSuoEntity.getAutographPath() + GenID.getID() + ".xlsx";
            String path = qiYueSuoEntity.getAutographPath() + GenID.getID() + ".pdf";
            ExcelInsertVo excelInsertVo = new ExcelInsertVo();
            Integer[] ids = new Integer[itemIds.size()];
            for (int j = 0; j < itemIds.size(); j++) {
                ids[j] = itemIds.get(j);
            }
            // 查询检测项对应的 sheet下标
            List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
            if (!CollectionUtils.isEmpty(sheetItems)) {
                List<Integer> idList = new ArrayList<>();
                for (int i = 0; i < sheetItems.size(); i++) {
                    ExcelInsertVo excelInsertVo1 = sheetItems.get(i);
                    if (StringUtils.isNotEmpty(excelInsertVo1.getTestSetUrl()) && StringUtils.isNotEmpty(excelInsertVo1.getRecordSetUrl())) {
                        idList.add(excelInsertVo1.getItemId());
                    }
                }
                excelInsertVo.setList(idList);
                // 判断检测数据不为空
                if (CollectionUtil.isNotEmpty(excelInsertVo.getList())) {
                    // excel 转 pdf
                    XSSFWorkbook wb = taskService.getOriginalRecordAttachment(excelInsertVo);
                    FileOutputStream out = new FileOutputStream(newFilePath);
                    wb.write(out);
                    out.flush();//刷新
                    InputStream out000 = new FileInputStream(newFilePath);
                    //相应pdf
                    ByteArrayOutputStream b1 = PDFHelper3.excel2pdf(out000, path);
                    InputStream inputStream = FileAndFolderUtil.parseOut(b1);
                    ServletOutputStream outputStream = response.getOutputStream();
                    int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
                    inputStream.close();
                    outputStream.close();
                    out000.close();
                    b1.close();
                    out.close();//关闭
                    // 删除附件
                    FileAndFolderUtil.delete(newFilePath);
                    FileAndFolderUtil.delete(path);
                }
            } else {
                // 检测项无Sheet页

            }
        }
//        // 检测项无Sheet页
//        new ModelAndView("error");
    }

    /**
     *任务大厅列表
     * @param bean
     * @return
     */
    @PostMapping("taskHall")
    public Result taskHall(@RequestBody ReqTaskPool bean){
        bean.setUserId(ShiroUtils.getUserInfo().getUserId());
        PageHelper.clearPage();
        Integer teamId = testTechnicistDao.getSealer(bean.getUserId());
        if (teamId != null){
            //Integer id = testTechnicistDao.getPidById(teamId);
            bean.setTeamId(teamId);
            PageInfo<TestTaskPool> pageInfo = taskService.taskHall(bean);
            return ResultUtil.success(pageInfo);
        }else {
            //根据用户id查询
            Boolean flag = userService.checkSysAndAdmRole(bean.getUserId());
            if (flag){
                PageInfo<TestTaskPool> pageInfo = taskService.taskHall(bean);
                return ResultUtil.success(pageInfo);
            }else {
                return ResultUtil.success(new PageInfo<TestTaskPool>());
            }
        }
    }

    /**
     * 我的任务列表
     *
     * @param bean
     * @return
     */
    @PostMapping("myTaskList")
    public Result myTaskList(@RequestBody ReqTaskPool bean) {
        bean.setUserId(ShiroUtils.getUserInfo().getUserId());
        PageInfo<TestTaskPool> pageInfo = taskService.myTaskList(bean);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 返回 辅助人员 用户信息
     *
     * @return
     */
    @RequestMapping("getAuxiliaryPersonnelInformation")
    public Result getAuxiliaryPersonnelInformation() {
        if (ShiroUtils.getUserInfo() != null) {
            return taskService.getAuxiliaryPersonnelInformation();
        }
        return ResultUtil.error(502, "token过期！");
    }

    @RequestMapping("plugOperation")
    public Result getplugOperation(String olCode, String taskCode) {

        return taskService.getplugOperation(olCode, taskCode);
    }
}
