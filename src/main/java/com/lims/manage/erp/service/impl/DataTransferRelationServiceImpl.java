package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DataTransferRelation;
import com.lims.manage.erp.mapper.DataTransferRelationDao;
import com.lims.manage.erp.service.DataTransferRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据流转关系业务层实现类
 *
 * @author: zhq
 * @date: 2024-06-13
 * @version: v1.0
 */
@Service
@Slf4j
public class DataTransferRelationServiceImpl extends ServiceImpl<DataTransferRelationDao, DataTransferRelation> implements DataTransferRelationService {


    @Override
    public boolean addTransferRelation(DataTransferRelation dataTransferRelation) {
        if (!dataTransferRelation.getDataId().equals(dataTransferRelation.getDataRelationId())) {
            //获取数据库中是否有对应记录
            Integer count = baseMapper.getTransferRelationCount(dataTransferRelation);
            //如果没有对应流转记录关系
            if (count < 1) {
                //添加流转记录关系
               return save(dataTransferRelation);
            }
        }
        return false;
    }

    @Override
    public Set<String> getDataIdSet(String dataId) {
        Set<String> idSet = new HashSet<>();
        dd(dataId, idSet);
        return idSet;
    }

    /**
     * 递归调用获取所有关联的数据id
     *
     * @param dataId 数据id
     * @param idSet  用于去重
     * @return 关联的数据列表
     */
    public Set<String> dd(String dataId, Set<String> idSet) {
        idSet.add(dataId);
        Set<String> dataIdSet = baseMapper.getDataIdSet(dataId);
        if (dataIdSet.size() > 0) {
            for (String id : dataIdSet) {
                if (!idSet.contains(id)) {
                    idSet.addAll(dd(id, idSet));
                }
            }
        }
        return idSet;
    }
}
