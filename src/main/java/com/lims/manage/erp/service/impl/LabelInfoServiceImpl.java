package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.LabelInfo;
import com.lims.manage.erp.mapper.LabelInfoDao;
import com.lims.manage.erp.service.LabelInfoService;
import com.lims.manage.erp.util.ListUtil;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 标签信息业务层实现类
 *
 * @author: zhq
 * @date: 2023-01-05
 * @version: v1.0
 */
@Slf4j
@Service
public class LabelInfoServiceImpl extends ServiceImpl<LabelInfoDao, LabelInfo> implements LabelInfoService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getLabelId(String labelNames) {
        List<String> dataLabelIdList = new ArrayList<>();
        //根据标签名称获取标签id
        if (StringUtils.isNotEmpty(labelNames)) {
            String[] dataLabels = labelNames.split(",");
            for (String dataLabelName : dataLabels) {
                //根据标签名称获取标签信息
                LabelInfo labelInfo = this.getOne(Wrappers.<LabelInfo>lambdaQuery().eq(LabelInfo::getLabelContent, dataLabelName));
                if (labelInfo != null) {
                    dataLabelIdList.add(labelInfo.getLabelId());
                } else {
                    labelInfo = new LabelInfo();
                    labelInfo.setLabelContent(dataLabelName);
                    labelInfo.setLabelType(CommonConstant.DATA_LABEL_TYPE_1);
                    labelInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
                    labelInfo.setCreateTime(new Date());
                    this.save(labelInfo);
                    dataLabelIdList.add(labelInfo.getLabelId());
                }
            }

        }
        return StringUtils.join(dataLabelIdList, ",");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delLabelInfo(List<String> labelIdList) {
        if(!ListUtils.isEmpty(labelIdList)){
            //对旧标签进行删除
            for (String labelId: labelIdList) {
                //根据标签名称获取标签所涉及的记录条数
                Integer count=baseMapper.getLabelCount(labelId);
                if(count!=null && count==0){
                    //如果没有资料使用则删除
                    this.removeById(labelId);
                }
            }
        }
    }
}
