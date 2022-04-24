package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        if (ShiroUtils.getUserInfo() != null) {
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
        }
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
        if (taskTestEntity.getInspector() == null || taskTestEntity.getRecorder() == null || taskTestEntity.getReviewer() == null || taskTestEntity.getReportProducer() == null) {
            return ResultUtil.error(201, "缺少必填参数！");
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
        if (paramVo == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success("查询任务列表成功！", taskService.getTaskList(paramVo));
        }
    }

    /**
     * 查询任务列表二次
     * 根据科室进行展示数据
     *
     * @param paramVo
     * @return
     */
    @PostMapping(value = "/getTaskList_two")
    public Result getTaskInfo_two(@RequestBody TaskListParamVo paramVo) {

        if (paramVo == null || paramVo.getState() == null || paramVo.getPageNum() == null || paramVo.getPageSize() == null) {
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
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            String fileName2 = URLEncoder.encode(split1[0], "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName2);
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载任务通知单 废弃 页面已经不使用
     *
     * @param taskId
     * @param response
     */
    @RequestMapping("downloadEntrust")
    public void downloadEntrust(Long taskId, HttpServletResponse response) {
        String fileName = "taskOrder1.docx";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_task_template, fileName);
            TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfo(taskId);
            XWPFDocument document = taskService.downloadEntrust(taskDetailInfo, object);
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            document.close();
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
        }
    }

    /**
     * 下载任务通知单——二次开发 丁 线上使用中
     *
     * @param taskId
     * @param response
     */
    @GetMapping("downloadEntrust_two")
    public void downloadEntrust_two(Long taskId, HttpServletResponse response) {
        String fileName = "taskOrder1.docx";
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(BucketsConst.buckets_task_template, fileName);
            TaskDetailInfoVo taskDetailInfo = taskService.getTaskDetailInfoTwo(taskId, null);
            XWPFDocument document = taskService.downloadEntrust(taskDetailInfo, object);
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            document.close();
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
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
    @GetMapping("/upload_original_records_in_batches")
    @ResponseBody
    public Result uploadingBatch(@RequestParam List<Integer> ids, MultipartFile file) {
        if (ids == null && ids.size()==0) {
            return ResultUtil.error("缺少必填参数！");
        }
        if (file == null) {
            return ResultUtil.error("样品file文件为空！");
        }
        if(taskService.effectDataSet((List<Integer>)ids)==false){
            return ResultUtil.error("文件已存在");
        }
        if (taskService.uploadingBatch((List<Integer>) ids, file) == true) {
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
     * 删除附件
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
        if (vo.getTaskId() == null || vo.getInspector() == null || vo.getRecorder() == null || vo.getReviewer() == null || vo.getReportProducer() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            int i = taskService.updatePersonInfo(vo);
            if (i > 0) {
                return ResultUtil.success("修改人员信息成功！", i);
            }
            return ResultUtil.error("修改人员信息失败！");
        }
    }

}
