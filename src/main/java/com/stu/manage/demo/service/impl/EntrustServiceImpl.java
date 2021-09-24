package com.stu.manage.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.EntrustStat;
import com.stu.manage.demo.entity.SampleStatus;
import com.stu.manage.demo.entity.StatusEntity;
import com.stu.manage.demo.mapper.EntrustMapper;
import com.stu.manage.demo.service.EntrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrustServiceImpl implements EntrustService {
    @Autowired
    private EntrustMapper entrustMapper;
    @Override
    public EntrustInfo onceMore(int entrustId) {
        return entrustMapper.onceMore(entrustId);
    }

    @Override
    public StatusEntity status(Integer id) {
        StatusEntity statusEntity = new StatusEntity();
        //获取委托单状态
        LambdaQueryWrapper<EntrustStat> wrapper = new LambdaQueryWrapper();
        wrapper.eq(EntrustStat::getId, id);
        EntrustStat stat = entrustMapper.selectOne(wrapper);
        statusEntity.setStatus(stat.getStatus());
        //获取委托单下，样品检测状态
        List<SampleStatus> list = entrustMapper.getSampleStat(id);
        //获取任务状态和任务流程审批状态
        List<SampleStatus> ll = entrustMapper.getTaskStat(id);
        for (SampleStatus status :ll) {
            for (SampleStatus sampleStatus:list) {
                if (sampleStatus.getSampleId()==status.getSampleId()){
                    status.setReportStat(sampleStatus.getReportStat());
                }
            }
        }
        statusEntity.setList(ll);
        return statusEntity;
    }
}
