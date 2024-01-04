package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.entity.TestControlledDocumentsEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestControlledDocumentsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * (TestControlledDocuments)表服务接口
 *
 * @author 丁连春
 * @since 2023-12-25 10:30:10
 */
@RestController
@RequestMapping("testControlledDocuments")
public class TestControlledDocumentsController extends ApiController {
    /**
     * 受控文件信息
     */
    @Resource
    private TestControlledDocumentsService testControlledDocumentsService;

    /**
     * 分页查询所有数据
     *
     * @param testControlledDocuments 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(TestControlledDocumentsEntity testControlledDocuments) {
        return testControlledDocumentsService.selectTestControlledDocumentsList(testControlledDocuments);
    }

    /**
     * 返回受控文件基础信息
     *
     * @return
     */
    @GetMapping("/getDocumentsBasic")
    public Result getDocumentsBasic() {

        return testControlledDocumentsService.getDocumentsBasic();

    }

    /**
     * 查询详情
     *
     * @return 查询详情
     */
    @GetMapping("/{id}")
    public Result details(@PathVariable Integer id) {

        return this.testControlledDocumentsService.details(id);
    }

    /**
     * 新增数据
     *
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestParam("json") String json, MultipartFile[] file) {
        TestControlledDocumentsEntity testControlledDocumentsEntity = JSON.parseObject(json, TestControlledDocumentsEntity.class);
        if (StrUtil.isEmptyIfStr(testControlledDocumentsEntity)) {
            return ResultUtil.error("数据为空");
        }
        return this.testControlledDocumentsService.addTestControlledDocuments(testControlledDocumentsEntity, file);
    }

    /**
     * 修改数据
     *
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestParam("json") String json, MultipartFile[] file) {
        TestControlledDocumentsEntity testControlledDocumentsEntity = JSON.parseObject(json, TestControlledDocumentsEntity.class);
        if (StrUtil.isEmptyIfStr(testControlledDocumentsEntity)) {
            return ResultUtil.error("数据为空");
        }
        return this.testControlledDocumentsService.updateTestControlledDocuments(testControlledDocumentsEntity, file);
    }

    /**
     * 变更记录
     *
     * @return 变更记录
     */
    @PostMapping("/change")
    public Result changeRecord(@RequestParam("json") String json, MultipartFile[] file) {
        TestControlledDocumentsEntity testControlledDocumentsEntity = JSON.parseObject(json, TestControlledDocumentsEntity.class);
        if (StrUtil.isEmptyIfStr(testControlledDocumentsEntity)) {
            return ResultUtil.error("数据为空");
        }
        return this.testControlledDocumentsService.changeRecord(testControlledDocumentsEntity, file);
    }

    /**
     * 查询-变更记录
     *
     * @param testControlledDocuments
     * @return
     */
    @GetMapping("/getList")
    public Result getAll(TestControlledDocumentsEntity testControlledDocuments) {
        // 实现分页
        return this.testControlledDocumentsService.getList(testControlledDocuments);
    }


    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除结果
     */
    @GetMapping("/del")
//    public Result delete(@RequestBody List<Integer> idList) {
    public Result delete(Integer id) {
        if (id != null) {
            return this.testControlledDocumentsService.delReportTemplate(id);
        } else {
            return ResultUtil.error("数据为空");
        }
    }

    /**
     * 在线模板数据查看
     *
     * @param type
     * @return
     */
    @GetMapping("/getTemplateData")
    public Result getTemplateData(String type) {
        if (type != null) {
            return this.testControlledDocumentsService.getTemplateData(type);
        } else {
            return ResultUtil.error("请求数据为空");
        }
    }
}

