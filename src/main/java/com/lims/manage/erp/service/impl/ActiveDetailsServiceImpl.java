package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ActiveDetailsService;
import com.lims.manage.erp.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 部门信息 上传附件详情
 *
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-07-26 11:44
 * @Copyright © 河南交科院
 */
@Service
public class ActiveDetailsServiceImpl extends ServiceImpl<ActiveDetailsDao, ActiveDetailsEntity> implements ActiveDetailsService {


    @Autowired
    private QsMrActiveDao qsMrActiveDao;
    @Autowired
    private DeptDao deptDao;
    @Autowired
    private DingNotifyUtils dingNotifyUtils;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private ActiveDetailsFileUrlDao activeDetailsFileUrlDao;

    /**
     * 部门负责人 上传材料
     *
     * @param activeDetailsEntity
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result uploadMaterial(ActiveDetailsEntity activeDetailsEntity, MultipartFile[] file) {

        LambdaQueryWrapper<ActiveDetailsEntity> detailsWrapper = new LambdaQueryWrapper<>();
        detailsWrapper.eq(ActiveDetailsEntity::getActiveId, activeDetailsEntity.getActiveId());
        detailsWrapper.eq(ActiveDetailsEntity::getDeptId, activeDetailsEntity.getDeptId());
        ActiveDetailsEntity detailsEntity = this.baseMapper.selectOne(detailsWrapper);
        if (detailsEntity == null) {
            return ResultUtil.error("操作失败，部门不存在");
        }

        // 验证当前登录人 是否为 部门负责人
        LambdaQueryWrapper<DingDeptEntity> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(DingDeptEntity::getId, activeDetailsEntity.getDeptId());
        DingDeptEntity dingDeptEntity = deptDao.selectOne(deptWrapper);
        if (dingDeptEntity == null) {
            return ResultUtil.error("部门不存在");
        }
        if (dingDeptEntity.getUserId() == null) {
            return ResultUtil.error(dingDeptEntity.getName() + " 部门负责人为空 ");
        }
        //  进行比对 获取业务受理人id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!userInfo.getUserId().toString().equals(dingDeptEntity.getUserId())) {
            return ResultUtil.error("操作失败 " + dingDeptEntity.getName() + " 部门负责人为 " + dingDeptEntity.getUserName());
        }

        if (file != null && file.length > 0) {
            for (MultipartFile multipartFile : file) {
                String nameSuffix = multipartFile.getOriginalFilename().split("\\.")[1];
                if (nameSuffix.equals("png") || nameSuffix.equals("jpg") || nameSuffix.equals("pdf")) {

                } else {
                    return ResultUtil.error("上传材料 应该为 png、jpg、pdf");
                }
            }
            for (MultipartFile multipartFile : file) {
                ActiveDetailsFileUrlEntity activeDetailsFileUrlEntity = new ActiveDetailsFileUrlEntity();
                Long fileCode = GenID.getID();
                String filename = fileCode + "_" + multipartFile.getOriginalFilename();
                String upload = MinIoUtil.upload(BucketsConst.manage_audit, multipartFile, filename);
                if (!org.springframework.util.StringUtils.isEmpty(upload)) {
                    String[] fileUrls = upload.split("\\?");
                    activeDetailsFileUrlEntity.setFileUrl(fileUrls[0]);
                    activeDetailsFileUrlEntity.setFileUrlName(filename);
                    activeDetailsFileUrlEntity.setActiveId(activeDetailsEntity.getActiveId());
                    activeDetailsFileUrlEntity.setActiveDetailId(detailsEntity.getActiveDetailId());
                    activeDetailsFileUrlDao.insert(activeDetailsFileUrlEntity);
                }
            }
            return ResultUtil.success("操作成功");
        }
        return ResultUtil.error("附件不能为空");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result removeFile(ActiveDetailsFileUrlEntity activeDetailsFileUrlEntity) {
        LambdaQueryWrapper<ActiveDetailsEntity> detailsWrapper = new LambdaQueryWrapper<>();
        detailsWrapper.eq(ActiveDetailsEntity::getActiveId, activeDetailsFileUrlEntity.getActiveId());
        detailsWrapper.eq(ActiveDetailsEntity::getActiveDetailId, activeDetailsFileUrlEntity.getActiveDetailId());
        ActiveDetailsEntity detailsEntity = this.baseMapper.selectOne(detailsWrapper);
        if (detailsEntity == null) {
            return ResultUtil.error("操作失败，部门不存在");
        }

        // 验证当前登录人 是否为 部门负责人
        LambdaQueryWrapper<DingDeptEntity> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(DingDeptEntity::getId, detailsEntity.getDeptId());
        DingDeptEntity dingDeptEntity = deptDao.selectOne(deptWrapper);
        if (dingDeptEntity == null) {
            return ResultUtil.error("部门不存在");
        }
        if (dingDeptEntity.getUserId() == null) {
            return ResultUtil.error(dingDeptEntity.getName() + " 部门负责人为空 ");
        }
        //  进行比对 获取业务受理人id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!userInfo.getUserId().toString().equals(dingDeptEntity.getUserId())) {
            return ResultUtil.error("操作失败 " + dingDeptEntity.getName() + " 部门负责人为 " + dingDeptEntity.getUserName());
        }
        // 读取 附件 进行移除。
        LambdaQueryWrapper<ActiveDetailsFileUrlEntity> entityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        entityLambdaQueryWrapper.eq(ActiveDetailsFileUrlEntity::getId, activeDetailsFileUrlEntity.getId());
        ActiveDetailsFileUrlEntity detailsFileUrlEntity = activeDetailsFileUrlDao.selectOne(entityLambdaQueryWrapper);
        if (detailsFileUrlEntity != null) {
            String[] arrays = detailsFileUrlEntity.getFileUrl().split("/");
            MinIoUtil.deleteFile(arrays[arrays.length - 2], arrays[arrays.length - 1]);
        }
        activeDetailsFileUrlDao.deleteById(activeDetailsFileUrlEntity.getId());
        return ResultUtil.success("操作成功");
    }

    /**
     * 根据登录人回显评审部门列表及附件信息
     *
     * @param activeId 活动id
     * @return
     */
    @Override
    public Result getDepartmentAndFile(String activeId) {

        //  进行比对 获取业务受理人id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();

        // 查询当前登录人 是否拥有部门管理者信息
        LambdaQueryWrapper<DingDeptEntity> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(DingDeptEntity::getUserId, userInfo.getUserId());
        List<DingDeptEntity> deptList = deptDao.selectList(deptWrapper);
        if (CollectionUtil.isEmpty(deptList)) {
            return ResultUtil.error("操作失败 当前账号 无部门负责人信息");
        }
        // 返回部门信息 及 附件信息

        //  部门信息 上传附件详情
        LambdaQueryWrapper<ActiveDetailsEntity> detailsQueryWrapper = new LambdaQueryWrapper<>();
        detailsQueryWrapper.eq(ActiveDetailsEntity::getActiveId, activeId);
        List<ActiveDetailsEntity> activeDetailsList = this.baseMapper.selectList(detailsQueryWrapper);

        // 部门中附件信息
        LambdaQueryWrapper<ActiveDetailsFileUrlEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActiveDetailsFileUrlEntity::getActiveId, activeId);
        List<ActiveDetailsFileUrlEntity> activeDetailsFileUrlEntities = activeDetailsFileUrlDao.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(activeDetailsList) && CollectionUtils.isNotEmpty(activeDetailsFileUrlEntities)) {
            for (ActiveDetailsEntity activeDetailsEntity : activeDetailsList) {
                List<ActiveDetailsFileUrlEntity> activeDetailsFileUrls = new ArrayList<>();
                for (ActiveDetailsFileUrlEntity activeDetailsFileUrlEntity : activeDetailsFileUrlEntities) {
                    if (activeDetailsEntity.getActiveDetailId() == activeDetailsFileUrlEntity.getActiveDetailId()) {
                        activeDetailsFileUrls.add(activeDetailsFileUrlEntity);
                    }
                }
                activeDetailsEntity.setActiveDetailsFileUrls(activeDetailsFileUrls);
            }
        }

        return ResultUtil.success(activeDetailsList);
    }

    /**
     * 催办
     *
     * @param detailsEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result hastenWork(ActiveDetailsEntity detailsEntity) {

        LambdaQueryWrapper<ActiveDetailsEntity> detailsWrapper = new LambdaQueryWrapper<>();
        detailsWrapper.eq(ActiveDetailsEntity::getActiveId, detailsEntity.getActiveId());
        detailsWrapper.eq(ActiveDetailsEntity::getDeptId, detailsEntity.getDeptId());
        ActiveDetailsEntity activeDetailsEntity = this.baseMapper.selectOne(detailsWrapper);
        if (activeDetailsEntity == null) {
            return ResultUtil.error("操作失败，部门不存在");
        }

        // 验证当前登录人 是否为 部门负责人
        LambdaQueryWrapper<DingDeptEntity> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(DingDeptEntity::getId, activeDetailsEntity.getDeptId());
        DingDeptEntity dingDeptEntity = deptDao.selectOne(deptWrapper);
        if (dingDeptEntity != null) {

            //  获取业务受理人id
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            // 获取 管理评审信息
            LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(QsMrActiveEntity::getActiveId, detailsEntity.getActiveId());
            QsMrActiveEntity qsMrActiveEntity = qsMrActiveDao.selectOne(queryWrapper);

            try {
                // 获取 任务单下检测人信息 userId
                LambdaQueryWrapper<SysUserEntity> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(SysUserEntity::getUserId, dingDeptEntity.getUserId());
                SysUserEntity userDetails = sysUserDao.selectOne(userWrapper);
                // 钉钉id
                String dingId = userDetails.getDingUserId();
                // 发送内容
                StringBuffer contextBuffer = new StringBuffer();
                contextBuffer.append("在 " + qsMrActiveEntity.getReviewName() + "中 " + " 部门 " + dingDeptEntity.getName() + "  请尽快提交资料");
                String time = DateUtil.formatMinuteDate(new Date());
                contextBuffer.append("催办时间为 " + time);
                dingNotifyUtils.OAWorkNotice(dingId, "管理评审：" + qsMrActiveEntity.getReviewName(), userInfo.getName(), contextBuffer.toString());

            } catch (Exception e) {
                log.error("当前账号登录人 在管理评审中催办任务", e);
            }
        }
        return ResultUtil.success("操作成功");
    }
}
