package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.ActiveDetailsEntity;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.QsMrActiveEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.ActiveDetailsDao;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.mapper.QsMrActiveDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ActiveDetailsService;
import com.lims.manage.erp.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

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
        if (!userInfo.getUserId().equals(dingDeptEntity.getUserId())) {
            return ResultUtil.error("操作失败 " + dingDeptEntity.getName() + " 部门负责人为 " + dingDeptEntity.getUserName());
        }

        // 处理附件 信息 多个 使用 逗号截取
        StringBuffer stringBuilder = new StringBuffer();
        if (file != null && file.length > 0) {
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");

                String upload = MinIoUtil.upload(BucketsConst.manage_audit, multipartFile, fileCode + "." + strings[strings.length - 1]);
                if (!org.springframework.util.StringUtils.isEmpty(upload)) {
                    String[] fileUrls = upload.split("\\?");
                    stringBuilder.append(fileUrls[0]);
                    stringBuilder.append(",");
                }
            }
            detailsEntity.setFileUrl(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
            this.baseMapper.updateById(detailsEntity);
            return ResultUtil.success("操作成功");
        }
        return ResultUtil.error("附件不能为空");
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
