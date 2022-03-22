package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestProductItemVo;
import com.lims.manage.erp.vo.TestProductSelVo;
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
    /*产品*/
    @Resource
    private TestProductDao testProductDao;
    /*产品等级*/
    @Resource
    private TestProductSpecsService testProductSpecsService;
    /*产品判断依据*/
    @Resource
    private TestProductStandardFileRelService testProductStandardFileRelService;
    /*产品类型*/
    @Resource
    private TestProductTypeService testProductTypeService;
    /*判定依据表*/
    @Resource
    private TestStandardFileService testStandardFileService;
    /*产品检测项*/
    @Resource
    private TestProductItemService testProductItemService;
    /*日志*/
    @Resource
    private LogManagerService logManagerService;
    @Override
    public Result addTestProduct(TestProductItemVo testProductItemVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        //判断产品基本信息参数
        if (testProductItemVo.getTestProduct().getProductName()==null){
            return ResultUtil.error("产品名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProduct>().eq("del_flag",0).eq("product_name",testProductItemVo.getTestProduct().getProductName()))!=null){
            return ResultUtil.error("产品名称重复");
        }
        //设置基础信息
        testProductItemVo.getTestProduct().setDelFlag(0);
        testProductItemVo.getTestProduct().setCreateTime(new Date());
        //保存产品
        if (this.save(testProductItemVo.getTestProduct())){
            //设置产品判定依据
            if (testProductItemVo.getStandardRelIds().size()>0){
                for (Integer standardRelId : testProductItemVo.getStandardRelIds()) {
                    testProductStandardFileRelService.save(new TestProductStandardFileRel(testProductItemVo.getTestProduct().getProductId(),standardRelId));
                }
            }
            //设置产品等级
            if (testProductItemVo.getSpecsList().size()>0){
                for (String Specs : testProductItemVo.getSpecsList()) {
                    if (Specs!=null&&Specs!=""){
                        testProductSpecsService.save(new TestProductSpecs(testProductItemVo.getTestProduct().getProductId(),Specs));
                    }
                }
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品"+testProductItemVo.getTestProduct().getProductId()+"成功!", Const.PRODUCT_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!",testProductItemVo);
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品失败!", Const.PRODUCT_MANAGEMENT_LOG,false);
            return ResultUtil.error("保存产品信息失败，未知异常!");
        }
    }

    @Override
    public Result updTestProduct(TestProductItemVo testProductItemVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        //判断产品基本信息参数
        if (testProductItemVo.getTestProduct().getProductName()==null){
            return ResultUtil.error("产品名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProduct>().eq("del_flag",0).ne("product_id",testProductItemVo.getTestProduct().getProductId()).eq("product_name",testProductItemVo.getTestProduct().getProductName()))!=null){
            return ResultUtil.error("产品名称重复");
        }
        //设置基础信息
        testProductItemVo.getTestProduct().setUpdateTime(new Date());
        //修改产品信息
        if (this.updateById(testProductItemVo.getTestProduct())){
            //删除原有依据
            testProductStandardFileRelService.remove(new QueryWrapper<TestProductStandardFileRel>().eq("product_id",testProductItemVo.getTestProduct().getProductId()));
            //设置产品判定依据
            if (testProductItemVo.getStandardRelIds().size()>0){
                for (Integer standardRelId : testProductItemVo.getStandardRelIds()) {
                    testProductStandardFileRelService.save(new TestProductStandardFileRel(testProductItemVo.getTestProduct().getProductId(),standardRelId));
                }
            }
            //删除原产品等级
            testProductSpecsService.remove(new QueryWrapper<TestProductSpecs>().eq("product_id",testProductItemVo.getTestProduct().getProductId()));
            //设置产品等级
            if (testProductItemVo.getSpecsList().size()>0){
                for (String Specs : testProductItemVo.getSpecsList()) {
                    if (Specs!=null&&Specs!=""){
                        testProductSpecsService.save(new TestProductSpecs(testProductItemVo.getTestProduct().getProductId(),Specs));
                    }
                }
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改产品"+testProductItemVo.getTestProduct().getProductId()+"成功!", Const.PRODUCT_MANAGEMENT_LOG,true);
            return ResultUtil.success("保存成功!",testProductItemVo);
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改产品"+testProductItemVo.getTestProduct().getProductId()+"失败!", Const.PRODUCT_MANAGEMENT_LOG,false);
            return ResultUtil.error("保存产品信息失败，未知异常!");
        }
    }

    @Override
    public Result delTestProduct(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestProduct> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestProduct testMethod=new TestProduct();
            testMethod.setUpdateTime(new Date());
            testMethod.setDelFlag(1);
            testMethod.setProductId(aLong.intValue());
            testMethods.add(testMethod);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除产品"+idStr+"成功!", Const.PRODUCT_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除产品"+idStr+"失败!", Const.PRODUCT_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestProductVo> getPageList(Page<TestProductVo> page, QueryWrapper<TestProduct> queryWrapper) {
        return testProductDao.getPageList(page,queryWrapper);
    }

    @Override
    public TestProductSelVo getTestProductSelVo(TestProduct testProduct) {
        //初始化对象
        TestProductSelVo testProductSelVo=new TestProductSelVo();
        //设置产品基本信息
        testProductSelVo.setTestProduct(testProduct);
        //获取并设置产品类型
        if(testProduct.getProductTypeId()!=null){
            testProductSelVo.setProductTypeName(testProductTypeService.getById(testProduct.getProductTypeId()).getProductTypeName());
        }
        //获取并设置产品判断依据
        List<TestProductStandardFileRel> testProductStandardFileRels=testProductStandardFileRelService.list(new QueryWrapper<TestProductStandardFileRel>().eq("product_id",testProduct.getProductId()));
        List<String> StandarString=new ArrayList<>();
        for (TestProductStandardFileRel testProductStandardFileRel : testProductStandardFileRels) {
            StandarString.add(testStandardFileService.getById(testProductStandardFileRel.getStandardFileId()).getName());
        }
        testProductSelVo.setStandardList(StandarString);
        //获取并设置产品等级
        List<TestProductSpecs> testProductSpecs=testProductSpecsService.list(new QueryWrapper<TestProductSpecs>().eq("product_id",testProduct.getProductId()));
        List<String> SpecsList=new ArrayList<>();
        for (TestProductSpecs testProductSpec : testProductSpecs) {
            SpecsList.add(testProductSpec.getSpecs());
        }
        testProductSelVo.setSpecsList(SpecsList);
        //设置产品检测项
        TestProductItem testProductItem=new TestProductItem();
        testProductItem.setProductId(testProduct.getProductId());
        testProductSelVo.setTestProductItemSelVoList(testProductItemService.getTestProductSelVoList(testProductItem));
        return testProductSelVo;
    }

    @Override
    public TestProductItemVo getTestProductItemVo(TestProduct testProduct) {
        //初始化对象
        TestProductItemVo testProductItemVo=new TestProductItemVo();
        //设置产品基本信息
        testProductItemVo.setTestProduct(testProduct);
        //获取并设置产品判断依据
        List<TestProductStandardFileRel> testProductStandardFileRels=testProductStandardFileRelService.list(new QueryWrapper<TestProductStandardFileRel>().eq("product_id",testProduct.getProductId()));
        List<Integer> StandarInteger=new ArrayList<>();
        for (TestProductStandardFileRel testProductStandardFileRel : testProductStandardFileRels) {
            StandarInteger.add(testStandardFileService.getById(testProductStandardFileRel.getStandardFileId()).getId());
        }
        testProductItemVo.setStandardRelIds(StandarInteger);
        //获取并设置产品等级
        List<TestProductSpecs> testProductSpecs=testProductSpecsService.list(new QueryWrapper<TestProductSpecs>().eq("product_id",testProduct.getProductId()));
        List<String> SpecsList=new ArrayList<>();
        for (TestProductSpecs testProductSpec : testProductSpecs) {
            SpecsList.add(testProductSpec.getSpecs());
        }
        testProductItemVo.setSpecsList(SpecsList);
        return testProductItemVo;
    }
}

