package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.ActiveContentEntity;
import com.lims.manage.erp.entity.QsMrActiveEntity;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ActiveContentService;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.service.QsMrActiveService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.DingNotifyUtils;
import com.lims.manage.erp.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2024-07-26 11:02
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/mrActive/")
public class QsMrActiveController {
    @Autowired
    private QsMrActiveService qsMrActiveService;
    @Autowired
    private ActiveContentService activeContentService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private DingNotifyUtils dingNotifyUtils;

    /**
     * 分页查询管理评审列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("list")
    public Result list(Integer pageNum,Integer pageSize){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.orderByDesc(QsMrActiveEntity::getTime);
        List<QsMrActiveEntity> list = qsMrActiveService.list(queryWrapper);
        PageInfo<QsMrActiveEntity> pageInfo = new PageInfo<>(list);
        //查询内容纲要
        List<Integer> ids = Lists.newArrayList();
        for (QsMrActiveEntity entity :pageInfo.getList()){
            ids.add(entity.getActiveId());
        }
        if (CollectionUtils.isNotEmpty(ids)){
            LambdaQueryWrapper<ActiveContentEntity> queryWrapper1 = new LambdaQueryWrapper();
            queryWrapper1.in(ActiveContentEntity::getActiveId,ids);
            List<ActiveContentEntity> list1 = activeContentService.list(queryWrapper1);
            if (CollectionUtils.isNotEmpty(list1)){
                for (QsMrActiveEntity entity :pageInfo.getList()){
                    List<ActiveContentEntity> entityList = Lists.newArrayList();
                    for (ActiveContentEntity contentEntity :list1){
                        if (entity.getActiveId().intValue() == contentEntity.getActiveId().intValue()){
                            entityList.add(contentEntity);
                        }
                    }
                }
            }
        }
        return ResultUtil.success(pageInfo);
    }

    /**
     * 创建管理评审
     * @param entity
     * @return
     */
    @Log(title = "创建管理评审", businessType = BusinessType.INSERT)
    @PostMapping("add")
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody QsMrActiveEntity entity){
        if (StringUtils.isEmpty(entity.getApproverName()) || StringUtils.isEmpty(entity.getReviewPurpose())
                || entity.getReviewTime() == null || StringUtils.isEmpty(entity.getReviewPlace())
                || StringUtils.isEmpty(entity.getReviewHost()) || StringUtils.isEmpty(entity.getParticipants())
                || StringUtils.isEmpty(entity.getEditorName()) || StringUtils.isEmpty(entity.getApproverName())
                || CollectionUtils.isEmpty(entity.getList())){
            return ResultUtil.error("缺少参数");
        }
        LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QsMrActiveEntity::getReviewName,entity.getReviewName());
        QsMrActiveEntity one = qsMrActiveService.getOne(queryWrapper);
        if (one != null){
            return ResultUtil.error("此管理评审已存在");
        }
        entity.setTime(new Date(System.currentTimeMillis()));
        qsMrActiveService.save(entity);
        List<ActiveContentEntity> list = entity.getList();
        for (ActiveContentEntity activeContentEntity :list){
            activeContentEntity.setActiveId(entity.getActiveId());
        }
        activeContentService.saveBatch(list);
        //通知所有部门负责人
        Set<Long> ids = deptService.getDingIds();
        if (CollectionUtils.isNotEmpty(ids)){
            List<String> idsByUserIds = sysUserService.getDingIdsByUserIds(ids);
            for (String dingId :idsByUserIds){
                try {
                    dingNotifyUtils.OAWorkNotice(dingId,entity.getReviewName(),entity.getReviewHost(),"管理评审已发布，请相关人员在"+ DateUtil.formatDate(entity.getReviewTime()) +"前提交材料");
                }catch (Exception e){
                    log.error("管理评审创建后钉钉通知失败："+dingId);
                }
            }
        }
        return ResultUtil.success("添加成功",null);
    }

    /**
     * 删除
     * @param activeId
     * @return
     */
    @Log(title = "删除管理评审", businessType = BusinessType.DELETE)
    @PostMapping("delete")
    @Transactional(rollbackFor = Exception.class)
    public Result delete(Integer activeId){
        if (activeId == null){
            return ResultUtil.error("缺少参数");
        }
        qsMrActiveService.removeById(activeId);
        Map<String,Object> map = new HashMap<>();
        map.put("active_id",activeId);
        activeContentService.removeByMap(map);
        return ResultUtil.success("删除成功");
    }

    /**
     * 变更管理评审
     * @param entity
     * @return
     */
    @Log(title = "变更管理评审", businessType = BusinessType.UPDATE)
    @PostMapping("edit")
    @Transactional(rollbackFor = Exception.class)
    public Result edit(@RequestBody QsMrActiveEntity entity){
        if (StringUtils.isEmpty(entity.getApproverName()) || StringUtils.isEmpty(entity.getReviewPurpose())
                || entity.getReviewTime() == null || StringUtils.isEmpty(entity.getReviewPlace())
                || StringUtils.isEmpty(entity.getReviewHost()) || StringUtils.isEmpty(entity.getParticipants())
                || StringUtils.isEmpty(entity.getEditorName()) || StringUtils.isEmpty(entity.getApproverName())
                || CollectionUtils.isEmpty(entity.getList()) || entity.getActiveId() == null){
            return ResultUtil.error("缺少参数");
        }
        QsMrActiveEntity byId = qsMrActiveService.getById(entity.getActiveId());
        if (byId != null){
            if (!entity.getReviewName().equals(byId.getReviewName())){
                LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(QsMrActiveEntity::getReviewName,entity.getReviewName());
                QsMrActiveEntity one = qsMrActiveService.getOne(queryWrapper);
                if (one != null){
                    return ResultUtil.error("此管理评审已存在");
                }
            }
        }
        qsMrActiveService.updateById(entity);
        List<ActiveContentEntity> list = entity.getList();
        for (ActiveContentEntity activeContentEntity :list){
            activeContentEntity.setActiveId(entity.getActiveId());
        }
        //删除旧数据
        Map<String,Object> map = new HashMap<>();
        map.put("active_id",entity.getActiveId());
        activeContentService.removeByMap(map);
        activeContentService.saveBatch(list);
        return ResultUtil.success("变更成功",null);
    }

}
