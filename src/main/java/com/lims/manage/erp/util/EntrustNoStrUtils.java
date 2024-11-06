package com.lims.manage.erp.util;

import com.github.pagehelper.PageHelper;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.vo.EntrustCategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/9/27 11:47
 */
@Component
public class EntrustNoStrUtils {

    @Autowired
    private TaskMapper taskMapper;

    /**
     * 用于拆分委托编号信息
     *
     * @param entrustNoStr
     * @return
     */
    public EntrustCategoryVo splitEntrustNo(String entrustNoStr) {
        EntrustCategoryVo entrustCategoryVo = new EntrustCategoryVo();
        // 处理委托编号String信息类型
        if (!StringUtils.isEmpty(entrustNoStr)) {
            // 查询编号类型值
            List<TestInitDataEntity> dataEntityList = taskMapper.selectEntrustBasis(15);

            String entrustCategory = null;
            Boolean flag = false;
            // 遍历委托单类别信息
            for (TestInitDataEntity testInitDataEntity : dataEntityList) {
                if (com.lims.manage.erp.util.StringUtils.isNotEmpty(testInitDataEntity.getRemark()) && entrustNoStr.contains(testInitDataEntity.getRemark())) {
                    flag = true;
                    entrustCategory = testInitDataEntity.getRemark();
                }
            }
            if (flag) {
                entrustCategoryVo.setEntrustCategoryType(entrustCategory);
                // 拆分委托编号 : "MD10086" 10086进行存储
                String[] entrustNostrs = entrustNoStr.split(entrustCategory);
                if (entrustNostrs.length > 0) {
                    if (!StringUtils.isEmpty(entrustNostrs[1])) {
                        entrustCategoryVo.setEntrustmentNo(Integer.parseInt(entrustNostrs[1]));
                    }
                }
            } else {
                entrustCategoryVo.setEntrustCategoryType(null);
                entrustCategoryVo.setEntrustmentNo(Integer.parseInt(entrustNoStr));
            }

        }
        return entrustCategoryVo;
    }
}
