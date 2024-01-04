package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.InternalAudit;
import com.lims.manage.erp.entity.InternalAuditInfo;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.InternalAuditInfoService;
import com.lims.manage.erp.service.InternalAuditService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.DingNotifyUtils;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2024-01-02 14:48
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/internalAudit/")
public class InternalAuditController {
    @Autowired
    private InternalAuditService auditService;
    @Autowired
    private InternalAuditInfoService auditInfoService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private DingNotifyUtils dingNotifyUtils;

    /**
     * 获取具有内审角色的人员列表
     * @return
     */
    @GetMapping("auditUserList")
    public Result auditUserList(){
        List<SysUserEntity> list = sysUserService.auditUserList();
        return ResultUtil.success(list);
    }

    /**
     * 内审计划列表
     * @param pageNum
     * @param pageSize
     * @param search
     * @return
     */
    @GetMapping("planList")
    public Result planList(Integer pageNum,Integer pageSize,String search){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        PageInfo<InternalAudit> pageInfo = auditService.planList(pageSize,pageNum,search,ShiroUtils.getUserInfo());
        return ResultUtil.success(pageInfo);
    }

    /**
     * 创建计划
     * @param jsonParam
     * @param file
     * @return
     */
    @PostMapping("addPlan")
    @Transactional(rollbackFor = Exception.class)
    public Result addPlan(@RequestParam("jsonParam") String jsonParam, MultipartFile file){
        if (StringUtils.isEmpty(jsonParam) || file == null){
            return ResultUtil.error("缺少参数");
        }
        String filename = file.getOriginalFilename();
        String[] split = filename.split("\\.");
        if (!"pdf".equals(split[split.length - 1])) {
            return ResultUtil.error("文件类型不正确，请上传pdf文件类型");
        }
        InternalAudit internalAudit = JSON.parseObject(jsonParam, InternalAudit.class);
        List<InternalAuditInfo> list = internalAudit.getList();
        if (CollectionUtils.isEmpty(list)){
            return ResultUtil.error("缺少审核员信息");
        }
        LambdaQueryWrapper<InternalAudit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InternalAudit::getName,internalAudit.getName());
        InternalAudit one = auditService.getOne(queryWrapper);
        if (one != null){
            return ResultUtil.error("内审计划已存在");
        }
        //上传附件
        String upload = MinIoUtil.upload(BucketsConst.internal_audit, file, file.getOriginalFilename());
        if (!StringUtils.isEmpty(upload)) {
            String uploadUrl = upload.substring(0, upload.indexOf("?"));
            internalAudit.setFileUrl(uploadUrl);
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        internalAudit.setOperateId(userInfo.getUserId());
        internalAudit.setOperateName(userInfo.getName());
        internalAudit.setOperateDate(new Date(System.currentTimeMillis()));
        auditService.save(internalAudit);
        for (InternalAuditInfo auditInfo :list){
            auditInfo.setAuditId(internalAudit.getId());
        }
        auditInfoService.saveBatch(list);
        //给审核相关人员发布钉钉通知 TODO
        return ResultUtil.success("保存成功");
    }

