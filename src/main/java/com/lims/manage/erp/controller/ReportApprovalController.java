package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.ReportApprovalService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestCheckItemsTaskRelService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.ReportApprovalVo;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:39
 * 报告审批
 */
@Slf4j
@RestController
@RequestMapping("/report_approval/")
public class ReportApprovalController {

    @Autowired
    ReportApprovalService reportApprovalService;
    @Autowired
    ReportApprovalMapper reportApprovalMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private EntrustService entrustService;
    @Autowired
    private TestCheckItemsTaskRelService testCheckItemsTaskRelService;
    @Autowired
    private TaskMapper taskMapper;

    /**
     * 报告审批列表
     *
     * @param search
     * @param state
     * @return
     */
//    @GetMapping("/applyfor")
//    public Result applyfor(String search, Integer state) {
//        List<ReportApprovalVo> list = reportApprovalService.getApplyforList(search, state);
//        if (!list.isEmpty()) {
//            return ResultUtil.success(list);
//        }
//        return ResultUtil.success(list);
//    }

    /**
     * 报告审批列表
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/applyfor")
    public Result applyfor(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus) {
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageInfo pageInfo = reportApprovalService.getApplyforList(search, pageNum,pageSize,reportTypeStatus);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 审批抢单
     *
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("applyfor_monad")
    public Result applyfor_monad(@RequestBody ReportApprovalVo reportApprovalVo1) {

        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        Long id = reportApprovalVo1.getId();
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        //2、 查询任务单号 是否被抢。
        ReportApprovalVo reportApprovalVo = reportApprovalMapper.getReportApprovalDetail(id);
        if (reportApprovalVo == null) {
            return ResultUtil.error(678, "此任务单号不存在");
        }
        if (reportApprovalVo.getVerifyer() != null) {
            return ResultUtil.error(678, "此任务单号已经被抢");
        }
        // 效验报告单状态
        if (reportApprovalVo.getState() != 1) {
            return ResultUtil.error(678, "此任务单号状态不对");
        }
        //3、进行抢单
        reportApprovalVo.setVerifyer(name);
        reportApprovalVo.setId(id);
        reportApprovalVo.setState(3);
        Boolean flag = reportApprovalService.applyfor_monad(reportApprovalVo);
        if (flag) {
            return ResultUtil.success("抢单成功");
        }
        return ResultUtil.error(678, "抢单失败");
    }


    /**
     * 审批数据——废弃
     *
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("approval_data")
    public Result approval_data(@RequestBody ReportApprovalVo reportApprovalVo1) {
        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        if (reportApprovalVo1.getState() == null) {
            return ResultUtil.error(678, "审批信息不能为空");
        }
        if (reportApprovalVo1.getState() != 1 && reportApprovalVo1.getState() != 0) {
            return ResultUtil.error(678, "审批信息有误");
        }
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        //2、 查询任务单号 是否被抢。
        ReportApprovalVo reportApprovalVo = reportApprovalMapper.getReportApprovalDetail(reportApprovalVo1.getId());
        if (reportApprovalVo == null) {
            return ResultUtil.error(678, "此任务单号不存在");
        }
        if (reportApprovalVo.getVerifyer() == null) {
            return ResultUtil.error(678, "请先抢单");
        }
        if (!reportApprovalVo.getVerifyer().equals(name)) {
            return ResultUtil.error(678, "审批失败，审批人与抢单人不一致");
        }
        // 效验报告单状态
        if (reportApprovalVo.getState() != 3) {
            return ResultUtil.error(678, "此任务单号状态不对");
        }
        Boolean flag = reportApprovalService.approval_data(reportApprovalVo1);
        if (flag) {
            return ResultUtil.success("审批成功");
        }
        return ResultUtil.error(678, "审批失败");
    }

    /**
     * 审批数据——二次开发
     *
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("approval_data_two")
    public Result approval_data_Two(@RequestBody ReportApprovalVo reportApprovalVo1) {
        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        if (reportApprovalVo1.getState() == null) {
            return ResultUtil.error(678, "审批信息不能为空");
        }
        if (reportApprovalVo1.getState() != 1 && reportApprovalVo1.getState() != 0) {
            return ResultUtil.error(678, "审批信息有误");
        }
        //1、 获取审批人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        // 通过报告id 和 登录人id和姓名 比对
        if(!reportApprovalService.efficacyApprovalData(reportApprovalVo1.getId(),userInfo.getUserId(),name,1)){
            return ResultUtil.error(678, "审批失败！当前登录人不是指定人");
        }
        // 审核人姓名保存
        reportApprovalVo1.setVerifyer(name);
        Boolean flag = reportApprovalService.approval_data_two(reportApprovalVo1);
        if (flag) {
            return ResultUtil.success("成功");
        }
        return ResultUtil.error(678, "审批失败");
    }

    /**
     * 报告审批历史查询列表
     *
     * @param search
     * @return
     */
//    @GetMapping("/applyfor_history")
//    public Result applyfor_history(String search) {
//
//        List<ReportApprovalVo> list = reportApprovalService.applyfor_history(search);
//        if (!list.isEmpty()) {
//            return ResultUtil.success(list);
//        }
//        return ResultUtil.success(list);
//    }

