package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TestCheckItemsTaskRelMapper;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.mapper.TestTaskPoolMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTaskPoolService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 任务单 服务实现类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Service
public class TestTaskPoolServiceImpl extends ServiceImpl<TestTaskPoolMapper, TestTaskPool> implements TestTaskPoolService {

    @Autowired
    private TestTaskPoolMapper taskPoolMapper;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;
    @Autowired
    private TestCheckItemsTaskRelMapper testCheckItemsTaskRelMapper;
    @Autowired
    private TestProductItemDao testProductItemDao;

    /**
     * 任务大厅 展示详情数据
     *
     * @param taskId
     * @param entrustId
     * @return
     */
    @Override
    public Result taskHallDetailsDisplay(Integer taskId, Long entrustId) {
        // 进行构造数据 返回。
        // 1、 展示模拟任务单号
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getId, taskId);
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        // 任务单模拟单据
        TestTaskPool detailedData = taskPoolMapper.selectOne(queryWrapper);
        if (detailedData == null) {
            return ResultUtil.error("查看失败： 任务单不存在");
        }
        // 2、 展示每组下样品列表
        List<SampleEntity> sampleList = sampleEntityMapper.selectSampleListGroup(entrustId);
        // 3、 查看检测项及所属类型：
        //      （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
                // 3.1：通过委托单id 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId);
                // 3.2：通过委托单id 查看检测项下指派人员信息。
        LambdaQueryWrapper<TestCheckItemsTaskRel> taskRelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        taskRelLambdaQueryWrapper.eq(TestCheckItemsTaskRel::getEntrustId,entrustId);
        List<TestCheckItemsTaskRel> itemsTaskRels = testCheckItemsTaskRelMapper.selectList(taskRelLambdaQueryWrapper);
        System.out.println("任务大厅----展示详情数据");
        return null;
    }
}
