package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.QsMrActiveEntity;
import com.lims.manage.erp.mapper.QsMrActiveDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.QsMrActiveService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-07-26 11:15
 * @Copyright © 河南交科院
 */
@Service
public class QsMrActiveServiceImpl extends ServiceImpl<QsMrActiveDao, QsMrActiveEntity> implements QsMrActiveService {

    /**
     * 提交管理评审总结
     *
     * @param qsMrActiveEntity
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitInternalAuditDocument(QsMrActiveEntity qsMrActiveEntity, MultipartFile[] file) {
        QsMrActiveEntity entity = this.baseMapper.selectById(qsMrActiveEntity.getActiveId());
        if (entity == null) {
            return ResultUtil.error("管理评审为空");
        }
        if (!entity.getState().equals("进行中")) {
            return ResultUtil.error("操作失败： " + entity.getReviewName() + " 状态为 " + entity.getState());
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
            entity.setFileUrl(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
            entity.setState("已完成");
            this.baseMapper.updateById(entity);
            return ResultUtil.success("操作成功");
        }
        return ResultUtil.error("附件不能为空");
    }
}
