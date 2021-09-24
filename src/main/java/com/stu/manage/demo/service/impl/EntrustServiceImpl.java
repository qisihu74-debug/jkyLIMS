package com.stu.manage.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.stu.manage.demo.entity.CheckItemCostVo;
import com.stu.manage.demo.entity.CheckItemInfoVo;
import com.stu.manage.demo.entity.Company;
import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.EntrustStat;
import com.stu.manage.demo.entity.JtEntrustCheckInfo;
import com.stu.manage.demo.entity.JtEntrustCheckItem;
import com.stu.manage.demo.entity.JtEntrustInfo;
import com.stu.manage.demo.entity.JtSampleObject;
import com.stu.manage.demo.entity.ProductVo;
import com.stu.manage.demo.entity.SampleInfoVo;
import com.stu.manage.demo.entity.SampleStatus;
import com.stu.manage.demo.entity.StatusEntity;
import com.stu.manage.demo.mapper.EntrustMapper;
import com.stu.manage.demo.mapper.JtEntrustCheckInfoMapper;
import com.stu.manage.demo.mapper.JtEntrustCheckItemMapper;
import com.stu.manage.demo.mapper.JtEntrustInfoMapper;
import com.stu.manage.demo.mapper.JtSampleObjectMapper;
import com.stu.manage.demo.service.EntrustService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class EntrustServiceImpl implements EntrustService {
    @Autowired
    private EntrustMapper entrustMapper;
    @Autowired
    private JtEntrustInfoMapper jtEntrustInfoMapper;
    @Autowired
    private JtEntrustCheckInfoMapper jtEntrustCheckInfoMapper;
    @Autowired
    private JtSampleObjectMapper jtSampleObjectMapper;
    @Autowired
    private JtEntrustCheckItemMapper jtEntrustCheckItemMapper;

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

    @Override
    public Integer addEntrust(JtEntrustInfo jtEntrustInfo) {
        JtEntrustInfo jtEntrustInfos = new JtEntrustInfo();
        jtEntrustInfos.setEntrustNumber("2021090023");// (getEntrustNumber() 委托号
        // 委托单基本信息
        // TODO: 2021/6/15 委托单状态 0 未分任务 1分完任务
        jtEntrustInfos.setEntrustStatus(0);
//        jtEntrustInfoMapper.insertSelective(jtEntrustInfo);
        return null;
    }

    @Override
    public synchronized Integer addEntrustInfo(Map<String, Object> map) {

        Object entrustInfo = map.get("entrustInfo");
        Object sampleList = map.get("sampleList");
        Object entrustCheckInfo = map.get("entrustCheckInfo");
        JtEntrustInfo jtEntrustInfo= JSONObject.parseObject(JSON.toJSONString(entrustInfo),JtEntrustInfo.class);
        jtEntrustInfo.setEntrustNumber(getEntrustNumber());//getEntrustNumber()
        // TODO: 2021/6/15 委托单状态 0 未分任务 1分完任务
        jtEntrustInfo.setEntrustStatus(0);


        // 1、得到公司名称 进行查询 增加id 2、没有公司名称 新增进行 id 补充
        Company company = jtEntrustInfoMapper.selectCompanyName(jtEntrustInfo.getComName());
        if(company.getCount()!=0)
        {
            jtEntrustInfo.setEntrustCompanyId(company.getComId());
        }
        else {

            company.setComName(jtEntrustInfo.getComName());
            company.setComContactPerson(jtEntrustInfo.getEntrustPeople());
            company.setComContactPhone(jtEntrustInfo.getEntrustPeoplePhone());
            jtEntrustInfoMapper.insertCompany(company);
            jtEntrustInfo.setEntrustCompanyId(company.getComId());
        }
        // 客户委托信息 代码补充基本信息
        jtEntrustInfo.setContactAddress(company.getComAddress());
        jtEntrustInfo.setContactPeople(company.getComContactPerson());
        jtEntrustInfo.setContactTel(company.getComContactPhone());
        jtEntrustInfoMapper.insertSelective(jtEntrustInfo);
        System.out.println("存储委托基本信息"+jtEntrustInfo);
        // 后台补充委托基本价格信息
        JtEntrustCheckInfo jtEntrustCheckInfo = new JtEntrustCheckInfo();
        if(jtEntrustInfo.getEntrustId()!=null)
        {
            jtEntrustCheckInfo.setEntrustId(jtEntrustInfo.getEntrustId());
            jtEntrustCheckInfo.setSampleStatusDesc("待补充");
            jtEntrustCheckInfo.setSampleStay(1);
            jtEntrustCheckInfo.setCost("0");
            jtEntrustCheckInfo.setPayMode(2);
            jtEntrustCheckInfo.setPay("0");
            jtEntrustCheckInfo.setAcceptUserId(160);//受理人默认为 程萍 160；
            jtEntrustCheckInfo.setSource(1);
            jtEntrustCheckInfo.setAcceptTime(new Date());
            jtEntrustCheckInfo.setStandardCost("0");
            jtEntrustCheckInfoMapper.insertSelective(jtEntrustCheckInfo);//任务来源

        }
        System.out.println("存储委托价格信息默认值"+jtEntrustCheckInfo);
        // 增加样品基本信息
        JtSampleObject jtSampleObject= JSONObject.parseObject(JSON.toJSONString(sampleList),JtSampleObject.class);
        // 后台补充业务id
        jtSampleObject.setReceiveUserId(160);//受理人默认为 程萍 160；
        jtSampleObject.setReceiveTime(new Date());
        jtSampleObjectMapper.insertSelective(jtSampleObject);

        //解析 产品下的 数据
        for (JtEntrustCheckItem jtEntrustCheckItem:jtSampleObject.getCheckList()) {
            System.out.println("解析 产品下的 检测项数据 "+jtEntrustCheckItem);
            jtEntrustCheckItem.setProductId(jtSampleObject.getProductId());
            jtEntrustCheckItem.setSampleId(jtSampleObject.getSampleObjectId());
            jtEntrustCheckItemMapper.insertSelective(jtEntrustCheckItem);
        }






        return 1;
    }

    @Override
    public Map<String, List<ProductVo>> getSelectLists() {
        Map<String,List<ProductVo>> result = Maps.newHashMap();
        result.put("entrustWay",jtEntrustInfoMapper.getEntrustTheWay());
        result.put("checkPurpose",jtEntrustInfoMapper.getcheckPurpose());
        result.put("receiveWay",jtEntrustInfoMapper.getreceiveWay());
        result.put("reportGetWay",jtEntrustInfoMapper.getreportGetWay());
        return result;
    }

    /**
     * 自动生产委托编号
     */
    private String getEntrustNumber(){
        StringBuilder stringBuffer=new StringBuilder();
        Calendar calendar=Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        stringBuffer.append(year).append(month>9?month:"0"+month);
        int count=jtEntrustInfoMapper.selectEntrustNumberForCount(stringBuffer.toString());
        stringBuffer.append(String.format("%04d",count+1));
        return stringBuffer.toString();
    }
}
