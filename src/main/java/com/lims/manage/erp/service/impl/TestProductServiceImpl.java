package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.mapper.TestReportTemplateProductRefDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestProductItemVo;
import com.lims.manage.erp.vo.TestProductSelVo;
import com.lims.manage.erp.vo.TestProductVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    /*报告模板xls与产品关联*/
    @Resource
    private TestReportTemplateProductRefDao testReportTemplateProductRefDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addTestProduct(TestProductItemVo testProductItemVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        //判断产品基本信息参数
        if (testProductItemVo.getTestProduct().getProductName() == null) {
            return ResultUtil.error("产品名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProduct>().eq("del_flag", 0).eq("product_name", testProductItemVo.getTestProduct().getProductName())) != null) {
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
            if (testProductItemVo.getSpecsList().size() > 0) {
                for (String Specs : testProductItemVo.getSpecsList()) {
                    if (Specs != null && Specs != "") {
                        testProductSpecsService.save(new TestProductSpecs(testProductItemVo.getTestProduct().getProductId(), Specs));
                    }
                }
            }
            //存储报告模板xls与产品关联
            if (CollectionUtil.isNotEmpty(testProductItemVo.getTemplateIds())) {
                LambdaQueryWrapper<TestReportTemplateProductRef> productRefLambdaQueryWrapper = new LambdaQueryWrapper<>();
                productRefLambdaQueryWrapper.eq(TestReportTemplateProductRef::getProductId, testProductItemVo.getTestProduct().getProductId());
                testReportTemplateProductRefDao.delete(productRefLambdaQueryWrapper);
                for (Integer templateId : testProductItemVo.getTemplateIds()) {
                    TestReportTemplateProductRef templateProductRef = new TestReportTemplateProductRef();
                    templateProductRef.setProductId(testProductItemVo.getTestProduct().getProductId());
                    templateProductRef.setTemplateId(templateId);
                    testReportTemplateProductRefDao.insert(templateProductRef);
                }
            }
            //保存产品与报告关系
            Integer productId = testProductItemVo.getTestProduct().getProductId();
            Long reportId = testProductItemVo.getTestProduct().getReportId();
            ProductReportRelEntity productReportRelEntity = new ProductReportRelEntity(productId.longValue(), reportId);
            testProductDao.insertProductReportRel(productReportRelEntity);
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "添加产品" + testProductItemVo.getTestProduct().getProductId() + "成功!", Const.PRODUCT_MANAGEMENT_LOG, true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品失败!", Const.PRODUCT_MANAGEMENT_LOG,false);
            return ResultUtil.error("保存产品信息失败，未知异常!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            testProductSpecsService.remove(new QueryWrapper<TestProductSpecs>().eq("product_id", testProductItemVo.getTestProduct().getProductId()));
            //设置产品等级
            if (testProductItemVo.getSpecsList().size() > 0) {
                for (String Specs : testProductItemVo.getSpecsList()) {
                    if (Specs != null && Specs != "") {
                        testProductSpecsService.save(new TestProductSpecs(testProductItemVo.getTestProduct().getProductId(), Specs));
                    }
                }
            }
            // 删除 报告模板xls与产品关联
            LambdaQueryWrapper<TestReportTemplateProductRef> productRefLambdaQueryWrapper = new LambdaQueryWrapper<>();
            productRefLambdaQueryWrapper.eq(TestReportTemplateProductRef::getProductId, testProductItemVo.getTestProduct().getProductId());
            testReportTemplateProductRefDao.delete(productRefLambdaQueryWrapper);
            //存储报告模板xls与产品关联
            if (CollectionUtil.isNotEmpty(testProductItemVo.getTemplateIds())) {
                for (Integer templateId : testProductItemVo.getTemplateIds()) {
                    TestReportTemplateProductRef templateProductRef = new TestReportTemplateProductRef();
                    templateProductRef.setProductId(testProductItemVo.getTestProduct().getProductId());
                    templateProductRef.setTemplateId(templateId);
                    testReportTemplateProductRefDao.insert(templateProductRef);
                }
            }
            //删除原报告与报告关系
            Integer productId = testProductItemVo.getTestProduct().getProductId();
            testProductDao.deleteProductReportRel(productId.longValue());
            //保存新产品与报告关系
            Long reportId = testProductItemVo.getTestProduct().getReportId();
            if (reportId != null) {
                ProductReportRelEntity productReportRelEntity = new ProductReportRelEntity(productId.longValue(), reportId);
                testProductDao.insertProductReportRel(productReportRelEntity);
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "修改产品" + testProductItemVo.getTestProduct().getProductId() + "成功!", Const.PRODUCT_MANAGEMENT_LOG, true);
            return ResultUtil.success("修改成功!");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "修改产品" + testProductItemVo.getTestProduct().getProductId() + "失败!", Const.PRODUCT_MANAGEMENT_LOG, false);
            return ResultUtil.error("保存产品信息失败，未知异常!");
        }
    }

    /**
     * 删除产品-及绑定关系
     *
     * @param idList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delTestProduct(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        // 通过 产品id 查询业务中数据，存在业务数据 删除失败。
        Integer count = testProductDao.selectSampleNumberCount(idList);
        if (count > 0) {
            return ResultUtil.error("删除失败，产品基础信息与业务信息参与绑定");
        }
        for (Long aLong : idList) {
            LambdaUpdateWrapper<TestProduct> testProductWrapper = new LambdaUpdateWrapper<>();
            testProductWrapper.eq(TestProduct::getProductId, aLong);
            testProductWrapper.set(TestProduct::getDelFlag, 1);
            testProductDao.update(null, testProductWrapper);
        }
        // TODO： 2023年12月7日 产品数据执行软删除。
/*//        for (Long aLong : idList) {
//            LambdaQueryWrapper<TestProduct> productLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            productLambdaQueryWrapper.eq(TestProduct::getProductId, aLong);
//            this.remove(productLambdaQueryWrapper);
//            //删除原有依据
//            testProductStandardFileRelService.remove(new QueryWrapper<TestProductStandardFileRel>().eq("product_id", aLong));
//            //删除原产品等级
//            testProductSpecsService.remove(new QueryWrapper<TestProductSpecs>().eq("product_id", aLong));
//            // 删除 报告模板xls与产品关联
//            LambdaQueryWrapper<TestReportTemplateProductRef> productRefLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            productRefLambdaQueryWrapper.eq(TestReportTemplateProductRef::getProductId, aLong);
//            testReportTemplateProductRefDao.delete(productRefLambdaQueryWrapper);
//            //删除原报告与报告关系
//            testProductDao.deleteProductReportRel(aLong);
//        }*/
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "删除产品" + idList.toArray() + "成功!", Const.PRODUCT_MANAGEMENT_LOG, true);
        return ResultUtil.success("删除成功");
    }

    @Override
    public IPage<TestProductVo> getPageList(Page<TestProductVo> page, QueryWrapper<TestProduct> queryWrapper) {
        IPage<TestProductVo> list = testProductDao.getPageList(page, queryWrapper);
//        if (CollectionUtil.isNotEmpty(list.getRecords())) {
//            // 起始条数
//            Long initialNumber = (page.getCurrent() * page.getSize()) - page.getSize();
//            // 序号
//            Integer serialNumber = 1 + initialNumber.intValue();
//            for (TestProductVo productVo : list.getRecords()) {
//                // 进行设置数据序号
//                productVo.setSerialNumber(+serialNumber);
//                serialNumber += 1;
//            }
//        }
        return list;
    }

    @Override
    public TestProductSelVo getTestProductSelVo(TestProduct testProduct) {
        //初始化对象
        TestProductSelVo testProductSelVo = new TestProductSelVo();
        //设置产品基本信息
        testProductSelVo.setTestProduct(testProduct);
        //获取并设置产品类型
        if (testProduct.getProductTypeId() != null) {
            testProductSelVo.setProductTypeName(testProductTypeService.getById(testProduct.getProductTypeId()).getProductTypeName());
        }
        //获取并设置产品判断依据
        List<TestProductStandardFileRel> testProductStandardFileRels = testProductStandardFileRelService.list(new QueryWrapper<TestProductStandardFileRel>().eq("product_id", testProduct.getProductId()));
        List<String> StandarString = new ArrayList<>();
        for (TestProductStandardFileRel testProductStandardFileRel : testProductStandardFileRels) {
            StandarString.add(testStandardFileService.getById(testProductStandardFileRel.getStandardFileId()).getName());
        }
        testProductSelVo.setStandardList(StandarString);
        //获取并设置产品等级
        List<TestProductSpecs> testProductSpecs = testProductSpecsService.list(new QueryWrapper<TestProductSpecs>().eq("product_id", testProduct.getProductId()));
        List<String> SpecsList = new ArrayList<>();
        for (TestProductSpecs testProductSpec : testProductSpecs) {
            SpecsList.add(testProductSpec.getSpecs());
        }
        testProductSelVo.setSpecsList(SpecsList);
        //设置产品检测项
        TestProductItem testProductItem = new TestProductItem();
        testProductItem.setProductId(testProduct.getProductId());
        testProductSelVo.setTestProductItemSelVoList(testProductItemService.getTestProductSelVoList(testProductItem));
        return testProductSelVo;
    }

    @Override
    public TestProductItemVo getTestProductItemVo(TestProduct testProduct) {
        //初始化对象
        TestProductItemVo testProductItemVo = new TestProductItemVo();
        //设置产品基本信息
        testProductItemVo.setTestProduct(testProduct);
        //获取并设置产品判断依据
        List<TestProductStandardFileRel> testProductStandardFileRels = testProductStandardFileRelService.list(new QueryWrapper<TestProductStandardFileRel>().eq("product_id", testProduct.getProductId()));
        List<Integer> StandarInteger = new ArrayList<>();
        for (TestProductStandardFileRel testProductStandardFileRel : testProductStandardFileRels) {
            TestStandardFile byId = testStandardFileService.getById(testProductStandardFileRel.getStandardFileId());
            if (byId != null) {
                StandarInteger.add(byId.getId());
            }
        }
        testProductItemVo.setStandardRelIds(StandarInteger);
        //获取并设置产品等级
        List<TestProductSpecs> testProductSpecs = testProductSpecsService.list(new QueryWrapper<TestProductSpecs>().eq("product_id", testProduct.getProductId()));
        List<String> SpecsList = new ArrayList<>();
        for (TestProductSpecs testProductSpec : testProductSpecs) {
            SpecsList.add(testProductSpec.getSpecs());
        }
        testProductItemVo.setSpecsList(SpecsList);
        // 报告模板xls与产品关联
        List<Integer> templateIds = new ArrayList<>();
        LambdaQueryWrapper<TestReportTemplateProductRef> productRefLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productRefLambdaQueryWrapper.eq(TestReportTemplateProductRef::getProductId, testProduct.getProductId());
        List<TestReportTemplateProductRef> templateProductRefs = testReportTemplateProductRefDao.selectList(productRefLambdaQueryWrapper);
        for (TestReportTemplateProductRef templateProductRef : templateProductRefs) {
            templateIds.add(templateProductRef.getTemplateId());
        }
        testProductItemVo.setTemplateIds(templateIds);
        return testProductItemVo;
    }

    @Override
    public TestProduct getProductInfo(Integer productId) {
        return testProductDao.getProductInfo(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateProductStatus(TestProductItemVo testProductItemVo) {
        if (testProductItemVo.getTestProduct() == null) {
            return ResultUtil.success("产品信息为空");
        }
        if (testProductItemVo.getTestProduct().getProductId() == null) {
            return ResultUtil.success("产品id为空");
        }
        if (testProductItemVo.getTestProduct().getStatus() == null) {
            return ResultUtil.success("产品状态为空");
        }
        LambdaUpdateWrapper<TestProduct> lambdaQueryWrapper = new LambdaUpdateWrapper<>();
        lambdaQueryWrapper.eq(TestProduct::getProductId, testProductItemVo.getTestProduct().getProductId());
        lambdaQueryWrapper.set(TestProduct::getStatus, testProductItemVo.getTestProduct().getStatus());
        this.update(lambdaQueryWrapper);
        return ResultUtil.success("更新状态成功");
    }
}

