package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.SysFunction;
import com.lims.manage.erp.entity.TreeFunction;
import com.lims.manage.erp.mapper.SysUserFuctionDao;
import com.lims.manage.erp.service.SysUserFuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.service.impl
 * @desc
 * @date 2021/11/10 14:16
 * @Copyright © 河南交科院
 */
@Service
public class SysSysUserFuctionServiceImpl implements SysUserFuctionService {
    @Autowired
    private SysUserFuctionDao fuctionDao;

    @Override
    public List<SysFunction> getFunctionByuserId(Long userId) {

        return fuctionDao.getFunctionByuserId(userId);
    }
    @Override
    public List<TreeFunction> GetList() {
        List<TreeFunction> dataList = fuctionDao.getList();
        List<TreeFunction> bigTree = new ArrayList<>();
        for (TreeFunction treeEntity : dataList) {
            List children = new ArrayList<>();
            //再次遍历list，找到user的子节点
            for (TreeFunction node : dataList) {
                if (node.getFunctionPid().equals(treeEntity.getFunctionId())) {
                    children.add(node);
                }
            }
            treeEntity.setChildren(children);
            if(treeEntity.getChildren()!=null && treeEntity.getChildren().size()>0){
                treeEntity.setCatesFlag(true);
            }
            else {
                treeEntity.setCatesFlag(false);
            }
            if(treeEntity.getFunctionPid() == null || treeEntity.getFunctionPid().equals(0)){
                bigTree.add(treeEntity);
            }
        }
        return bigTree;
    }
}
