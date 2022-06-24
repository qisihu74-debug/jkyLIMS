package com.lims.manage.erp.service.impl;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;

import com.lims.manage.erp.controller.UserFuctionController;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.service.HomeService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.ReportApprovalVo;
import com.lims.manage.erp.vo.ReportListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private HomeAfficheDao homeAfficheDao;

    @Autowired
    private HomeMapper homeMapper;
    @Autowired
    UserFuctionController userFuctionController;
    // 委托单调用。
//    @Autowired
//    EntrustController entrustController;
    @Autowired
    EntrustEntityMapper entrustEntityMapper;
    //    任务单调用
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    TestSampleEntityMapper testSampleEntityMapper;
//    // 报告合成 / 盖章 / 邮寄
    @Autowired
    ReportMapper reportMapper;
    //    // 报告审批
    @Autowired
    ReportApprovalMapper reportApprovalMapper;
    // 报告
    @Autowired
    ReportRecordEntityMapper reportRecordEntityMapper;
    @Autowired
    private SysRoleFuncMenuDao sysRoleFuncMenuDao;
    @Autowired
    private EntrustServiceImpl entrustServiceImpl;


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
        if (flag == 1) {
            Calendar cal = Calendar.getInstance();

            //获取本周的周一日期
            Date date = getThisWeekMonday();
            cal.setTime(date);
            int day = 0;
            for (int i = 0; i < 7; i++) {
                day = cal.get(Calendar.DATE);
                if (i == 0) {
                    cal.set(Calendar.DATE, day + i);
                } else {
                    cal.set(Calendar.DATE, day + 1);
                }
                String dayAfter = dateFormat.format(cal.getTime());
                dateList.add(dayAfter);
            }
        } else if (flag == 2) {
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
        } else if (flag == 3) {
            Calendar cal = Calendar.getInstance();// 获得当前日期对象
            cal.setTime(new Date(System.currentTimeMillis()));
            int year = cal.get(Calendar.YEAR);
            for (int i = 1; i <= 12; i++) {
                if (i < 10) {
                    dateList.add(year + "-0" + i);
                } else {
                    dateList.add(year + "-" + i);
                }
            }
        }
        List<LabelValueVo> result = Lists.newArrayList();
        List<LabelValueVo> temp;
        if (flag == 3) {
            temp = homeMapper.outputValueStatisticsYear(dateList.get(0) + "-01", dateList.get(dateList.size() - 1) + "-31");
        } else {
            temp = homeMapper.outputValueStatistics(dateList.get(0), dateList.get(dateList.size() - 1));
        }
        for (String s : dateList) {
            LabelValueVo vo = null;
            for (LabelValueVo labelValueVo : temp) {
                if (s.equals(labelValueVo.getLabel())) {
                    vo = new LabelValueVo(s, labelValueVo.getValue());
                }
            }
            if (vo == null) {
                vo = new LabelValueVo(s, 0L);
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

    @Override
    public List<LabelValueVo> taskKanban(Long userId) {
        // 根据人员id 返回团队id集合
        List<Long> deptIds = teamMapper.getUserTeamIds(userId);
        // 输出最终拥有的菜单
        List<LabelValueVo> returnData = new ArrayList<>();
        // 获取 当前登录展示菜单。
        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userId);
        if (menuIdList.isEmpty()) {
            return null;
        }
        // 设置任务看板 菜单名
        String[] strings = Const.taskKanbans;
        // 获取菜单成功！
        methodRenurnData(menuIdList, strings, returnData);
        if (returnData != null) {
            // 统计看板数据。
            methodTaskKanbanData(deptIds, returnData);
        }
        return returnData;
    }

    /**
     * 通过任务看板模块栏 比对 当前用户拥有菜单列表。
     *
     * @param menuIdList      任务看板模块栏
     * @param strings    当前用户拥有菜单列表
     * @param returnData 输出最终拥有的菜单。
     */
    void methodRenurnData(List<SysRoleFunctionParent> menuIdList, String[] strings, List<LabelValueVo> returnData) {
        // 针对菜单名 进行模块划分。
        // 整理 菜单属性图 为List排序。
        // 遍历获取 菜单名。
        for (int i = 0; i < strings.length; i++) {
            LabelValueVo labelValueVo = new LabelValueVo();
            for (SysRoleFunctionParent data : menuIdList) {
                if (data.getTreeName()!=null&&data.getTreeName().equals(strings[i])) {
                    labelValueVo.setLabel(data.getTreeName());
                    labelValueVo.setText(data.getKanbanName());
                    returnData.add(labelValueVo);
                }
            }
        }
    }



    /**
     * 统计看板上各模块数据。
     *
     * @param deptIds    所属部门集合
     * @param returnData 用户拥有菜单数据
     */
    void methodTaskKanbanData(List<Long> deptIds, List<LabelValueVo> returnData) {
        // 统计样品已检
//        Object MethodData = entrustServiceImpl.setSampleList();
        List<TestSampleEntity> sampleList = testSampleEntityMapper.selectStateCollection("0");
//        entrustServiceImpl.setSampleList();
        // 统计未分配委托单
        Integer entrustCount = entrustEntityMapper.selectCount(0);
        if(deptIds.isEmpty()){
            deptIds = null;
        }
        // 未任务领取。
        Integer taskCount = taskMapper.selectCount(0, deptIds);
        // 试验检测中
        Integer testCount = taskMapper.selectCount(3, deptIds);
        //报告合成
        List<ReportListVo> reportList = reportMapper.reportDownloadList(deptIds, null);
        // ArrList 转set
        Set<Long> setUserId  = new ReportApprovalServiceImpl().getNextIdsToTeam();
        // 报告审核
        List<ReportApprovalVo> approvalList = reportApprovalMapper.getReportApprovalList(null, setUserId,null);
        // 报告签发
        List<ReportApprovalVo> verifyList = reportApprovalMapper.getVerifyList(null, setUserId,null);
        // 报告盖章
        List<ReportRecordEntity> sealList = reportRecordEntityMapper.getSealList(null, "1", "1",null,null);
        // 报告邮寄
        Integer toBeA = reportRecordEntityMapper.selectCount(7);
        // 循环 输出赋值。
        for (LabelValueVo data : returnData) {
            switch (data.getLabel()) {
//                case Const.sampleStr:
//                    data.setValue((long) sampleList.size());
//                    break;
                case Const.entrustStr:
                    data.setValue(entrustCount.longValue());
                    break;
                case Const.taskStr:
                    data.setValue(taskCount.longValue());
                    break;
                case Const.testStr:
                    data.setValue(testCount.longValue());
                    break;
                case Const.reportStr:
                    data.setValue((long) reportList.size());
                    break;
                case Const.approvalStr:
                    data.setValue((long) approvalList.size());
                    break;
                case Const.verifyStr:
                    data.setValue((long) verifyList.size());
                    break;
                case Const.sealStr:
                    data.setValue((long) sealList.size());
                    break;
                case Const.toBeAStr:
                    data.setValue(toBeA.longValue());
                    break;
                default:
                    break;
            }
        }
    }
}
