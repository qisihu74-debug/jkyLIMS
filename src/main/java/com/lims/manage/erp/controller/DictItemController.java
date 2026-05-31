package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.Dict;
import com.lims.manage.erp.entity.DictItem;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.ShiroUtils;
import java.util.Date;
import com.lims.manage.erp.service.DictItemService;
import com.lims.manage.erp.service.DictService;
import com.lims.manage.erp.util.CopyBeanUtils;
import com.lims.manage.erp.vo.DictItemTreeVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典数据
 *
 * @author : zhq
 * @version : V1.0
 * @date :   2021-12-10
 */
@Slf4j
@RestController
@RequestMapping("/dict/item")
public class DictItemController extends ApiController {

    @Resource
    private DictItemService dictItemService;

    @Resource
    private DictService dictService;
    /**
     * 分页列表查询
     *
     * @param dictItem 字典对象
     * @param pageNo   页码
     * @param pageSize 条数
     * @return Result
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(DictItem dictItem,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        QueryWrapper<DictItem> queryWrapper = new QueryWrapper<>(dictItem);
        Page<DictItem> page = new Page<>(pageNo, pageSize);
        IPage<DictItem> pageList = dictItemService.page(page, queryWrapper);
        return ResultUtil.success("获取字典值数据成功", pageList);
    }

    /**
     * 字典管理-获取字典项值树结构
     *
     * @param dictItemTreeVo	字典项值树模型
     * @return	Result
     */
    @GetMapping(value = "/dictItemTree")
    public Result<?> dictItemTree(DictItemTreeVo dictItemTreeVo) {
        LambdaQueryWrapper<DictItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictItem::getDictId, dictItemTreeVo.getDictId());
        wrapper.orderByAsc(DictItem::getItemSort);
        List<DictItem> dictItemList = dictItemService.list(wrapper);
        List<DictItemTreeVo> treeModelList;
        treeModelList = CopyBeanUtils.copyListProperties(dictItemList, DictItemTreeVo::new);
        if(treeModelList.size() > 0){
            treeModelList.forEach(this::findAllChild);
        }
        return ResultUtil.success(treeModelList);
    }

    /**
     * 字典管理-通过字典编码获取字典项值树结构
     *
     * @param dictCode	字典编码
     * @return	Result
     */
    @GetMapping(value = "/getDictItemTree/{dictCode}")
    public Result<?> getDictItemTree(@PathVariable String dictCode) {
        LambdaQueryWrapper<Dict> dictWrapper = Wrappers.lambdaQuery();
        dictWrapper.eq(Dict::getDictCode, dictCode);
        Dict dict = dictService.getOne(dictWrapper);
        DictItemTreeVo dictItemTreeVo = new DictItemTreeVo();
        dictItemTreeVo.setDictId(dict.getDictId());
        return dictItemTree(dictItemTreeVo);
    }

    /**
     * 通过上级主键级联查询
     *
     * @param dictItemTreeModel	字典项值树结构对象
     */
    public void findAllChild(DictItemTreeVo dictItemTreeModel){
        List<DictItemTreeVo> childrenList = dictItemService.getDictItemTree(dictItemTreeModel.getItemId());
        dictItemTreeModel.setChildren(childrenList);
        if(childrenList.size() > 0){
            childrenList.forEach(this::findAllChild);
        }
    }

    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody DictItem e) {
        String u = ShiroUtils.getUserInfo() != null ? ShiroUtils.getUserInfo().getUsername() : "system";
        e.setCreateBy(u);
        e.setCreateTime(new Date());
        e.setUpdateBy(u);
        e.setUpdateTime(new Date());
        dictItemService.save(e);
        return ResultUtil.success("新增成功");
    }

    @PostMapping(value = "/edit")
    public Result<?> edit(@RequestBody DictItem e) {
        e.setUpdateBy(ShiroUtils.getUserInfo() != null ? ShiroUtils.getUserInfo().getUsername() : "system");
        e.setUpdateTime(new Date());
        dictItemService.updateById(e);
        return ResultUtil.success("修改成功");
    }

    @PostMapping(value = "/del")
    public Result<?> removeItems(@RequestBody java.util.List<String> ids) {
        if (ids == null || ids.isEmpty()) { return ResultUtil.error("数据为空"); }
        dictItemService.removeByIds(ids);
        return ResultUtil.success("删除成功");
    }

}
