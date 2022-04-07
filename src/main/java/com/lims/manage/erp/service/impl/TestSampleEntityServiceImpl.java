package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.service.TestSampleEntityService;
import com.lims.manage.erp.vo.SampleEntrustAddVo;
import com.lims.manage.erp.vo.SampleSimpleListVo;
import com.lims.manage.erp.vo.SampleDetailAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class TestSampleEntityServiceImpl extends ServiceImpl<TestSampleEntityMapper, TestSampleEntity> implements TestSampleEntityService {
    @Autowired
    private TestSampleEntityMapper testSampleEntityMapper;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;

    @Override
    public Integer batchInsertSample(List<SampleDetailAddVo> samples) {
        List<TestSampleEntity> entities = Lists.newArrayList();
        //获取数据库当前年份最大的样品编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        String maxNumber = sampleEntityMapper.getMaxNumber(sdf.format(now));
        int newMax;
        if (maxNumber == null) {
            newMax = 0;
        } else {
            newMax = Integer.parseInt(maxNumber);
        }
        for (int i = 0; i < samples.size(); i++) {
            int code = newMax + i + 1;
            String codeStr = new DecimalFormat("0000").format(code);
            String sampleCode;
            if (Integer.parseInt(samples.get(i).getSampleQuantity()) > 1) {
                String numStr = new DecimalFormat("00").format(Integer.parseInt(samples.get(i).getSampleQuantity()));
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr + "（01~" + numStr + "）";
            } else {
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
            }
            TestSampleEntity entity = new TestSampleEntity(samples.get(i), sampleCode);
            entities.add(entity);
        }
        return testSampleEntityMapper.insertBatch(entities);
    }

    @Override
    public PageInfo querySampleList(TestSampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        List<SampleSimpleListVo> simpleList = testSampleEntityMapper.getSimpleList(sampleEntity);
        PageInfo<SampleSimpleListVo> pageInfo = new PageInfo<>(simpleList);
        return pageInfo;
    }
}
