package com.lims.manage.erp.service.impl;

import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.mapper.ReserveCodeEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.service.ReserveCodeService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
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

    @Override
    public Result addReserveCode(ReserveCodeEntity reserveCodeEntity) {
        if(reserveCodeEntity.getEntrustmentNo() == null){
            return ResultUtil.error("委托单号不能为空！");
        }
        String reportCode = reserveCodeEntity.getReportCode();
        if(reportCode != null){
            int codeSize = reserveCodeEntityMapper.getCodeSize(reportCode);
            if(codeSize != 0){
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
        if(!CollectionUtils.isEmpty(allList)){
            for (int i = 0; i < allList.size(); i++) {
                ReserveCodeEntity entity = allList.get(i);
                int codeSize = reserveCodeEntityMapper.getCodeSize(entity.getReportCode());
                if(codeSize != 0){//存在的不插入
                    noInsertList.add(entity);
                }else{//不存在的插入
                    insertList.add(entity);
                }
            }
            if(!CollectionUtils.isEmpty(noInsertList)){
                return ResultUtil.error("下方编号已存在，请重新编辑后再导入！",noInsertList);
            }
        }
        if(!CollectionUtils.isEmpty(insertList)){
            reserveCodeEntityMapper.batchInsert(insertList);
            return ResultUtil.success("预留编号成功！",insertList);
        }else{
            return ResultUtil.error("预留编号失败！");
        }
    }

    @Override
    public Result getMaxReportCode() {
        return ResultUtil.success("查询最大编号成功！",reportService.getMaxByUser());
    }


}
