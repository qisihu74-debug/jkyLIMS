package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.mapper.TestReportQualifcationDao;
import com.lims.manage.erp.entity.TestReportQualifcation;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestReportQualifcationService;
import com.lims.manage.erp.vo.TestReportQualifcationVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (TestReportQualifcation)表服务实现类
 *
 * @author makejava
 * @since 2022-03-14 14:33:33
 */
@Service("testReportQualifcationService")
public class TestReportQualifcationServiceImpl extends ServiceImpl<TestReportQualifcationDao, TestReportQualifcation> implements TestReportQualifcationService {
    @Resource
    private TestReportQualifcationDao testReportQualifcationDao;


    @Override
    public Result addtestReportQualifcation(TestReportQualifcation testReportQualifcation) {
        testReportQualifcation.setStatus("0");
        if (this.save(testReportQualifcation)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public Result updtestReportQualifcation(TestReportQualifcation testReportQualifcation) {
        if (testReportQualifcation.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        testReportQualifcation.setUpdateTime(new Date());
        if (this.updateById(testReportQualifcation)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result deltestReportQualifcation(List<Long> idList) {
        List<TestReportQualifcation> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestReportQualifcation testStandardFile=new TestReportQualifcation();
            testStandardFile.setId(aLong.intValue());
            testStandardFile.setDelFlag(1);
            testMethods.add(testStandardFile);
        }
        if (this.updateBatchById(testMethods)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }

    }

    @Override
    public IPage<TestReportQualifcationVo> getPageList(Page<TestReportQualifcationVo> page, QueryWrapper<TestReportQualifcation> queryWrapper) {
        return testReportQualifcationDao.getListPage(page,queryWrapper);
    }
}

