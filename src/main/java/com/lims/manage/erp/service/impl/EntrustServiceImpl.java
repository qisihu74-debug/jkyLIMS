package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.entity.TestSampleJsonEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ProductItemEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestCustomerDao;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EntrustServiceImpl implements EntrustService {

    @Autowired
    private EntrustEntityMapper entityMapper;
    @Autowired
    TestCompanyDao testCompanyDao;
    @Autowired
    TestCustomerDao testCustomerDao;
    @Autowired
    SampleEntityMapper sampleEntityMapper;
    @Autowired
    TestProductDao testProductDao;

    @Autowired
    private ProductItemEntityMapper itemEntityMapper;
    /**
     * 新增委托任务
     * @param vo
     * @return
     */
    @Override
    public Boolean addEntrust(EntrustAddVo vo) {
        Boolean result = false;
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        entityMapper.insert(basisInfo);
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件

        //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel，上传附件

        //存放委托单样品，使用依据信息test_entrusted_sample_standard_file_rel

        //更新委托单收费记录信息

        result = true;
        return result;
    }

    @Override
    public List<CheckItemDetailVo> getAllItemByProductId(Integer productId) {
        return itemEntityMapper.getAllItemByProductId(productId);
    }

    @Override
    public List<CheckItemInfoVo> getCheckItemInfoVo(List<Integer> ids) {
        return itemEntityMapper.getItemInfo(ids);
    }


    @Override
    public  Map<String,List<LabelValueVo>> returnEntrustData() {

        Map<String,List<LabelValueVo>> map = new HashMap<>();
        // type =1 委托单位
       List<LabelValueVo> EntrustCompany = testCompanyDao.selectEntrustCompanyList(1);
        // type =2 见证单位
        List<LabelValueVo> witnessCompany = testCompanyDao.selectEntrustCompanyList(2);
        List<TestInitDataEntity> ReturnBasisData  = testCompanyDao.selectEntrustBasis();
        // 团队信息
        List<LabelValueVo> arryTeam = testCompanyDao.selectTestTeam();
//        1 = 委托方式： 2=取样方式 ： 3=检测目的：4 ： 取报告方式 5：样品外观  6：签章类型： 7：用户来源： 8：设备类型  9：支付方式
        // 委托方式 type =1
        List<LabelValueVo> arryEntrust = new ArrayList<>();
        // 取样方式 type =2
        List<LabelValueVo> arrySampling = new ArrayList<>();
        // 3=检测目的
        List<LabelValueVo> arryCheckout = new ArrayList<>();
        // 4=取报告方式
        List<LabelValueVo> arryGetReport = new ArrayList<>();
        // 5：样品外观
        List<LabelValueVo> arrySampleAppearance = new ArrayList<>();
        // 6：签章类型：
        List<LabelValueVo> arrySeal = new ArrayList<>();
        // 8：设备类型
        List<LabelValueVo> arryEquipment = new ArrayList<>();
        // 11：支付方式
        List<LabelValueVo> arryPayment = new ArrayList<>();
        for(TestInitDataEntity testInitDataEntity:ReturnBasisData){
            LabelValueVo labelValueVo = new LabelValueVo();
            labelValueVo.setLabel(testInitDataEntity.getName());
            labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
            switch (testInitDataEntity.getType()) {
                case 1:  arryEntrust.add(labelValueVo); break;
                case 2:   arrySampling.add(labelValueVo); break;
                case 3:   arryCheckout.add(labelValueVo); break;
                case 4:   arryGetReport.add(labelValueVo); break;
                case 5:   arrySampleAppearance.add(labelValueVo); break;
                case 6:   arrySeal.add(labelValueVo); break;
                case 11:   arryPayment.add(labelValueVo); break;
                default:break;
            }
        }
        map.put("entrustCompany",EntrustCompany);
        map.put("witnessCompany",witnessCompany);
        map.put("arryEntrust",arryEntrust);
        map.put("arrySampling",arrySampling);
        map.put("arryCheckout",arryCheckout);
        map.put("arryGetReport",arryGetReport);
        map.put("arrySampleAppearance",arrySampleAppearance);
        map.put("arrySeal",arrySeal);
        map.put("arryTeam",arryTeam);
        map.put("arryPayment",arryPayment);
        return map;
    }

    @Override
    public List<TestCustomerJsonEntity> returnTestCustomerEntityList(Integer companyId) {

        return testCompanyDao.selectPeopleInformation(companyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCompanyData(TestCompanyJsonEntity testCompanyEntity) {
        TestCompanyEntity testCompanyEntity1 = new TestCompanyEntity();
        testCompanyEntity1.setCompanyName(testCompanyEntity.getCompanyName());
        testCompanyEntity1.setType(testCompanyEntity.getType());
        int statusNumber = testCompanyDao.insert(testCompanyEntity1);
        if(statusNumber>=1){
            TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
            testCustomerEntity.setCompanyId(testCompanyEntity1.getCompanyId());
            testCustomerEntity.setContacts(testCompanyEntity.getContacts());
            testCustomerEntity.setPhone(testCompanyEntity.getContactWay());
           int addCustomer = testCustomerDao.insertTestCustomer(testCustomerEntity);
           if(addCustomer>=1){
               return true;
           }
            return false;
        }
        return false;
    }

    @Override
    public List<SampleEntity> getSampleDataList(SampleEntity sampleEntity) {

        return sampleEntityMapper.selectSampleList(sampleEntity);
    }

    @Override
    public List<LabelValueVo> selectProductList(String productName) {

        return testProductDao.selectProductList(productName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean returnAddSampleData(TestSampleJsonEntity testSampleJsonEntity) {
        // 获取样品编号  sampleGroups * quantityPerGroup

        if(testSampleJsonEntity.getSampleGroups()!=null&&testSampleJsonEntity.getQuantityPerGroup()!=null){
            // 样品组数
            int number1 = testSampleJsonEntity.getSampleGroups();
            // 每组样品数
            int number2 = testSampleJsonEntity.getQuantityPerGroup();
            int count = number1 * number2;
            // 方法1：根据年份时间 返回 表最大值 = 0082

            // 根据 组号 * 每组样品数 做下标数据。
            // 生成样品编号
            List<String> numberCollection = new ArrayList<>();
            String NumberStr = "YP-2021-";
            if(number1 > 1){
                for(int i =1;number1>=i;i++){
                    for(int j=1;number2>=j;j++){
                        numberCollection.add(NumberStr+"008"+i+"-0"+j);
                    }
                }
            }
            System.out.println(numberCollection);

        }
        return null;
    }
}
