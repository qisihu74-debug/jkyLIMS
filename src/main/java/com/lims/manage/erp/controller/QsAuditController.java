package com.lims.manage.erp.controller;

import com.aspose.cells.Cells;
import com.aspose.cells.Worksheet;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.AduditBaseData;
import com.lims.manage.erp.entity.BaseTreeBuild;
import com.lims.manage.erp.entity.InternalAuditorActive;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AduditBaseDataService;
import com.lims.manage.erp.service.QsAuditService;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc 部门负责人（部长/所长/主任）；内审员（具有内审角色的账户）
 * @date 2024-07-04 15:56
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("/audit/")
public class QsAuditController {
    @Autowired
    private QsAuditService qsAuditService;
    @Autowired
    private AduditBaseDataService aduditBaseDataService;

    /**
     * 技术质量部内审活动列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("internalAuditorActiveList")
    public Result internalAuditorActiveList(Integer pageNum, Integer pageSize, String name){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少参数");
        }
        Long userId = ShiroUtils.getUserInfo().getUserId();
        PageInfo<InternalAuditorActive> pageInfo = qsAuditService.internalAuditorActiveList(pageNum,pageSize,name,userId);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 开始检查前获取基础数据
     * @return
     */
    @GetMapping("getCheckBaseDataList")
    public Result getCheckBaseDataList(){
        List<AduditBaseData> list = qsAuditService.getCheckBaseDataList();
        //根据类型处理数据
        List<AduditBaseData> cnasList = Lists.newArrayList();
        List<AduditBaseData> cmaList = Lists.newArrayList();
        for (AduditBaseData baseData :list){
            if ("CNAS".equals(baseData.getType())){
                cnasList.add(baseData);
            }
            if ("CMA".equals(baseData.getType())){
                cmaList.add(baseData);
            }
        }
        List<AduditBaseData> cnasLis = BaseTreeBuild.buildTree(cnasList);
        List<AduditBaseData> cmaLis = BaseTreeBuild.buildTree(cmaList);
        Map<String,List<AduditBaseData>> map = new HashMap<>();
        map.put("CNAS",cnasLis);
        map.put("CMA",cmaLis);
        return ResultUtil.success(map);
    }


    //检查暂存，判断管理员是否完成检查


    //检查提交，判断管理员是否完成检查


    //继续检查数据回显，

    //检查完成修改数据回显

    @SneakyThrows
    @GetMapping("importBaseData")
    public Result importBaseData(){
        List<AduditBaseData> list = Lists.newArrayList();
        //读取excel
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook("D:\\Users\\Administrator\\Desktop\\CMA.xlsx");
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        //遍历excel，根据编号查询
        int num = 2;

        while (num<=242){
            AduditBaseData baseData = new AduditBaseData(null,null,null,null,null,null,null,0);
            baseData.setType(cells.get("A"+num).getValue().toString());
            baseData.setDirectory(cells.get("B"+num).getValue().toString());
            String string = cells.get("C" + num).getValue().toString();
            String[] split = string.split("\\.");
            baseData.setId(Integer.parseInt(split[0]));
            String string1 = cells.get("D" + num).getValue().toString();
            String[] split1 = string1.split("\\.");
            baseData.setPid(Integer.parseInt(split1[0]));
            baseData.setContent(cells.get("E"+num).getValue().toString());
            Object value = cells.get("F" + num).getValue();
            if (value != null){
                baseData.setMethod(value.toString());
            }
            Object value1 = cells.get("G" + num).getValue();
            if (value1 != null){
                baseData.setNote(value1.toString());
            }
            list.add(baseData);
            num++;
        }
        aduditBaseDataService.saveBatch(list);
        return ResultUtil.success("导入成功");
    }

}
