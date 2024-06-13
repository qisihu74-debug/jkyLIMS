package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.SnEntity;
import com.lims.manage.erp.entity.SnRuleEntity;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ISnEntityService;
import com.lims.manage.erp.service.ISnRuleEntityService;
import com.lims.manage.erp.service.SnRuleService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义编号
 */
@Slf4j
@Api(tags="自定义编号")
@RestController
@RequestMapping("/sn/")
public class SnEntityController {
	@Autowired
	private ISnEntityService snEntityService;
	@Autowired
	private ISnRuleEntityService snRuleEntityService;
	@Autowired
	private SnRuleService service;

	/**
	 * 分页列表查询
	 *
	 * @param receiptType
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@GetMapping(value = "list")
	public Result<?> queryPageList(@Param("receiptType") String receiptType,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
		LambdaQueryWrapper<SnEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(StringUtils.isNotEmpty(receiptType),SnEntity::getReceiptType,receiptType);
		Page<SnEntity> page = new Page<SnEntity>(pageNo, pageSize);
		IPage<SnEntity> pageList = snEntityService.page(page, queryWrapper);
		//详情列表
		if (CollectionUtils.isNotEmpty(pageList.getRecords())){
			List<Long> ids = Lists.newArrayList();
			for (SnEntity entity:pageList.getRecords()) {
				ids.add(entity.getId());
			}
			LambdaQueryWrapper<SnRuleEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
			lambdaQueryWrapper.in(CollectionUtils.isNotEmpty(ids),SnRuleEntity::getSerialNumberId,ids);
			lambdaQueryWrapper.orderByAsc(SnRuleEntity::getSort);
			List<SnRuleEntity> list = snRuleEntityService.list(lambdaQueryWrapper);
			Map<Long,List<SnRuleEntity>> map = new HashMap<>();
			if (CollectionUtils.isNotEmpty(list)){
				for (SnRuleEntity snRuleEntity:list) {
					List<SnRuleEntity> snRuleEntities = map.get(snRuleEntity.getSerialNumberId());
					if (CollectionUtil.isEmpty(snRuleEntities)){
						List<SnRuleEntity> ruleEntities = Lists.newArrayList();
						ruleEntities.add(snRuleEntity);
						map.put(snRuleEntity.getSerialNumberId(),ruleEntities);
					}else {
						List<SnRuleEntity> ruleEntities = map.get(snRuleEntity.getSerialNumberId());
						ruleEntities.add(snRuleEntity);
						map.put(snRuleEntity.getSerialNumberId(),ruleEntities);
					}
				}
			}
			for (SnEntity snEntity:pageList.getRecords()) {
				List<SnRuleEntity> snRuleEntities = map.get(snEntity.getId());
				if (CollectionUtil.isNotEmpty(snRuleEntities)){
					snEntity.setList(snRuleEntities);
				}
			}
		}
		return ResultUtil.success(pageList);
	}

	/**
	 * 添加
	 *
	 * @param snEntity
	 * @return
	 */
	@Log(title = "自定义编号-新增", businessType = BusinessType.INSERT)
	@PostMapping(value = "add")
	@Transactional(rollbackFor = Exception.class)
	public Result<?> add(@RequestBody SnEntity snEntity) {
		Long id = GenID.getID();
		snEntity.setId(id);
		snEntity.setCreateTime(new Date());
		snEntity.setUserId(ShiroUtils.getUserInfo().getUserId()+"");
		snEntityService.save(snEntity);
		//规则详情表
		List<SnRuleEntity> list = snEntity.getList();
		for (SnRuleEntity snRuleEntity:list) {
			snRuleEntity.setSerialNumberId(id);
		}
		snRuleEntityService.saveBatch(list);
		return ResultUtil.success("添加成功","添加成功！");
	}

	/**
	 * 编辑
	 * @param snEntity
	 * @return
	 */
	@Log(title = "自定义编号-编辑", businessType = BusinessType.UPDATE)
	@Transactional(rollbackFor = Exception.class)
	@RequestMapping(value = "edit", method = {RequestMethod.PUT, RequestMethod.POST})
	public Result<?> edit(@RequestBody SnEntity snEntity) {
		//如果编辑前和编辑后的编号规则不一致，更新sys_serial_number_record reset状态
		SnEntity byId = snEntityService.getById(snEntity.getId());
		if (!byId.getSerialNumberRule().equals(snEntity.getSerialNumberRule())){
			snEntityService.updateResetByTypeAndTenantId(snEntity.getReceiptType(),1);
		}
		snEntity.setUpdateTime(new Date());
		snEntityService.updateById(snEntity);
		//删除旧记录
		Map<String,Object> map = new HashMap<>();
		map.put("serial_number_id",snEntity.getId());
		snRuleEntityService.removeByMap(map);
		//新增
		List<SnRuleEntity> list = snEntity.getList();
		for (SnRuleEntity snRuleEntity:list) {
			snRuleEntity.setSerialNumberId(snEntity.getId());
		}
		snRuleEntityService.saveBatch(list);
		return ResultUtil.success("编辑成功","编辑成功!");
	}

	/**
	 * 通过id删除
	 * @param id
	 * @return
	 */
	@Log(title = "自定义编号-删除", businessType = BusinessType.UPDATE)
	@DeleteMapping(value = "delete")
	public Result<?> delete(String id) {
		snEntityService.removeById(id);
		Map<String, Object> map = new HashMap<>();
		map.put("serial_number_id", id);
		snRuleEntityService.removeByMap(map);
		return ResultUtil.success("删除成功","删除成功!");
	}

	/**
	 * 根据编号规则类型获取编号
	 * @param type
	 * @return
	 */
	@GetMapping("getSnByType")
	public Result getSnByType(String type,String code){
		// 创建一个Runnable对象，它包含线程需要执行的任务
		Runnable task = () -> {
			// 打印当前线程的名字和递增的计数器
			String sn = service.getSnByType(type,code);
			System.out.println("线程："+Thread.currentThread().getName() + "获取到编号: "+sn);
		};
		// 创建并启动三个线程，它们都将运行相同的任务
		Thread thread1 = new Thread(task, "Thread-1");
		Thread thread2 = new Thread(task, "Thread-2");
		Thread thread3 = new Thread(task, "Thread-3");
		thread1.start();
		thread2.start();
		thread3.start();
		// 等待所有线程执行完毕，这里简单通过join方法实现
		try {
			thread1.join();
			thread2.join();
			thread3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("All threads have finished execution.");
		return ResultUtil.success("成功");
	}
}
