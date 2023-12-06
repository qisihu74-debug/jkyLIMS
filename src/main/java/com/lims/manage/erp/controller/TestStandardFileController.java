package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.StandardFileEntity;
import com.lims.manage.erp.entity.StandardMethodEntity;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestStandardFileService;
import com.lims.manage.erp.util.MinIoUtil;
import io.minio.MinioClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * 检验依据标准表(TestStandardFile)表控制层
 *
 * @author makejava
 * @since 2022-03-09 10:22:55
 */
@RestController
@RequestMapping("testStandardFile")
public class TestStandardFileController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestStandardFileService testStandardFileService;

    @GetMapping("/getList")
    public Result getAll(TestStandardFile testStandardFile) {
        QueryWrapper<TestStandardFile> queryWrapper = new QueryWrapper<>();
        if (testStandardFile.getType() != null) {
            queryWrapper.ne("type", testStandardFile.getType());
            queryWrapper.ne("type", "4");
        }
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testStandardFileService.list(queryWrapper));
    }

    /**
     * 分页查询所有数据
     *
     * @param page             分页对象
     * @param testStandardFile 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestStandardFile> page, TestStandardFile testStandardFile) {

        QueryWrapper<TestStandardFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (testStandardFile.getName() != null) {
            queryWrapper.like("name", testStandardFile.getName());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testStandardFileService.page(page, queryWrapper));

    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result selectOne(@PathVariable Serializable id) {
        if (id != null && id != "") {
            TestStandardFile testStandardFile = this.testStandardFileService.getOne(new QueryWrapper<TestStandardFile>().eq("id", id).eq("status", 0));
            return ResultUtil.success(testStandardFile);
        } else {
            return ResultUtil.error("参数为空");
        }

    }

    /**
     * 新增数据
     *
     * @param testStandardFile 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestStandardFile testStandardFile) {
        if (StrUtil.isEmptyIfStr(testStandardFile)) {
            return ResultUtil.error("数据为空");
        }
        return this.testStandardFileService.addTestStandardFile(testStandardFile);
    }

    /**
     * 修改数据
     *
     * @param testStandardFile 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestStandardFile testStandardFile) {

        if (StrUtil.isEmptyIfStr(testStandardFile)) {
            return ResultUtil.error("数据为空");
        }
        return this.testStandardFileService.updTestStandardFile(testStandardFile);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size() != 0) {
            return this.testStandardFileService.delTestStandardFile(idList);
        } else {
            return ResultUtil.error("数据为空");
        }
    }

    /**
     * 规范预览
     *
     * @param fileUrl
     * @return
     */
    @PostMapping("/previewFile")
    public void previewFile(String fileUrl, HttpServletResponse response) {
        if (StringUtils.isNotEmpty(fileUrl)) {
            MinioClient client = MinIoUtil.minioClient;
            //预览word转pdf
            String[] split = fileUrl.split("\\?");
            String[] strings = split[0].split("\\/");
            String bluckName = strings[3];
            String fileName = strings[4];
            try {
                client.statObject(bluckName, fileName);
                InputStream inputStream = client.getObject(bluckName, fileName);
                ServletOutputStream outputStream = response.getOutputStream();
                int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
                inputStream.close();
                outputStream.close();
            } catch (Exception e) {
                logger.error("预览标准规范异常:{}", e);
            }
        } else {
            return;
        }
    }

    /**##############################依据重做##################################**/
    /**
     * 新增依据
     *
     * @param standardFile
     * @param standardJson
     * @return
     */
    @PostMapping("/addStandard")
    public Result addStandard(@RequestParam("standardFile") MultipartFile standardFile,
                              @RequestParam("standardJson") String standardJson) {
        StandardFileEntity standardFileEntity = JSON.parseObject(standardJson, StandardFileEntity.class);
        return this.testStandardFileService.addStandardFile(standardFileEntity, standardFile);
    }

    /**
     * 新增检测方法
     *
     * @param standardMethodEntity
     * @return
     */
    @PostMapping("/addStandardMethod")
    public Result addStandardMethod(@RequestBody StandardMethodEntity standardMethodEntity) {
        return this.testStandardFileService.addStandardMethod(standardMethodEntity);
    }

    /**
     * 变更依据
     *
     * @param standardFile
     * @param standardJson
     * @return
     */
    @PostMapping("/updateStandard")
    public Result updateStandard(@RequestParam("standardFile") MultipartFile standardFile,
                                 @RequestParam("standardJson") String standardJson) {
        StandardFileEntity standardFileEntity = JSON.parseObject(standardJson, StandardFileEntity.class);
        return this.testStandardFileService.updateStandard(standardFileEntity, standardFile);
    }

    /**
     * 查询变更记录
     *
     * @param pid
     * @return
     */
    @GetMapping("/getRecords")
    public Result getRecords(Integer pid,Integer pageNum,Integer pageSize) {
        if (pid != null&&pageNum != null && pageSize!=null) {
            return ResultUtil.success("查询变更记录成功！",testStandardFileService.getRecords(pid,pageNum,pageSize));
        } else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 查询检测方法
     *
     * @param id
     * @return
     */
    @GetMapping("/getMethodList")
    public Result getMethodList(Integer id) {
        if (id != null) {
            return testStandardFileService.getMethodList(id);
        } else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 删除检测方法
     * @param id
     * @return
     */
    @GetMapping("/deleteMethod")
    public Result deleteMethod(Integer id) {
        if (id != null) {
            return testStandardFileService.deleteMethod(id);
        } else {
            return ResultUtil.error("参数为空");
        }
    }

}

