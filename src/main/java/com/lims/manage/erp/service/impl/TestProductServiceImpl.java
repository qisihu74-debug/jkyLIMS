package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductService;
import com.lims.manage.erp.vo.TestProductVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 产品信息(TestProduct)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 15:00:15
 */
@Service("testProductService")
public class TestProductServiceImpl extends ServiceImpl<TestProductDao, TestProduct> implements TestProductService {
    @Resource
    private TestProductDao testProductDao;
    @Override
    public Result addTestProduct(TestProduct testProduct) {
        if (testProduct.getProductName()==null){
            return ResultUtil.error("产品名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProduct>().eq("product_name",testProduct.getProductName()))!=null){
            return ResultUtil.error("产品名称重复");
        }
        testProduct.setStatus("0");
        testProduct.setDelFlag(0);
        testProduct.setCreateTime(new Date());
        if (this.save(testProduct)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestProduct(TestProduct testProduct) {
        if (testProduct.getProductId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testProduct.getProductName()==null){
            return ResultUtil.error("产品名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProduct>().eq("product_name",testProduct.getProductName()).eq("del_flag",0).ne("product_id",testProduct.getProductId()))!=null){
            return ResultUtil.error("产品名称重复");
        }
        testProduct.setUpdateTime(new Date());
        if (this.updateById(testProduct)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTestProduct(List<Long> idList) {
        List<TestProduct> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestProduct testMethod=new TestProduct();
            testMethod.setUpdateTime(new Date());
            testMethod.setDelFlag(1);
            testMethod.setProductId(aLong.intValue());
            testMethods.add(testMethod);
        }
        if (this.updateBatchById(testMethods)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestProductVo> getPageList(Page<TestProductVo> page, QueryWrapper<TestProduct> queryWrapper) {
        return testProductDao.getPageList(page,queryWrapper);
    }
}

