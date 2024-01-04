package com.lims.manage.erp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.ManageReviewInformationEntity;
import com.lims.manage.erp.entity.ManageReviewPlanEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.ManageReviewInformationEntityMapper;
import com.lims.manage.erp.mapper.ManageReviewPlanEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ManagementReviewService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/1/2 16:59
 */
@Service("managementReviewService")
public class ManagementReviewServiceImpl extends ServiceImpl<ManageReviewPlanEntityMapper, ManageReviewPlanEntity> implements ManagementReviewService {

    @Resource
    private ManageReviewPlanEntityMapper manageReviewPlanEntityMapper;
    @Resource
    private ManageReviewInformationEntityMapper manageReviewInformationEntityMapper;

    @Override
    public Result getList(ManageReviewPlanEntity manageReviewPlanEntity) {

        List<ManageReviewPlanEntity> list = manageReviewPlanEntityMapper.selectList(null);

        return ResultUtil.success(list);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result addManageReviewPlanEntity(ManageReviewPlanEntity manageReviewPlanEntity, MultipartFile[] file) {
        // 效验数据 评审目的
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewPurpose())) {
            return ResultUtil.error("评审目的不能为空");
        }
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewStartTime()) || StringUtils.isEmpty(manageReviewPlanEntity.getReviewEndTime())) {
            return ResultUtil.error("评审时间不能为空");
        }
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewHost())) {
            return ResultUtil.error("评审主持不能为空");
        }
        // 参加人员不能为空
        if (CollectionUtils.isEmpty(manageReviewPlanEntity.getManageReviewInformationEntities())) {
            return ResultUtil.error("参加人员不能为空");
        }
        // 创建人：
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        manageReviewPlanEntity.setPlanCreator(userInfo.getName() + "&" + userInfo.getUserId());
        manageReviewPlanEntity.setDelFlag(0);
        manageReviewPlanEntity.setCreateTime(new Date());
        manageReviewPlanEntity.setId(null);
        // 创建计划
        manageReviewPlanEntityMapper.insertSelective(manageReviewPlanEntity);
        if (manageReviewPlanEntity.getId() == null) {
            return null;
        }
        // 创建人新增
        fileAdd(manageReviewPlanEntity, "创建人", file);
        // 参加人员新增
        if (CollectionUtil.isNotEmpty(manageReviewPlanEntity.getManageReviewInformationEntities())) {
            for (ManageReviewInformationEntity informationEntity1 : manageReviewPlanEntity.getManageReviewInformationEntities()) {
                // 参加新增
                manageReviewPlanEntity.setPlanCreator(informationEntity1.getPlanCreator());
                fileAdd(manageReviewPlanEntity, "参加人员", null);
            }
        }
        return ResultUtil.success("创建成功");
    }

    /**
     * 方法处理
     *
     * @param manageReviewPlanEntity 计划信息
     * @param duties                 职务
     * @param file                   附件
     */
    @Transactional(rollbackFor = Exception.class)
    public void fileAdd(ManageReviewPlanEntity manageReviewPlanEntity, String duties, MultipartFile[] file) {
        // 创建人信息新增
        ManageReviewInformationEntity informationEntity = new ManageReviewInformationEntity();
        // 创建人
        informationEntity.setPlanCreator(manageReviewPlanEntity.getPlanCreator());
        // 职务
        informationEntity.setDuties(duties);
        // 创建时间
        informationEntity.setCreateTime(new Date());
        // 状态
        informationEntity.setDelFlag(0);
        informationEntity.setPid(manageReviewPlanEntity.getId());
        informationEntity.setId(null);
        StringBuffer fileNameBuffer = new StringBuffer();
        StringBuffer originalFileNameBuffer = new StringBuffer();
        //附件存在上传附件到服务器
        if (file != null && file.length != 0) {
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload("manage-audit", multipartFile, fileCode + "." + strings[strings.length - 1]);
                // 截取 \\? 前数据
                String filePath = upload.split("\\?")[0];
                fileNameBuffer.append(filePath);
                fileNameBuffer.append(",");
                originalFileNameBuffer.append(name);
                originalFileNameBuffer.append(",");
            }
        }
        // 补充文件附件
        if (fileNameBuffer.length() >= 1) {
            informationEntity.setFileUrl(fileNameBuffer.deleteCharAt(fileNameBuffer.length() - 1).toString());
        }
        if (originalFileNameBuffer.length() >= 1) {
            informationEntity.setOriginalFileName(originalFileNameBuffer.deleteCharAt(originalFileNameBuffer.length() - 1).toString());
        }
        manageReviewInformationEntityMapper.insert(informationEntity);

    }
}
