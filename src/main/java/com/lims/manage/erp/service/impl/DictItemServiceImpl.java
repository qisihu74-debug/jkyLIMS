package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DictItem;
import com.lims.manage.erp.mapper.DictItemDao;
import com.lims.manage.erp.service.DictItemService;
import com.lims.manage.erp.vo.DictItemTreeVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhq
 * @Desc 字典数据业务实现类
 * @Date 2023-01-03 14:48
 */
@Service
@Slf4j
public class DictItemServiceImpl extends ServiceImpl<DictItemDao, DictItem> implements DictItemService {

    @Override
    public List<DictItemTreeVo> getDictItemTree(String parentId) {
        return baseMapper.getDictItemTree(parentId);
    }
}
