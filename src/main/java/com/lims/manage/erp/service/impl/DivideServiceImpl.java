package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DivideEntity;
import com.lims.manage.erp.mapper.DivideDao;
import com.lims.manage.erp.service.DivideService;
import com.lims.manage.erp.vo.DivideVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-07-10 11:30
 * @Copyright © 河南交科院
 */
@Service
public class DivideServiceImpl extends ServiceImpl<DivideDao, DivideEntity> implements DivideService {
    /**
     * 更新 评审分工信息
     *
     * @param newDivideVoList
     * @param activeId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDivide(List<DivideVo> newDivideVoList, Integer activeId) {

        // 获取 对应评审分工 信息：
        LambdaQueryWrapper<DivideEntity> divideWrapper = new LambdaQueryWrapper<>();
        divideWrapper.eq(DivideEntity::getActiveId, activeId);
        List<DivideEntity> oldDivideList = this.baseMapper.selectList(divideWrapper);
        // 删除不包含的 分工信息
        for (DivideEntity divideEntity : oldDivideList) {
            Boolean falg = true;
            for (DivideVo divideVo : newDivideVoList) {
                String[] arrays = divideVo.getDeptName().split("&");
                if (divideEntity.getDeptId().equals(arrays[0])) {
                    falg = true;
                }
            }
            if (!falg) {
                // 进行删除操作
                LambdaQueryWrapper<DivideEntity> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(DivideEntity::getDeptId, divideEntity.getDeptId());
                deleteWrapper.eq(DivideEntity::getActiveId, activeId);
                this.baseMapper.delete(deleteWrapper);
            }
        }
        if (CollectionUtil.isNotEmpty(oldDivideList)) {
            // key deptId 、value =  List<DivideEntity> divideList;
            LinkedHashMap<String, List<DivideEntity>> map = new LinkedHashMap<>();

            for (DivideEntity divideEntity : oldDivideList) {
                if (map.get(divideEntity.getDeptId() + "&" + divideEntity.getDeptName()) == null) {
                    List<DivideEntity> entityList = new ArrayList<>();
                    entityList.add(divideEntity);
                    map.put(divideEntity.getDeptId() + "&" + divideEntity.getDeptName(), entityList);
                } else {
                    List<DivideEntity> entityList = map.get(divideEntity.getDeptId() + "&" + divideEntity.getDeptName());
                    entityList.add(divideEntity);
                    map.put(divideEntity.getDeptId() + "&" + divideEntity.getDeptName(), entityList);
                }
            }

            // 比较 更新数据
            for (DivideVo divideVo : newDivideVoList) {
                // 获取当前 最大id +1.
                QueryWrapper<DivideEntity> entityLambdaQueryChainWrapper = new QueryWrapper<>();
                entityLambdaQueryChainWrapper.select("IFNULL(max( divide_id ) + 1,1) as divide_id");
                entityLambdaQueryChainWrapper.last("limit 1");
                DivideEntity divideEntity = this.baseMapper.selectOne(entityLambdaQueryChainWrapper);
                Integer divideId = divideEntity.getDivideId();

                if (map.get(divideVo.getDeptId() + "&" + divideVo.getDeptName()) == null) {
                    // 这组信息 是需要新增的

//                    String[] arrays = divideVo.getDeptName().split("&");
                    List<DivideEntity> addDivideList = divideVo.getDivideList();

                    for (DivideEntity divideEntity1 : addDivideList) {
                        divideEntity1.setDivideId(divideId);
                        divideEntity1.setActiveId(activeId);
                        divideEntity1.setDeptId(divideVo.getDeptId());
                        divideEntity1.setDeptName(divideVo.getDeptName());
                        this.baseMapper.insert(divideEntity1);
                    }
                    divideId = divideId + 1;
                } else {
//                    String[] arrays = divideVo.getDeptName().split("&");
                    // 进行更新操作
                    for (DivideEntity divideEntity1 : divideVo.getDivideList()) {
                        divideEntity1.setDivideId(divideVo.getDivideId());
                        divideEntity1.setActiveId(divideVo.getActiveId());
                        divideEntity1.setDeptId(divideVo.getDeptId());
                        divideEntity1.setDeptName(divideVo.getDeptName());
                    }
                    // 执行新增 or 更新
                    saveOrUpdateBatch(divideVo.getDivideList());
                }
            }
        }
    }
}
