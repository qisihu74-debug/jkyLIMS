package com.lims.manage.erp.service.impl;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.HomeAfficheEntity;
import com.lims.manage.erp.mapper.HomeAfficheDao;
import com.lims.manage.erp.mapper.HomeMapper;
import com.lims.manage.erp.service.HomeService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private HomeAfficheDao homeAfficheDao;

    @Autowired
    private HomeMapper homeMapper;

    @Override
    public Map<String, Integer> taskStatistics() {
        Map<String, Integer> result = Maps.newHashMap();
        result.put("allTask", homeMapper.getAllTask());
        result.put("completeTask", homeMapper.getCompleteTask());
        result.put("incompleteTak", homeMapper.getIncompleteTask());
        result.put("outputValue", homeMapper.getOutputValue());
        return result;
    }

    @Override
    public List<LabelValueVo> outputValueStatistics(Integer flag) {
        List<String> dateList = Lists.newArrayList();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if(flag == 1){
            Calendar cal = Calendar.getInstance();

            //获取本周的周一日期
            Date date = getThisWeekMonday();
            cal.setTime(date);
            int day = 0;
            for (int i = 0; i <7; i++) {
                day = cal.get(Calendar.DATE);
                if(i==0){
                    cal.set(Calendar.DATE, day + i);
                }else{
                    cal.set(Calendar.DATE, day + 1);
                }
                String dayAfter = dateFormat.format(cal.getTime());
                dateList.add(dayAfter);
            }
        }else if(flag == 2){
            Date now = new Date();
            String date = dateFormat.format(now);
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = 1;// 所有月份从1号开始
            Calendar cal = Calendar.getInstance();// 获得当前日期对象
            cal.clear();// 清除信息
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);// 1月从0开始
            cal.set(Calendar.DAY_OF_MONTH, day);
            int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int j = 0; j <= (count - 1); ) {
                if (dateFormat.format(cal.getTime()).equals(getLastDay(year, month)))
                    break;
                cal.add(Calendar.DAY_OF_MONTH, j == 0 ? +0 : +1);
                j++;
                dateList.add(dateFormat.format(cal.getTime()));
            }
        }
        List<LabelValueVo> result = Lists.newArrayList();
        List<LabelValueVo> temp = homeMapper.outputValueStatistics(dateList.get(0), dateList.get(dateList.size()-1));
        for (String s : dateList) {
            LabelValueVo vo = null;
            for (LabelValueVo labelValueVo : temp) {
                if (s.equals(labelValueVo.getLabel())) {
                    vo = new LabelValueVo(s,labelValueVo.getValue());
                }
            }
            if(vo == null){
                vo = new LabelValueVo(s,0L);
            }
            result.add(vo);
        }
        return result;
    }

    public Date getThisWeekMonday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    public String getLastDay(int year, int month) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return sdf.format(cal.getTime());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postAnnounce(HomeAfficheEntity homeAfficheEntity, MultipartFile[] file) {
        HomeAfficheEntity data = homeAfficheEntity;
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.buckets_affiche_template, multipartFile, GenID.getID() + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（委托文档资料.pdf,原始文档.docx）
                stringfileUrlStr.append(name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                data.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                data.setFileUrlName(substring);
            }
        }
        data.setCreateTime(new Date());
        data.setUpdateTime(new Date());
        data.setId(GenID.getID());
        homeAfficheDao.addHomeAffiche(data);
        return true;
    }

    @Override
    public List<HomeAfficheEntity> showAnnounce() {
        List<HomeAfficheEntity> data = homeAfficheDao.announceHistory();
        return data;
    }
}
