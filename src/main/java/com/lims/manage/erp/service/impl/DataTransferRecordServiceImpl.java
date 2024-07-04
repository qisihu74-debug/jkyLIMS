package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DataTransferRecord;
import com.lims.manage.erp.entity.DataTransferRelation;
import com.lims.manage.erp.mapper.DataTransferRecordDao;
import com.lims.manage.erp.service.DataTransferRecordService;
import com.lims.manage.erp.service.DataTransferRelationService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.DataTransferRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 数据流转记录业务层实现类
 *
 * @author: zhq
 * @date: 2024-06-12
 * @version: v1.0
 */
@Service
@Slf4j
public class DataTransferRecordServiceImpl extends ServiceImpl<DataTransferRecordDao, DataTransferRecord> implements DataTransferRecordService {

    @Resource
    private DataTransferRelationService dataTransferRelationService;

    @Override
    public boolean addDataTransfer(DataTransferRecord dataTransferRecord) {
        dataTransferRecord.setUserId(ShiroUtils.getUserInfo().getUserId());
        dataTransferRecord.setUserName(ShiroUtils.getUserInfo().getName());
        return save(dataTransferRecord);
    }

    @Override
    public boolean addTransferRelation(DataTransferRelation dataTransferRelation) {
       return dataTransferRelationService.addTransferRelation(dataTransferRelation);
    }

    @Override
    public List<DataTransferRecordVo> getTransferRecordList(String dataId) {
        //获取当前数据有关联的数据id列表
        Set<String> dataIdSet = dataTransferRelationService.getDataIdSet(dataId);
        if (dataIdSet.size() > 0) {
            //根据id列表获取流转记录
            return baseMapper.getTransferRecordList(dataIdSet);
        } else {
            return new ArrayList<>();
        }
    }
}
