package com.lims.manage.erp.service.impl;

import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.mapper.ReserveCodeEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.service.ReserveCodeService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.StringUtils;
import com.lims.manage.erp.vo.LabelValueVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ReserveCodeServiceImpl implements ReserveCodeService {
    @Autowired
    private ReserveCodeEntityMapper reserveCodeEntityMapper;
    @Autowired
    private ReportService reportService;
    @Autowired
    private LogManagerService logManagerService;

    @Override
    public Result addReserveCode(ReserveCodeEntity reserveCodeEntity) {
        if (reserveCodeEntity.getEntrustmentNo() == null) {
            return ResultUtil.error("委托单号不能为空！");
        }
        String reportCode = reserveCodeEntity.getReportCode();
        if (reportCode != null) {
            int codeSize = reserveCodeEntityMapper.getCodeSize(reportCode);
            if (codeSize != 0) {
                return ResultUtil.error("预留编号已存在，请重新设置！");
            }
        }
        reserveCodeEntity.setId(GenID.getID());
        reserveCodeEntity.setCreateDate(new Date());
        reserveCodeEntity.setState("未使用");
        reserveCodeEntityMapper.insert(reserveCodeEntity);
        return ResultUtil.success("预留编号成功！",null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result del(List<Long> idList) {
        reserveCodeEntityMapper.batchDelete(idList);
        return ResultUtil.success("删除成功！",null);
    }

    @Override
    public Result delete(Long id) {
        ReserveCodeEntity entity = reserveCodeEntityMapper.selectByPrimaryKey(id);
        if("已使用".equals(entity.getState())){
            return ResultUtil.error("预留编号已被使用，无法删除！");
        }
        reserveCodeEntityMapper.deleteByPrimaryKey(id);
        return ResultUtil.success("删除成功！",null);
    }

    @Override
    public Result updateReserveCode(ReserveCodeEntity reserveCodeEntity) {
        if(reserveCodeEntity.getId() == null){
            return ResultUtil.error("请选择要编辑的预留编号！");
        }
        ReserveCodeEntity entity = reserveCodeEntityMapper.selectByPrimaryKey(reserveCodeEntity.getId());
        if("已使用".equals(entity.getState())){
            return ResultUtil.error("预留编号已被使用，无法编辑！");
        }
        String reportCode = reserveCodeEntity.getReportCode();
        if(reportCode != null){
            int codeSize = reserveCodeEntityMapper.getCodeSize(reportCode);
            if(codeSize != 0){
                return ResultUtil.error("预留编号已存在，请重新设置！");
            }
        }
        reserveCodeEntityMapper.updateReserveCode(reserveCodeEntity);
        return ResultUtil.success("编辑成功！",null);
    }

    @Override
    public Result list(ReserveCodeEntity reserveCodeEntity) {
        if(reserveCodeEntity.getPageNum() == null || reserveCodeEntity.getPageSize() == null){
            return ResultUtil.error("缺少分页参数！");
        }
        PageHelper.startPage(reserveCodeEntity.getPageNum(),reserveCodeEntity.getPageSize());
        List<ReserveCodeEntity> list = reserveCodeEntityMapper.getList(reserveCodeEntity);
        PageInfo<ReserveCodeEntity> pageInfo = new PageInfo<>(list);
        return ResultUtil.success(pageInfo);
    }

    @Override
    public Result getEntrustmentNoList(String entrustmentNo) {
        List<LabelValueVo> entrustmentNoList = reserveCodeEntityMapper.getEntrustmentNoList(entrustmentNo);
        return ResultUtil.success(entrustmentNoList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result importEquipments(MultipartFile file) {
        List<ReserveCodeEntity> allList = Lists.newArrayList();
        List<ReserveCodeEntity> insertList = Lists.newArrayList();
        List<ReserveCodeEntity> noInsertList = Lists.newArrayList();
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = null;
            try {
                workbook = new Workbook(inputStream);
            } catch (Exception e) {
                log.error("加载文件失败:{}",e);
            }
            Worksheet worksheet = workbook.getWorksheets().get(0);
            Cells cells = worksheet.getCells();
            int maxRow = worksheet.getCells().getMaxRow();
            int index = 2;
            for (int i = 0; i < maxRow; i++) {
                String entrustmentNo = cells.get("A"+index).getValue().toString().trim();
                String reportCode = cells.get("B"+index).getValue().toString().trim();//预留编号
                String remark = null;
                if(cells.get("C"+index).getValue() != null){
                    remark =cells.get("C"+index).getValue().toString();//备注
                }
//                System.out.println(entrustmentNo + "*----*" + reportCode + "*----*" + remark);
                ReserveCodeEntity entity = new ReserveCodeEntity();
//                entity.setId(GenID.getID());
                entity.setEntrustmentNo(Integer.parseInt(entrustmentNo));
                entity.setReportCode(reportCode);
                entity.setType("报告编号");
                entity.setState("未使用");
                entity.setCreateDate(new Date());
                entity.setRemark(remark);
                allList.add(entity);
                index ++;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        if (!CollectionUtils.isEmpty(allList)) {
            for (int i = 0; i < allList.size(); i++) {
                ReserveCodeEntity entity = allList.get(i);
                int codeSize = reserveCodeEntityMapper.getCodeSize(entity.getReportCode());
                if (codeSize != 0) {//存在的不插入
                    noInsertList.add(entity);
                } else {//不存在的插入
                    insertList.add(entity);
                }
            }
            if (!CollectionUtils.isEmpty(noInsertList)) {
                return ResultUtil.error("下方编号已存在，请重新编辑后再导入！", noInsertList);
            }
        }
        if (!CollectionUtils.isEmpty(insertList)) {
            reserveCodeEntityMapper.batchInsert(insertList);
            return ResultUtil.success("预留编号成功！", insertList);
        } else {
            return ResultUtil.error("预留编号失败！");
        }
    }

    @Override
    public Result getMaxReportCode() {
        return ResultUtil.success("查询最大编号成功！", reportService.getMaxByUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result alternateReportNumber(String oldReportNumber, String newReportNumber) {
        if (StringUtils.isEmpty(oldReportNumber)) {
            return ResultUtil.error("操作失败：旧报告号不能为空");
        }
        if (StringUtils.isEmpty(newReportNumber)) {
            return ResultUtil.error("操作失败：报告号不能为空");
        }
        // 效验旧报告单号信息：旧报告号 在其中 = test_report_record、test_report_record_mid、test_reserve_code（state = 已废弃，备注描述信息已废弃）。
        ReportRecordEntity oldReportMark1 = reserveCodeEntityMapper.selectReportRecord(oldReportNumber);
        ReportRecordEntity oldReportMark2 = reserveCodeEntityMapper.selectReportRecordMid(oldReportNumber);
        if (oldReportMark1 == null && oldReportMark2 == null) {
            return ResultUtil.error("操作失败：旧报告号不存在");
        }
        // 新单号 不在其中 在其中 = test_report_record、test_report_record_mid、test_reserve_code在的话，需要清除。
        ReportRecordEntity newReportMark1 = reserveCodeEntityMapper.selectReportRecord(newReportNumber);
        ReportRecordEntity newReportMark2 = reserveCodeEntityMapper.selectReportRecordMid(newReportNumber);
        if (newReportMark1 != null || newReportMark2 != null) {
            return ResultUtil.error("操作失败：报告号已存在");
        }
        // 报告号 不得大于当前JC所-年-报告号 最大范围10以内。
        String[] arrays = newReportNumber.split("-");
        // 当前系统报告最大号
        int reportMaxCode = 0;
        Integer finalReportMaxCode = reserveCodeEntityMapper.getfinalReportOtherMaxCode(arrays[1], arrays[0]);
        Integer midReportMaxCode = reserveCodeEntityMapper.getMidReportOtherMaxCode(arrays[1], arrays[0]);
        if (finalReportMaxCode == null && midReportMaxCode == null) {
            return ResultUtil.error("操作失败： " + "报告号" + newReportNumber + " 在系统中不存在 " + arrays[1] + "年" + "报告号");
        }
        if (midReportMaxCode == null) midReportMaxCode = 0;
        // 比较报告最大值进行返回
        if (finalReportMaxCode.compareTo(midReportMaxCode) > 0) {
            reportMaxCode = finalReportMaxCode;
        } else {
            reportMaxCode = midReportMaxCode;
        }
        // 当前变更的报告单号
        int nowRportNumber = Integer.parseInt(arrays[arrays.length - 1]);
        if (nowRportNumber > reportMaxCode) {
            // 进行 A%B 求余操作
            int zhi = nowRportNumber % reportMaxCode;
            if (zhi > 10) {
                return ResultUtil.error("操作失败：报告号 " + nowRportNumber + " 超过当前最大数10位" + " 系统最大报告号 " + arrays[0] + "-" + arrays[1] + "-YC-" + reportMaxCode);
            }
        }
        // 日志Buffer
        StringBuffer logBuffer = new StringBuffer();
        // 留号管理中
        ReserveCodeEntity oldReportMark3 = reserveCodeEntityMapper.selectReportRecordReserveCode(oldReportNumber);
        if (oldReportMark3 != null) {
            // 删除数据
            logBuffer.append("留号管理中旧报告号 = " + oldReportMark3.getReportCode() + "委托单号 = " + oldReportMark3.getEntrustmentNo());
            reserveCodeEntityMapper.deleteByPrimaryKey(oldReportMark3.getId());
        }
        ReserveCodeEntity newReportMark3 = reserveCodeEntityMapper.selectReportRecordReserveCode(newReportNumber);
        if (newReportMark3 != null) {
            // 删除数据
            logBuffer.append(" 留号管理中报告号 = " + newReportMark3.getReportCode() + "委托单号 = " + newReportMark3.getEntrustmentNo());
            reserveCodeEntityMapper.deleteByPrimaryKey(newReportMark3.getId());
        }
        String entrustmentNo = "";
        // 执行更新报告号操作。
        if (oldReportMark1 != null) {
            // 更新最终报告
            logBuffer.append(" 更新最终报告 旧报告号 = " + oldReportMark1.getReportCode() + "报告id = " + oldReportMark1.getId());
            logBuffer.append("委托单号 = " + oldReportMark1.getEntrustmentNo() + "新报告号 = " + newReportNumber);
            reserveCodeEntityMapper.updatefinalReport(oldReportMark1.getId(), newReportNumber);
            // 在中间报告中，当前新号 大于 最大号 需要进行add留号 state已使用。
            entrustmentNo = reserveCodeEntityMapper.getEntrustmentNo(oldReportMark1.getEntrustmentId());
        }
        if (oldReportMark2 != null) {
            // 更新中间报告
            logBuffer.append(" 更新中间报告 旧报告号 = " + oldReportMark2.getReportCode() + "报告id = " + oldReportMark2.getId());
            logBuffer.append("委托单号 = " + oldReportMark2.getEntrustmentNo() + "新报告号 = " + newReportNumber);
            reserveCodeEntityMapper.updateMidReport(oldReportMark2.getId(), newReportNumber);
            entrustmentNo = reserveCodeEntityMapper.getEntrustmentNo(oldReportMark2.getEntrustId());
        }
        // 在中间报告中，当前新号 大于 最大号 需要进行add留号 state已使用。
        ReserveCodeEntity reserveCodeEntity = new ReserveCodeEntity();
        reserveCodeEntity.setId(GenID.getID());
        reserveCodeEntity.setCreateDate(new Date());
        reserveCodeEntity.setState("已使用");
        reserveCodeEntity.setEntrustmentNo(Integer.parseInt(entrustmentNo));
        reserveCodeEntity.setReportCode(newReportNumber);
        reserveCodeEntity.setUseDate(new Date());
        reserveCodeEntity.setType("报告编号");
        reserveCodeEntityMapper.insert(reserveCodeEntity);
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), logBuffer.toString(), Const.REPORT_ORIGINAL, true);
        return ResultUtil.error("操作成功");
    }


}