    /**
     * 编辑计划
     * @param jsonParam
     * @param file
     * @return
     */
    @PostMapping("editPlan")
    @Transactional(rollbackFor = Exception.class)
    public Result editPlan(@RequestParam("jsonParam") String jsonParam, MultipartFile file){
        if (StringUtils.isEmpty(jsonParam)){
            return ResultUtil.error("缺少参数");
        }
        if (file != null) {
            String filename = file.getOriginalFilename();
            String[] split = filename.split("\\.");
            if (!"pdf".equals(split[split.length - 1])) {
                return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
            }
        }
        InternalAudit internalAudit = JSON.parseObject(jsonParam, InternalAudit.class);
        List<InternalAuditInfo> list = internalAudit.getList();
        if (CollectionUtils.isEmpty(list)){
            return ResultUtil.error("缺少审核员信息");
        }
        LambdaQueryWrapper<InternalAudit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InternalAudit::getName,internalAudit.getName());
        InternalAudit one = auditService.getOne(queryWrapper);
        if (!one.getName().equals(internalAudit.getName())){
            LambdaQueryWrapper<InternalAudit> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper.eq(InternalAudit::getName,internalAudit.getName());
            InternalAudit one1 = auditService.getOne(queryWrapper1);
            if (one1 != null){
                return ResultUtil.error("内审计划已存在");
            }
        }
        //如果文件发生变更删除旧文件，更新新文件
        if (file != null) {
            String fileUrl = one.getFileUrl();
            if (!StringUtils.isEmpty(fileUrl)) {
                auditService.delFileByUrl(fileUrl);
            }
            String upload = MinIoUtil.upload(BucketsConst.internal_audit, file, file.getOriginalFilename());
            if (!StringUtils.isEmpty(upload)) {
                String uploadUrl = upload.substring(0, upload.indexOf("?"));
                one.setFileUrl(uploadUrl);
            }
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        one.setOperateId(userInfo.getUserId());
        one.setOperateName(userInfo.getName());
        one.setOperateDate(new Date(System.currentTimeMillis()));
        one.setAuditLeaderId(internalAudit.getAuditLeaderId());
        one.setAuditLeaderName(internalAudit.getAuditLeaderName());
        one.setAuditDate(internalAudit.getAuditDate());
        one.setName(internalAudit.getName());
        auditService.updateById(one);
        //处理审核人信息
        LambdaQueryWrapper<InternalAuditInfo> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(InternalAuditInfo::getAuditId,internalAudit.getId());
        List<InternalAuditInfo> auditInfos = auditInfoService.list(lambdaQueryWrapper);
        List<Integer> ids = Lists.newArrayList();
        List<Integer> ids2 = Lists.newArrayList();

        //删除数据
        for (InternalAuditInfo info :auditInfos){
            ids.add(info.getId());
        }
        for (InternalAuditInfo auditInfo :list){
            if (auditInfo.getId() != null){
                ids2.add(auditInfo.getId());
            }
        }
        //差集删除取消掉的内审员
        ids.removeAll(ids2);
        if (CollectionUtils.isNotEmpty(ids)){
            auditInfoService.removeByIds(ids);
            //发送钉钉通知 TODO
        }
        //更新数据
        for (InternalAuditInfo info :list){
            info.setAuditId(internalAudit.getId());
            for (InternalAuditInfo auditInfo :auditInfos){
                if (info.getId() != null){
                    if (info.getId().equals(auditInfo.getId())){
                        info.setFileUrl(auditInfo.getFileUrl());
                    }
                }
            }
        }
        auditInfoService.saveOrUpdateBatch(list);
        return ResultUtil.success("保存成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("delete")
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam("ids") List<String> ids){
        if (CollectionUtils.isEmpty(ids)){
            return ResultUtil.error("缺少参数");
        }
        //删除内审计划信息
        auditService.removeByIds(ids);
        //删除内审员信息
        LambdaQueryWrapper<InternalAuditInfo> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(InternalAuditInfo::getAuditId,ids);
        List<InternalAuditInfo> list = auditInfoService.list(queryWrapper);
        List<Integer> integers = Lists.newArrayList();
        for (InternalAuditInfo info :list){
            integers.add(info.getId());
        }
        auditInfoService.removeByIds(integers);
        return ResultUtil.success("删除成功");
    }

    /**
     * 上传报告
     * @param id
     * @param file
     * @param type 1内审员上传，2体系管理员上传
     * @return
     */
    @RequestMapping("uploadFile")
    public Result uploadFile(@RequestParam("type") Integer type, @RequestParam("id") Integer id,MultipartFile file){
        if (type == null || id == null || file  == null){
            return ResultUtil.error("缺少参数");
        }
        if (type.intValue()>2 || type.intValue()<1){
            return ResultUtil.error("上传类型未定义");
        }
        String upload = MinIoUtil.upload(BucketsConst.internal_audit, file, file.getOriginalFilename());
        String uploadUrl = upload.substring(0, upload.indexOf("?"));
        if (type.intValue() == 2){
            LambdaUpdateWrapper<InternalAudit> updateWrapper = new LambdaUpdateWrapper();
            updateWrapper.eq(InternalAudit::getId,id);
            updateWrapper.set(InternalAudit::getReportUrl,uploadUrl);
            auditService.update(null,updateWrapper);
            //判断计划下所有的评审附件是否上传完成，如果完成更新状态为：已完成 TODO
        }else {
            LambdaUpdateWrapper<InternalAuditInfo> updateWrapper = new LambdaUpdateWrapper();
            updateWrapper.eq(InternalAuditInfo::getAuditId,id);
            updateWrapper.eq(InternalAuditInfo::getAuditorId,ShiroUtils.getUserInfo().getUserId());
            updateWrapper.set(InternalAuditInfo::getFileUrl,uploadUrl);
            auditInfoService.update(null,updateWrapper);
            //判断计划下所有的评审附件是否上传完成，如果完成更新状态为：已完成
        }
        return ResultUtil.success("上传成功");
    }
}
