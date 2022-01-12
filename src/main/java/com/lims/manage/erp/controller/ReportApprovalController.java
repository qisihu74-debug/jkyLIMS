package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ReportApprovalService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.ReportApprovalVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:39
 * 报告审批
 */
@RestController
@RequestMapping("/report_approval/")
public class ReportApprovalController {

    @Autowired
    ReportApprovalService reportApprovalService;
    @Autowired
    ReportApprovalMapper reportApprovalMapper;

    /**
     * 报告审批列表
     *
     * @param search
     * @param state
     * @return
     */
    @GetMapping("/applyfor")
    public Result applyfor(String search, Integer state) {
        List<ReportApprovalVo> list = reportApprovalService.getApplyforList(search, state);
        if (!list.isEmpty()) {
            return ResultUtil.success(list);
        }
        return ResultUtil.success("查询数据为空");
    }

    /**
     * 抢单
     * @param id
     * @return
     */
    @PostMapping("applyfor_monad")
    public Result applyfor_monad(@Param(value = "id") Long id) {
        if (id == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error(678, "token已经过期");
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
     * 审批数据
     * @param id
     * @param peroration
     * @param reason
     * @return
     */
    @PostMapping("approval_data")
    public Result approval_data(@Param(value = "id") Long id,@Param(value = "peroration")Integer peroration,@Param(value = "reason")String reason) {
        if (id == null) {
            return ResultUtil.error(678, "任务单主键不能为空");
        }
        if(peroration==null){
            return ResultUtil.error(678, "审批信息不能为空");
        }
        if(peroration!=1&&peroration!=0){
            return ResultUtil.error(678, "审批信息有误");
        }
        //1、 获取抢单人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error(678, "token已经过期");
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
        if(reportApprovalVo.getVerifyer()==null){
            return ResultUtil.error(678, "请先抢单");
        }
        if (!reportApprovalVo.getVerifyer().equals(name)) {
            return ResultUtil.error(678, "审批失败，审批人与抢单人不一致");
        }
        Boolean flag = reportApprovalService.approval_data(id,peroration,reason);
        if (flag) {
            return ResultUtil.success("审批成功");
        }
        return ResultUtil.error(678, "审批失败");
    }



}
