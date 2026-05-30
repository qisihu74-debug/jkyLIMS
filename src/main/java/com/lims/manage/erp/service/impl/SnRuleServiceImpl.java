package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.SnRecord;
import com.lims.manage.erp.entity.SnRule;
import com.lims.manage.erp.mapper.SnEntityMapper;
import com.lims.manage.erp.mapper.SnRuleDao;
import com.lims.manage.erp.service.SnRuleService;
import com.lims.manage.erp.util.GenID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-06-11 16:33
 * @Copyright © 河南交科院
 */
@Service
public class SnRuleServiceImpl implements SnRuleService {
    @Autowired
    private SnRuleDao snRuleDao;
    @Autowired
    private SnEntityMapper snEntityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized String getSnByType(String type,String code) {
        String s = "";
        //根据类型获取编号sys_serial_number_record
        SnRecord snRecord = snRuleDao.getInfoByType(type);
        if (snRecord != null){
            if (snRecord.getStatus() == 0){
                //获取sn、根据编号定义规则最后规则序号生成下一个编号
                String sn = snRecord.getSn();
                //判断需要延续的编号是否和当前日期相同、处理相应的操作
                List<SnRule> list = snRuleDao.getLastNumberByType(type);//sys_serial_number_rule
                String[] split = sn.split("-");
                //任务单标识处理
                String originalString = split[0];
                int startIndex = originalString.length() - 4;
                split[0]=originalString.substring(startIndex);
                boolean flag = false;
                int index = 1;
                if ("任务编号".equals(type)){
                    index = 2;
                }
                for (int i =0;i<list.size();i++) {
                    if (list.get(i).getSerialNumberType().equals("年份")){
                        int year = Calendar.getInstance().get(Calendar.YEAR);
                        String yy = split[list.get(i).getSort()-index];
                        if (!(Integer.parseInt(yy) == year)){
                            split[list.get(i).getSort()-index] = year+"";
                            flag = true;
                        }
                    }
                    if (list.get(i).getSerialNumberType().equals("月份")){
                        String mm = "";
                        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                        if ((month+"").length()<2){
                            mm = "0"+month;
                        }else {
                            mm = month+"";
                        }
                        String m = split[list.get(i).getSort()-index];
                        if (!m.equals(mm)){
                            split[list.get(i).getSort()-index] = mm;
                            flag = true;
                        }
                    }
                    if (list.get(i).getSerialNumberType().equals("日期")){
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd");
                        String currentDay = sdf.format(date);
                        String dd = split[list.get(i).getSort()-index];
                        if (!currentDay.equals(dd)){
                            split[list.get(i).getSort()-index] = currentDay;
                            flag = true;
                        }
                    }
                }
                if (flag){
                    int length = split.length;
                    String lastNum = split[length - 1];
                    String str = "";
                    for (int j=0;j<lastNum.length();j++) {
                        if (j == lastNum.length()-1){
                            str = str+"1";
                        }else {
                            str = str+"0";
                        }
                    }
                    split[length - 1] = str;
                }
                sn = Arrays.stream(split)
                        .collect(Collectors.joining("-"));
                String substring = sn.substring(0, sn.length() - list.get(0).getSerialNumberContent().length());
                String s1 = sn.substring(substring.length(), sn.length());
                int parseInt = Integer.parseInt(s1);
                String s2 = parseInt+"";
                if (s2.length()==s1.length()){
                    s = code+substring+(parseInt+1);
                }else {
                    //补位
                    int num = s1.length() - ((parseInt+1)+"").length();
                    if (num != 0){
                        String s3 = "0";
                        for (int i =1;i<num;i++){
                            s3=s3+"0";
                        }
                        s = code+substring+s3+(parseInt+1);
                    }else {
                        s = code+substring+(parseInt+1);
                    }
                }
            }else {
                //根据编号定义规则生成新编号sys_serial_number
                s  = snRuleDao.getSnByType(type);
                //生成初始记录后改回状态
                snEntityMapper.updateResetByTypeAndTenantId(type,0);
            }
            String ddyy = handlerDDYY(type, s);
            s =ddyy;
            //更新编号最新记录
            snRuleDao.updateSnById(snRecord.getId(),s);
        }else {
            s  = snRuleDao.getSnByType(type);
            String ddyy = handlerDDYY(type, s);
            SnRecord snRecord1 = new SnRecord();
            snRecord1.setId(GenID.getID());
            snRecord1.setStatus(0);
            if ("任务编号".equals(type)){
                ddyy = code+ddyy;
            }
            snRecord1.setSn(ddyy);
            snRecord1.setType(type);
            snRuleDao.insertSn(snRecord1);
            s=snRecord1.getSn();
        }
        return s;
    }

    /**
     * 处理年月份
     * @param sn
     * @return
     */
    public String handlerDDYY(String type,String sn){
        String result = "";
        Long id = snRuleDao.getIdByType(type);
        List<String> list = snRuleDao.getMessageById(id);
        if (list.contains("年份")){
            int year = Calendar.getInstance().get(Calendar.YEAR);
            result = sn.replace("年份", year+"");
        }
        if (list.contains("月份")){
            String mm = "";
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            if ((month+"").length()<2){
                mm = "0"+month;
            }else {
                mm = month+"";
            }
            if (StringUtils.isNotEmpty(result)){
                result = result.replace("月份", mm+"");
            }else {
                result = sn.replace("月份", mm+"");
            }
        }
        if (list.contains("日期")){
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd");
            String currentDay = sdf.format(date);
            if (StringUtils.isNotEmpty(result)){
                result = result.replace("日期", currentDay+"");
            }else {
                result = sn.replace("日期", currentDay+"");
            }
        }
        if (StringUtils.isNotEmpty(result)){
            return result;
        }else {
            return sn;
        }
    }
}
