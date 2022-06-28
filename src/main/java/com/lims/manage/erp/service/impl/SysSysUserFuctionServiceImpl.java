package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.SysRoleDao;
import com.lims.manage.erp.mapper.SysRoleFuncMenuDao;
import com.lims.manage.erp.mapper.SysUserFuctionDao;
import com.lims.manage.erp.service.SysUserFuctionService;
import com.lims.manage.erp.vo.SysRoleFuncMenuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

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
    @Autowired
    private SysRoleFuncMenuDao sysRoleFuncMenuDao;

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
            if (treeEntity.getChildren() != null && treeEntity.getChildren().size() > 0) {
                treeEntity.setCatesFlag(true);
            } else {
                treeEntity.setCatesFlag(false);
            }
            if (treeEntity.getFunctionPid() == 0) {
                bigTree.add(treeEntity);
            }
        }
        return bigTree;
    }

    @Override
    public List<TreeFunction> GetListPeer() {
        return fuctionDao.getList();
    }

    @Override
    public List<TreeFunction> GetListUpgrade(Long userid) {
        List<TreeFunction> dataList = returnListUpgrade(userid);
        if (dataList == null || dataList.isEmpty()) {
            System.out.println("此用户不包含菜单信息，请配置");
            return null;
        }
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
            if (treeEntity.getChildren() != null && treeEntity.getChildren().size() > 0) {
                treeEntity.setCatesFlag(true);
            } else {
                treeEntity.setCatesFlag(false);
            }
            if (treeEntity.getFunctionPid() == 0) {
                bigTree.add(treeEntity);
            }
        }
        return bigTree;
    }

    @Override
    public List<TreeFunction> GetListUpgrade1(Long userid) {
//        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userid);
        List<TreeFunction> dataList = returnListUpgrade1(userid);
        if(CollectionUtils.isEmpty(dataList)){
            System.out.println("此用户不包含菜单信息，请配置");
            return null;
        }
//        List<TreeFunction> dataList = new ArrayList<>();
//        for(SysRoleFunctionParent sysRoleFunctionParent:menuIdList){
//            TreeFunction treeFunction = new TreeFunction();
//            treeFunction.setFunctionId(sysRoleFunctionParent.getFunctionId());
//            treeFunction.setFunctionPid(sysRoleFunctionParent.getFunctionPid());
//            treeFunction.setTreeName(sysRoleFunctionParent.getTreeName());
//            treeFunction.setCatesFlag(false);
//            treeFunction.setSort(sysRoleFunctionParent.getSort());
//            dataList.add(treeFunction);
//        }
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
            if (treeEntity.getChildren() != null && treeEntity.getChildren().size() > 0) {
                treeEntity.setCatesFlag(true);
            } else {
                treeEntity.setCatesFlag(false);
            }
            if (treeEntity.getFunctionPid() == 0) {
                bigTree.add(treeEntity);
            }
        }
        return bigTree;
    }

    @Override
    public List<Long> getRoleMenu(Long roleId) {

        return sysRoleFuncMenuDao.getFunctionIdByRoleIdS(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean grant(SysRoleFuncMenuVo entity) {
        Boolean flag = false;
        List<SysRoleFunction> roleFunctions = new ArrayList<>();
        for (Long id : entity.getList()) {
            SysRoleFunction sysRoleFunction = new SysRoleFunction();
            sysRoleFunction.setRoleId(entity.getRoleId());
            sysRoleFunction.setFunctionId(id);
            roleFunctions.add(sysRoleFunction);
        }
        //角色菜单重新更新
        sysRoleFuncMenuDao.delFuncByRoleId(entity.getRoleId());
        sysRoleFuncMenuDao.insertBatchRoleFunc(roleFunctions);
        flag = true;
        return flag;
    }

    /**
     * 1、用户拥有 多个角色 2、多个角色下 展示具体菜单项ID
     *
     * @param userid
     * @return
     */
    public List<TreeFunction> returnListUpgrade(Long userid) {
        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userid);
        if (menuIdList.isEmpty() || menuIdList.get(0) == null) {
            System.out.println("此用户不包含菜单信息，请配置");
            return null;
        }
        // 得到用户id下 所属菜单。
        Map<Long, SysRoleFunction> map = new HashMap<>();
        for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
            map.put(sysRoleFunction.getFunctionId(), sysRoleFunction);
            map.put(sysRoleFunction.getFunctionPid(), sysRoleFunction);
        }
        // 菜单ID信息 展示所有 去除 functionIdSet
        List<TreeFunction> dataList = fuctionDao.getList();
        Iterator<TreeFunction> iterator = dataList.iterator();
        // 保留父级id 存放到map中
        for (TreeFunction treeFunction : dataList) {
            for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
                if (treeFunction.getFunctionId().equals(sysRoleFunction.getFunctionPid())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionId(), sysRoleFunction);
                }
                if (treeFunction.getFunctionPid().equals(sysRoleFunction.getFunctionId())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionPid(), sysRoleFunction);
                }
            }
        }
        for (TreeFunction treeFunction : dataList) {
            if (map.get(treeFunction.getFunctionId()) != null && map.get(treeFunction.getFunctionPid()) == null) {
//                map.put(treeFunction.getFunctionPid(), null);
                System.out.println("输出展示信息" + treeFunction.getFunctionId());
                map.put(treeFunction.getFunctionPid(), null);
                map.put(treeFunction.getFunctionId(), null);
            }
        }
        while (iterator.hasNext()) {
            TreeFunction item = iterator.next();
            SysRoleFunction removeEntity = map.get(item.getFunctionId());
            if (removeEntity == null) {
                iterator.remove();
            }
        }
        return dataList;
    }

    /**
     * 1、用户拥有 多个角色 2、多个角色下 展示具体菜单项ID
     * 暂时废弃 6月28 误删（测试菜单详情处理）
     * @param userid
     * @return
     */
    public List<TreeFunction> returnListUpgrade1(Long userid) {
        List<SysRoleFunctionParent> menuIdList = sysRoleFuncMenuDao.selectSetMenuPid(userid);
        int idNumber = 0;
        for (SysRoleFunctionParent sysRoleFunctionParent:menuIdList) {
            System.out.println("自定义菜单序号"+idNumber+"\t"+sysRoleFunctionParent);
            idNumber+=1;
        }
        List<TreeFunction> dataList = fuctionDao.getList();
        int dataIds = 0;
        for(TreeFunction treeFunction:dataList){
            System.out.println("返回all菜单"+dataIds+"\t"+treeFunction);
            dataIds+=1;
        }
        System.out.println("自定义菜单序号集"+idNumber+"\t"+dataIds+"\t"+"返回all菜单");
        if (menuIdList.isEmpty() || menuIdList.get(0) == null) {
            System.out.println("此用户不包含菜单信息，请配置");
            return null;
        }
        // 得到用户id下 所属菜单。
        Map<Long, SysRoleFunction> map = new HashMap<>();
        for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
            map.put(sysRoleFunction.getFunctionId(), sysRoleFunction);
            map.put(sysRoleFunction.getFunctionPid(), sysRoleFunction);
        }
        for (SysRoleFunctionParent sysRoleFunction : menuIdList) {
            for (TreeFunction treeFunction : dataList) {
                if (treeFunction.getFunctionId().equals(sysRoleFunction.getFunctionPid())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionId(), sysRoleFunction);
                }
                if (treeFunction.getFunctionPid().equals(sysRoleFunction.getFunctionId())) {
                    // 主要取决于key 不要求vlue数值的准确性
                    map.put(treeFunction.getFunctionPid(), sysRoleFunction);
                }
            }
        }
        for (TreeFunction data : dataList) {
            if (map.get(data.getFunctionId()) != null && map.get(data.getFunctionPid()) == null) {
                SysRoleFunction removeEntity = new SysRoleFunction();
                removeEntity.setId(0L);
                map.put(data.getFunctionPid(), removeEntity);
            }
        }
        // 菜单ID信息 展示所有 去除 functionIdSet
        Iterator<TreeFunction> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            TreeFunction item = iterator.next();
            SysRoleFunction removeEntity = map.get(item.getFunctionId());
            if (removeEntity == null) {
                iterator.remove();
            }
        }
        return dataList;
    }
}