    /**
     * 报告审批历史查询列表
     */
    @GetMapping("/applyfor_history")
    public Result applyfor_history(String search,Integer pageNum,Integer pageSize,Integer reportTypeStatus) {
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageInfo pageInfo = reportApprovalService.applyfor_history(search, pageNum,pageSize,reportTypeStatus);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 根据报告id 查询历史审批详情
     *
     * @param id
     * @return
     */
    @GetMapping("/applyfor_details_history")
    public Result applyfor_details_history(Long id) {
        if (id == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        return ResultUtil.success("查询任务详情成功！", reportApprovalService.getDetails(id));
    }


    /**
     * 根据报告id 查询详情
     *
     * @param id
     * @return
     */
    @GetMapping("/applyfor_details")
    public Result applyfor_details(Long id) {
        if (id == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        return ResultUtil.success("查询任务详情成功！", reportApprovalService.getDetails(id));
    }

    /**
     * 线上审批详情
     * @param reportId
     * @return
     */
    @GetMapping("/onlineReport")
    public void onlineApprove(Long reportId, HttpServletResponse response) {
        if (reportId == null) {
            log.info("导出失败：", "报告主键不能为空");
        }
//        String url = "http://121.89.242.0:9000/report-download/JC7-2023-YC-0471.pdf";
        String url = reportApprovalService.getReportUrl(reportId);
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        String[] split = url.split("/");
        String fileName = split[split.length-1];
        String bucket = split[split.length-2];
        try {
            InputStream inputStream = MinIoUtil.getFileStream(bucket, fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);// copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            log.info("导出失败：", e.getMessage());
        }
    }

    /**
     * 审批保存
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("approvalSave")
    public Result approvalSave(@RequestBody ReportApprovalVo reportApprovalVo1) {
        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数！");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "任务单主键不能为空！");
        }
        if (reportApprovalVo1.getStandardConclusion() == null || reportApprovalVo1.getReportRange() == null) {
            return ResultUtil.error(678, "审批信息不能为空！");
        }
        if(reportApprovalVo1.getStandardConclusion().equals(1) || reportApprovalVo1.getReportRange().equals(1)){
            if(reportApprovalVo1.getReason() == null || "".equals(reportApprovalVo1.getReason())){
                return ResultUtil.error(678, "驳回原因不能为空！");
            }else{
                reportApprovalVo1.setState(1);//驳回
            }
        }else{
            reportApprovalVo1.setState(0);//通过
        }

        //1、 获取审批人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        // 通过报告id 和 登录人id和姓名 比对
        if(!reportApprovalService.efficacyApprovalData(reportApprovalVo1.getId(),userInfo.getUserId(),name,1)){
            return ResultUtil.error(678, "审批失败！当前登录人不是指定人");
        }
        // 审核人姓名保存
        reportApprovalVo1.setVerifyer(name);
        Boolean flag = reportApprovalService.approval_data_two(reportApprovalVo1);
        if (flag) {
            return ResultUtil.success("成功");
        }
        return ResultUtil.error(678, "审批失败");
    }

    /**
     * 报告签发列表
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/verify_list")
    public Result verify_list(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus) {
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageInfo pageInfo = reportApprovalService.getVerify_list(search, pageNum, pageSize,reportTypeStatus);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 签发抢单 废弃
     *
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("verify_monad")
    public Result verify_monad(@RequestBody ReportApprovalVo reportApprovalVo1) {

        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        Long id = reportApprovalVo1.getId();
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        //2、 查询任务单号 是否被抢。
        ReportApprovalVo reportApprovalVo = reportApprovalMapper.getReportApprovalDetail(id);
        if (reportApprovalVo == null) {
            return ResultUtil.error(678, "此任务单号不存在");
        }
        if (reportApprovalVo.getIssuer() != null) {
            return ResultUtil.error(678, "此任务单号已经被抢");
        }
        // 效验报告单状态
        if (reportApprovalVo.getState() != 4) {
            return ResultUtil.error(678, "此任务单号状态不对");
        }
        //3、签发人进行抢单
        reportApprovalVo.setIssuer(name);
        reportApprovalVo.setId(id);
        reportApprovalVo.setState(5);
        Boolean flag = reportApprovalService.verify_monad(reportApprovalVo);
        if (flag) {
            return ResultUtil.success("抢单成功");
        }
        return ResultUtil.error(678, "抢单失败");
    }

    /**
     * 签发报告
     *
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("verify_data")
    public Result verify_data(@RequestBody ReportApprovalVo reportApprovalVo1) {
        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        if (reportApprovalVo1.getState() == null) {
            return ResultUtil.error(678, "审批信息不能为空");
        }
        if (reportApprovalVo1.getState() != 1 && reportApprovalVo1.getState() != 0) {
            return ResultUtil.error(678, "审批信息有误");
        }
        if (reportApprovalVo1.getState() == 0) {
            if (reportApprovalVo1.getSealTypeArray() == null || reportApprovalVo1.getSealTypeArray().length<=0) {
                return ResultUtil.error(678, "印章不能为空");
            }
        }
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        //2、 查询任务单号 是否被抢。
        ReportApprovalVo reportApprovalVo = reportApprovalMapper.getReportApprovalDetail(reportApprovalVo1.getId());
        if (reportApprovalVo == null) {
            return ResultUtil.error(678, "此任务单号不存在");
        }
        if (reportApprovalVo.getIssuer() == null) {
            return ResultUtil.error(678, "请先抢单");
        }
        if (!reportApprovalVo.getIssuer().equals(name)) {
            return ResultUtil.error(678, "签发失败，审批人与抢单人不一致");
        }
        reportApprovalVo1.setIssuer(name);
        // 签发报告单状态 应该是已经抢单 state =5
        if (reportApprovalVo.getState() != 5) {
            return ResultUtil.error(678, "此任务单号状态不对");
        }
        Boolean flag = reportApprovalService.verify_data(reportApprovalVo1);
        if (flag) {
            return ResultUtil.success("签发成功");
        }
        return ResultUtil.error(678, "签发失败");
    }

    /**
     * 签发报告——二次开发
     *
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("verify_data_two")
    public Result verify_data_two(@RequestBody ReportApprovalVo reportApprovalVo1) {
        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        if (reportApprovalVo1.getState() == null) {
            return ResultUtil.error(678, "审批信息不能为空");
        }
        if (reportApprovalVo1.getState() != 1 && reportApprovalVo1.getState() != 0) {
            return ResultUtil.error(678, "审批信息有误");
        }
        if (reportApprovalVo1.getState() == 0) {
            if (reportApprovalVo1.getSealTypeArray() == null || reportApprovalVo1.getSealTypeArray().length==0) {
                return ResultUtil.error(678, "印章不能为空");
            }
        }
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
//        reportApprovalVo1.setIssuer(name);
        // 通过报告id 和 登录人id和姓名 比对
        if(!reportApprovalService.efficacyApprovalData(reportApprovalVo1.getId(),userInfo.getUserId(),name,2)){
            return ResultUtil.error(678, "签发失败！当前登录人不是指定人");
        }
        reportApprovalVo1.setIssuer(name);
        Boolean flag = reportApprovalService.verify_data_two(reportApprovalVo1);
        if (flag) {
            return ResultUtil.success("成功");
        }
        return ResultUtil.error(678, "签发失败");
    }

    /**
     * 线上签发保存
     * @param reportApprovalVo1
     * @return
     */
    @PostMapping("verifySave")
    public Result verifySave(@RequestBody ReportApprovalVo reportApprovalVo1) {
        if (reportApprovalVo1 == null) {
            return ResultUtil.error(678, "缺少必填参数！");
        }
        if (reportApprovalVo1.getId() == null) {
            return ResultUtil.error(678, "报告主键不能为空！");
        }
        if (null == reportApprovalVo1.getConclusionMatch() && null == reportApprovalVo1.getQualificationsRange()) {
            return ResultUtil.error(678, "审批信息不能为空！");
        }
        String msg;
        if(reportApprovalVo1.getConclusionMatch().equals(1) || reportApprovalVo1.getQualificationsRange().equals(1)){
            if(null == reportApprovalVo1.getReason()){
                return ResultUtil.error(678, "驳回原因不能为空！");
            }else{
                reportApprovalVo1.setState(1);
                msg = "签发驳回";
            }
        }else{
            reportApprovalVo1.setState(0);//通过
            msg = "签发通过";
        }
        if (reportApprovalVo1.getState() == 0) {
            if (reportApprovalVo1.getSealTypeArray() == null || reportApprovalVo1.getSealTypeArray().length==0 || reportApprovalVo1.getSealTypeArray()[0] == null) {
                return ResultUtil.error(678, "印章不能为空!");
            }
        }
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error(678, "token已经过期，请退出重新登录");
        }
        String name = reportApprovalMapper.getUserName(userInfo.getUserId());
        if (name == null) {
            return ResultUtil.error(678, "账号未配置使用人");
        }
        // 通过报告id 和 登录人id和姓名 比对
        if(!reportApprovalService.efficacyApprovalData(reportApprovalVo1.getId(),userInfo.getUserId(),name,2)){
            return ResultUtil.error(678, "签发失败！当前登录人不是指定人");
        }
        reportApprovalVo1.setIssuer(name);
        Boolean flag = reportApprovalService.verify_data_two(reportApprovalVo1);
        if (flag) {
            // 报告签发完成：记录工时
            if (reportApprovalVo1.getState() == 0) {
                testCheckItemsTaskRelService.handleWorkingHours(reportApprovalVo1.getId(), 0);
            }
            return ResultUtil.success(msg + "成功！", true);
        }
        return ResultUtil.error(678, "签发失败！");
    }

    /**
     * 报告签发历史查询列表
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/verify_history")
    public Result verify_history(String search, Integer pageNum, Integer pageSize,Integer reportTypeStatus) {
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageInfo pageInfo= reportApprovalService.verifyHistory(search,pageNum,pageSize,reportTypeStatus);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 根据检测项 下载原始数据
     *
     * @param id
     * @return
     */
    @GetMapping("download")
    public String downloadData(Long id) {
        if (id == null) {
            return null;
        }
        // 根据检测项 下载原始数据
        return reportApprovalMapper.getCheckItemUrl(id);
    }

    /**
     * 工时从新统计导入
     */
    /**
     * @return
     */
    @GetMapping("verifySave11")
    public Result verifySave(Integer type) {

//        List<Long> taskIds = reportApprovalMapper.getTaskList();
//        List<Long> taskIds = new ArrayList<>();
//        taskIds.add(4689202608451264L);
//        taskIds.add(4689206872841732L);
//        testCheckItemsTaskRelService.testCommit(taskIds);
        List<Long> reortIds = new ArrayList<>();
        reortIds.add(4689202608451264L);
        //TODO:1月5日  查询基础表信息 - 检测类型包含工时
        List<TestInitDataEntity> sqlBasisList = new ArrayList<>();
        sqlBasisList = taskMapper.selectEntrustBasis(30);
//        reortIds.add(4689206872841732L);
        for (Long reportId : reortIds) {
            System.out.println(reportId + " type == " + type);
            testCheckItemsTaskRelService.handleWorkingHours(reportId, type);
        }
        return null;

    }

}
