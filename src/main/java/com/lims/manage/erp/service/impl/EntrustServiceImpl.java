package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestCustomerDao;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import com.lims.manage.erp.vo.EntrustAddVo;
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


    @Override
    public Boolean addEntrust(EntrustAddVo vo) {
        Boolean result = false;
        //存放委托基本信息
        EntrustEntity basisInfo = new EntrustEntity(vo);
        entityMapper.insert(basisInfo);
        //存放样品信息
        result = true;
        return result;
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
            if(testInitDataEntity.getType()==1){
                labelValueVo.setLabel(testInitDataEntity.getName());
                labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
                arryEntrust.add(labelValueVo);
            }
            if(testInitDataEntity.getType()==2){
                labelValueVo.setLabel(testInitDataEntity.getName());
                labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
                arrySampling.add(labelValueVo);
            }
            if(testInitDataEntity.getType()==3){
                labelValueVo.setLabel(testInitDataEntity.getName());
                labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
                arryCheckout.add(labelValueVo);
            }
            if(testInitDataEntity.getType()==4){
                labelValueVo.setLabel(testInitDataEntity.getName());
                labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
                arryGetReport.add(labelValueVo);
            }
            if(testInitDataEntity.getType()==5){
                labelValueVo.setLabel(testInitDataEntity.getName());
                labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
                arrySampleAppearance.add(labelValueVo);
            }
            if(testInitDataEntity.getType()==6){
                labelValueVo.setLabel(testInitDataEntity.getName());
                labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
                arrySeal.add(labelValueVo);
            }
            if(testInitDataEntity.getType()==11){
                labelValueVo.setLabel(testInitDataEntity.getName());
                labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
                arryPayment.add(labelValueVo);
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
}
