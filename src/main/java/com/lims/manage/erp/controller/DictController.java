package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.Dict;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.ShiroUtils;
import java.util.Date;
import com.lims.manage.erp.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 字典
 *
 * @author : zhq
 * @version : V1.0
 * @date :   2021-12-10
 */
@Slf4j
@RestController
@RequestMapping("/dict")
public class DictController extends ApiController {

    @Resource
    private DictService dsDictService;

    /**
     * 分页列表查询
     *
     * @param dict     字典对象
     * @param pageNo   页码
     * @param pageSize 条数
     * @return Result
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(Dict dict,
                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>(dict);
        Page<Dict> page = new Page<>(pageNo, pageSize);
        IPage<Dict> pageList = dsDictService.page(page, queryWrapper);
        return ResultUtil.success("获取字典数据成功", pageList);
    }

    /**
     * 获取字典数据
     *
     * @param dictCode 字典code
     * @param dictCode 表名,文本字段,code字段  | 举例：sys_user,realname,id
     * @return
     */
    @PostMapping(value = "/getDictItems")
    public Result<?> getDictItems(@RequestBody String dictCode) {
		/*Result<List<DsDictItem>> result = new Result<List<DsDictItem>>();
		try{
			List<DsDictItem> ls = dsDictService.queryDictItemsByCode(dictCode);
			result.setSuccess(true);
			result.setResult(ls);
		}catch(Exception e){
			result.error500("操作失败");
			return result;
		}
		return result;*/
        return null;
    }



    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody Dict e) {
        String u = ShiroUtils.getUserInfo() != null ? ShiroUtils.getUserInfo().getUsername() : "system";
        e.setCreateBy(u);
        e.setCreateTime(new Date());
        e.setUpdateBy(u);
        e.setUpdateTime(new Date());
        dsDictService.save(e);
        return ResultUtil.success("新增成功");
    }

    @PostMapping(value = "/edit")
    public Result<?> edit(@RequestBody Dict e) {
        e.setUpdateBy(ShiroUtils.getUserInfo() != null ? ShiroUtils.getUserInfo().getUsername() : "system");
        e.setUpdateTime(new Date());
        dsDictService.updateById(e);
        return ResultUtil.success("修改成功");
    }

    @PostMapping(value = "/del")
    public Result<?> removeItems(@RequestBody java.util.List<String> ids) {
        if (ids == null || ids.isEmpty()) { return ResultUtil.error("数据为空"); }
        dsDictService.removeByIds(ids);
        return ResultUtil.success("删除成功");
    }

}
