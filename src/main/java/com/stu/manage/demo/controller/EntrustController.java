package com.stu.manage.demo.controller;

import com.github.pagehelper.PageInfo;
import com.stu.manage.demo.entity.StatusEntity;
import com.stu.manage.demo.mapper.JtEntrustInfoMapper;
import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.service.EntrustService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Get;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/entrust/")
public class EntrustController {
    Logger logger = LoggerFactory.getLogger(EntrustController.class);
    @Autowired
    private EntrustService entrustService;
    @Autowired
    private JtEntrustInfoMapper jtEntrustInfoMapper;

    @GetMapping("once_more")
    public Result onceMore(String entrustNumber){
        return ResultUtil.success(entrustService.onceMore(entrustNumber));
    }

    @GetMapping("get_check_items")
    public Result getCheckItems(int productId){
        return ResultUtil.success(entrustService.getCheckItemsByProductId(productId));
    }
    @GetMapping("get_check_basis")
    public Result getCheckBasis(int productId){
        return ResultUtil.success(entrustService.getCheckBasisByProductId(productId));
    }

    /**
     * 根据委托单id查看委托单状态和所属样品检测进度
     * @param id
     * @return
     */
    @GetMapping("status")
    public Result status(Integer id){
        if (id == null){
            return ResultUtil.error(-1,"缺少必要参数");
        }
        return ResultUtil.success(entrustService.status(id));
    }




    @RequestMapping(value="/add",method = RequestMethod.POST)
    public Result add(@RequestBody HashMap<String,Object> map)
    {
        System.out.println("展示存储信息！   "+map);
        String status = entrustService.addEntrustInfo(map);
        if (status !=null)
        {
            return ResultUtil.success(status);
        }
         return ResultUtil.error(-1,"新增失败");
    }

    /**
     * 返回委托方式
     * @return
     */
    @GetMapping("get_select_lists")
    public Result getEntrustTheWay() {
        return ResultUtil.success(entrustService.getSelectLists());
    }

    /**
     * 发送消息
     * @return
     */
    @GetMapping("sendMessage")
    public Result sendMessage(){
        return ResultUtil.success(entrustService.sendMessage());
    }

    /**
     * 获取委托列表
     * @param type
     * @param userId
     * @param adminId
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("ownerTask")
    public Result ownerTask(String type, Integer userId, String adminId, Long startTime,
                            Long endTime, Integer pageNo, Integer pageSize){
        if (pageNo == null || pageSize == null){
            return ResultUtil.error(-1,"缺少分页参数");
        }
        if (StringUtils.isEmpty(type)){
            return ResultUtil.error(-1,"缺少要查看的委托类型");
        }
        if (StringUtils.isEmpty(adminId) || userId == null){
            return ResultUtil.error(-1,"缺少必要参数");
        }
        PageInfo pageInfo = entrustService.ownerTask(type, userId, adminId, startTime,endTime, pageNo, pageSize);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 获取委托数量
     * @param adminId
     * @return
     */
    @GetMapping("count")
    public Result count(String adminId){
        if (StringUtils.isEmpty(adminId)){
            return ResultUtil.error(-1,"缺少必要的参数！");
        }
        Map<String,String> map = entrustService.count(adminId);
        return ResultUtil.success(map);
    }
}
