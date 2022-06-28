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
import org.springframework.util.CollectionUtils;
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
        // 查看团队顶级部门下所属团队集合
        List<Long> deptIds = new ArrayList<>();
        // 是否有无所属科室 （验证团队与检测项是否存在）
        boolean isDepartment = false;
        // 获取当前用户所在科室id
        Long department = teamMapper.getTeamIdByUid(userId);
        if(!StringUtils.isEmpty(department)){
            // 获取顶级部门 为空则是当前部门
            Long topDepartment = teamMapper.getTopDepartment(department);
            if(StringUtils.isEmpty(topDepartment)){
                topDepartment = department;
            }
            // 通过顶级部门 查看团队顶级部门下所属团队集合
            List<TeamTreeStructureEntity> deptList = teamMapper.getChirds(topDepartment);
            for(int i=0; i<deptList.size();i++){
                TeamTreeStructureEntity teamTreeStructureEntity  = deptList.get(i);
                deptIds.add(teamTreeStructureEntity.getId());
            }
            // 该团队存在团队检测项 ： 检测项 与所属部门验证。 存在 或不存在。
            List<TestCheckItemTeamRel> checkItemList = teamMapper.getDepartmentList(deptIds);
            if(!CollectionUtils.isEmpty(checkItemList))
            {
                // 检测项 与所属部门验证。 存在
                isDepartment = true;
            }
        }

        // 个人拥有的菜单
        List<LabelValueVo> personalMenu = new ArrayList<>();
        // 获取 当前登录展示菜单。
        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userId);
        if (menuIdList.isEmpty()) {
            return null;
        }
        // 设置任务看板 菜单名
        String[] strings = Const.taskKanbans;
        // 获取菜单成功！
        methodRenurnData(menuIdList, strings, personalMenu);
        if (personalMenu != null) {
            // 统计看板数据。
            methodTaskKanbanData(deptIds, personalMenu,isDepartment,department);
        }
        return personalMenu;
    }

    /**
     * 通过任务看板模块栏 比对 当前用户拥有菜单列表。
     *
     * @param  strings    任务看板模块栏
     * @param  menuIdList   当前用户拥有菜单列表
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
     * @param isDepartment 是否有无所属科室  （验证团队与检测项是否存在） ：true = 存在 false = B不存在
     * @param department 获取当前用户所在科室id
     */
    void methodTaskKanbanData(List<Long> deptIds, List<LabelValueVo> returnData,Boolean isDepartment,Long department ) {
        /**
         *  一：1、无所属科室 （查询时全部的）
         * 	二： 1、所属团队 ： 部门信息唯一。
         * 	2、该团队存在团队检测项 ： 检测项 与所属部门验证。 存在 或不存在。
         * 	3、查看团队顶级部门下所属团队集合
         *
         */
        // 所属团队 ： 部门信息唯一。
        List<Long> departmentIds = new ArrayList<>();
        departmentIds.add(department);
        // 获取团队下 人员id集合。
        Set<Long> listUser = new HashSet<>();
        Set<Long> SetDeptIds = new HashSet<>();
        SetDeptIds.add(department);
        // 根据团队id 获取人员id集合
        List<LabelValueVo> teamVos0 = taskMapper.getMemberInformation(SetDeptIds);
        if(!CollectionUtils.isEmpty(teamVos0)){
            for(LabelValueVo labelValueVo:teamVos0){
                listUser.add(labelValueVo.getValue());
            }
        }
        else{
            listUser = null;
        }
        // 统计未分配委托单
        Integer entrustCount = 0;
        // 未任务领取。
        Integer taskCount = 0;
        // 试验检测中
        Integer testCount =0;
        //报告合成
        Integer reportInt = 0;
        // 报告审核
        Integer approvalInt = 0;
        // 报告签发
        Integer verifyInt = 0;
        // 报告邮寄
        Integer toBeA = 0;
        // 待盖章：
        Integer sealInt = 0;
        /**
         * deptIds = null
         * 账号无所属科室 = 查看的待发出信息是全部的。
         */
        if(CollectionUtils.isEmpty(deptIds)) {
            entrustCount  = entrustEntityMapper.selectCountUnallocated(0,null);
            taskCount = taskMapper.selectCount(0, null);
            testCount = taskMapper.selectCount(3, null);
            List<ReportListVo> reportList = reportMapper.reportDownloadList(null, null);
            if(CollectionUtils.isEmpty(reportList)){
                reportInt = 0;
            }else{
                reportInt = reportList.size();
            }
            // 报告审核
            List<ReportApprovalVo> approvalList = reportApprovalMapper.getReportApprovalList(null, null,null);
            if(CollectionUtils.isEmpty(approvalList)){
                approvalInt = 0;
            }else {
                approvalInt = approvalList.size();
            }
            // 报告签发
            List<ReportApprovalVo> verifyList = reportApprovalMapper.getVerifyList(null, null,null);
            if(CollectionUtils.isEmpty(verifyList)){
                verifyInt = 0;
            }else {
                verifyInt = verifyList.size();
            }
            // 待发出报告
            List<ReportRecordEntity> sendList = reportRecordEntityMapper.getSendListCount("1", null);
            if(CollectionUtils.isEmpty(sendList)){
                toBeA = 0;
            }
            else {
                toBeA = sendList.size();
            }
            // 待盖章：
            List<ReportRecordEntity> sealList  = reportRecordEntityMapper.getSealListCount(null);
            if(CollectionUtils.isEmpty(sealList)){
                sealInt = 0;
            }
            else {
                 sealInt = sealList.size();
            }

        } else {
            taskCount = taskMapper.selectCount(0, departmentIds);
            testCount = taskMapper.selectCount(3, departmentIds);
            List<ReportListVo> reportList = reportMapper.reportDownloadList(departmentIds, null);
            if(CollectionUtils.isEmpty(reportList)){
                reportInt = 0;
            }
            else {
                reportInt = reportList.size();
            }
            // 报告审核
            List<ReportApprovalVo> approvalList = reportApprovalMapper.getReportApprovalList(null, listUser,null);
            if(CollectionUtils.isEmpty(approvalList)){
                approvalInt = 0;
            } else{
                approvalInt = approvalList.size();
            }
            // 报告签发
            List<ReportApprovalVo> verifyList = reportApprovalMapper.getVerifyList(null, listUser,null);
            if(CollectionUtils.isEmpty(verifyList)){
                verifyInt = 0;
            }else {
                verifyInt = verifyList.size();
            }
            // 待发出报告
            List<ReportRecordEntity> sendList = reportRecordEntityMapper.getSendListCount("1", departmentIds);
            if(CollectionUtils.isEmpty(sendList)){
                toBeA = 0;
            }
            else {
                toBeA = sendList.size();
            }
            // 待盖章：
            List<ReportRecordEntity> sealList  = reportRecordEntityMapper.getSealListCount(departmentIds);
            if(CollectionUtils.isEmpty(sealList)){
                sealInt = 0;
            }
            else {
                sealInt = sealList.size();
            }
            /**
             * 有所属团队 并且 该团队存在团队检测项
             */
            if(isDepartment==true){
                entrustCount  = entrustEntityMapper.selectCountUnallocated(0,null);
            }
            else {
                entrustCount = entrustEntityMapper.selectCountUnallocated(0,deptIds);
            }
        }
        // 循环 输出赋值。
        for (LabelValueVo data : returnData) {
            switch (data.getLabel()) {
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
                    data.setValue((long) reportInt);
                    break;
                case Const.approvalStr:
                    data.setValue((long) approvalInt);
                    break;
                case Const.verifyStr:
                    data.setValue((long) verifyInt);
                    break;
                case Const.sealStr:
                    data.setValue((long) sealInt);
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
