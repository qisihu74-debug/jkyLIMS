package com.stu.manage.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.manage.demo.entity.*;
import com.stu.manage.demo.mapper.EntrustMapper;
import com.stu.manage.demo.service.EntrustService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class EntrustServiceImpl implements EntrustService {
    @Autowired
    private EntrustMapper entrustMapper;
    @Override
    public EntrustInfo onceMore(int entrustId) {
        EntrustInfo entrustInfo = entrustMapper.onceMore(entrustId);
        List<SampleInfoVo> sampleInfo = entrustMapper.getSampleInfo(entrustId);
        List<CheckItemInfoVo> checkItemInfo = entrustMapper.getCheckItemInfo(entrustId);

        for (int i = 0; i < sampleInfo.size(); i++) {
            SampleInfoVo sampleInfoVo = sampleInfo.get(i);
            List<CheckItemInfoVo> items = Lists.newArrayList();
            for (int j = 0; j < checkItemInfo.size(); j++) {
                CheckItemInfoVo checkItemInfoVo = checkItemInfo.get(j);
                if(sampleInfoVo.getProductId() == checkItemInfoVo.getProductId()){
                    items.add(checkItemInfoVo);
                }
            }
            sampleInfoVo.setItems(items);
        }

        EntrustInfo result = new EntrustInfo(entrustInfo,sampleInfo);

        return result;
    }

    @Override
    public List<CheckItemCostVo> getCheckItemsByProductId(int productId) {
        return entrustMapper.getCheckItemsByProductId(productId);
    }

    @Override
    public List<ProductVo> getCheckBasisByProductId(int productId) {
        return entrustMapper.getCheckBasisByProductId(productId);
    }

    @Override
    public StatusEntity status(Integer id) {
        StatusEntity statusEntity = new StatusEntity();
        //获取委托单状态
        LambdaQueryWrapper<EntrustStat> wrapper = new LambdaQueryWrapper();
        wrapper.eq(EntrustStat::getId, id);
        EntrustStat stat = entrustMapper.selectOne(wrapper);
        if (stat != null){
            statusEntity.setStatus(stat.getStatus());
        }
        //获取委托单下，样品检测状态
        List<SampleStatus> list = entrustMapper.getSampleStat(id);
        //获取任务状态和任务流程审批状态
        List<SampleStatus> ll = entrustMapper.getTaskStat(id);
        if (!CollectionUtils.isEmpty(ll) && !CollectionUtils.isEmpty(list)){
            for (SampleStatus status :ll) {
                for (SampleStatus sampleStatus:list) {
                    if (sampleStatus.getSampleId()==status.getSampleId()){
                        status.setReportStat(sampleStatus.getReportStat());
                    }
                }
            }
            statusEntity.setList(ll);
        }
        return statusEntity;
    }
}
