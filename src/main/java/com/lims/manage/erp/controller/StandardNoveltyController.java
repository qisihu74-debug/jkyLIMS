package com.lims.manage.erp.controller;

import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.StandardNovelty;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.job.StandardHander;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.StandardNoveltyService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2024-01-23 10:50
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/standardNovelty/")
public class StandardNoveltyController {
    /**
     * 查新结果缓存
     */
    public static final ConcurrentHashMap<String, Object> CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    @Autowired
    private StandardNoveltyService service;
    @Autowired
    private StandardHander standardHander;

    /**
     * 查新列表
     * @param bean
     * @return
     */
    @PostMapping("list")
    public Result list(@RequestBody StandardNovelty bean){
        //查新列表前把缓存查新数据写入数据库
        Collection<Object> values = CONCURRENT_HASH_MAP.values();
        List<StandardNovelty> arrayList = Lists.newArrayList();
        for (Object o :values){
            StandardNovelty standardNovelty1 = (StandardNovelty)o;
            arrayList.add(standardNovelty1);
        }
        service.updateBatchByCode(arrayList);
        //查新数据
        LambdaQueryWrapper<StandardNovelty> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(StringUtils.isNotEmpty(bean.getStatus()),StandardNovelty::getStatus,bean.getStatus());
        queryWrapper.like(StringUtils.isNotEmpty(bean.getCode()),StandardNovelty::getCode,bean.getCode().trim());
        queryWrapper.like(StringUtils.isNotEmpty(bean.getName()),StandardNovelty::getName,bean.getName().trim());
        List<StandardNovelty> list = service.list(queryWrapper);
        for (StandardNovelty standardNovelty :list){
            if ("现行".equals(standardNovelty.getStatus())){
                standardNovelty.setNote("发布日期："+standardNovelty.getReleaseDate()+";实施日期："+standardNovelty.getImplementationDate());
            }else if ("作废".equals(standardNovelty.getStatus())){
                standardNovelty.setNote("发布日期："+standardNovelty.getReleaseDate()+";作废日期："+standardNovelty.getImplementationDate());
            }
        }
        return ResultUtil.success(list);
    }

    /**
     * 新增
     * @param bean
     * @return
     */
    @PostMapping("add")
    public Result add(@RequestBody StandardNovelty bean){
        if (StringUtils.isEmpty(bean.getCode()) || StringUtils.isEmpty(bean.getName())){
            return ResultUtil.error("缺少参数");
        }
        LambdaQueryWrapper<StandardNovelty> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(StandardNovelty::getCode,bean.getCode());
        StandardNovelty one = service.getOne(queryWrapper);
        if (one != null){
            return ResultUtil.error("规范已存在");
        }
        boolean save = service.save(bean);
        if (save){
            return ResultUtil.success("操作成功");
        }else {
            return ResultUtil.error("操作失败");
        }
    }

    /**
     * 编辑
     * @param bean
     * @return
     */
    @PostMapping("edit")
    public Result edit(@RequestBody StandardNovelty bean){
        if (bean.getId() == null || StringUtils.isEmpty(bean.getCode()) || StringUtils.isEmpty(bean.getName())){
            return ResultUtil.error("缺少参数");
        }
        StandardNovelty byId = service.getById(bean.getId());
        if (!byId.getCode().equals(bean.getCode())){
            LambdaQueryWrapper<StandardNovelty> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(StandardNovelty::getCode,bean.getCode());
            StandardNovelty one = service.getOne(queryWrapper);
            if (one != null){
                return ResultUtil.error("规范已存在");
            }
        }
        LambdaUpdateWrapper<StandardNovelty> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(StandardNovelty::getId,bean.getId());
        updateWrapper.set(StandardNovelty::getCode,bean.getCode());
        updateWrapper.set(StandardNovelty::getName,bean.getName());
        boolean update = service.update(updateWrapper);
        if (update){
            return ResultUtil.success("操作成功");
        }else {
            return ResultUtil.error("操作失败");
        }
    }

    /**
     * 移除
     * @param ids
     * @return
     */
    @PostMapping("remove")
    public Result remove(@RequestParam("ids") List<Integer> ids){
        if (CollectionUtils.isEmpty(ids)){
            List<Integer> idList = Lists.newArrayList();
            List<StandardNovelty> list = service.list();
            for (StandardNovelty standardNovelty :list){
                idList.add(standardNovelty.getId());
            }
            boolean b = service.removeByIds(idList);
            if (b){
                return ResultUtil.success("操作成功");
            }else {
                return ResultUtil.error("操作失败");
            }
        }else {
            boolean b = service.removeByIds(ids);
            if (b){
                return ResultUtil.success("操作成功");
            }else {
                return ResultUtil.error("操作失败");
            }
        }
    }

    /**
     * 规范查新 code无值默认查新全部
     * @param code
     * @return
     */
//    @GetMapping("searchStandard")
//    public void searchStandard(String code){
//        if (StringUtils.isNotEmpty(code)){
//            TestStandardFile standardFiles = standardHander.checkStandard(code);
//            String note = "";
//            if ("现行".equals(standardFiles.getStandardStatus())){
//                note = "发布日期："+standardFiles.getReleaseDate()+";实施日期："+standardFiles.getImplementationDate();
//            }else if ("作废".equals(standardFiles.getStandardStatus())){
//                note  = "发布日期："+standardFiles.getReleaseDate()+";作废日期："+standardFiles.getImplementationDate();
//            }
//            //更新状态
//            LambdaUpdateWrapper<StandardNovelty> updateWrapper = new LambdaUpdateWrapper();
//            updateWrapper.eq(StandardNovelty::getCode,standardFiles.getCode());
//            updateWrapper.set(StandardNovelty::getFindDate, DateUtil.formatDate(new Date()));
//            updateWrapper.set(StandardNovelty::getStatus,standardFiles.getStandardStatus());
//            updateWrapper.set(StandardNovelty::getReleaseDate,standardFiles.getReleaseDate());
//            updateWrapper.set(StandardNovelty::getImplementationDate,standardFiles.getImplementationDate());
//            updateWrapper.set(StandardNovelty::getNote,note);
//            service.update();
//        }else {
//            //查新当天未查新的数据
//            LambdaQueryWrapper<StandardNovelty> queryWrapper = new LambdaQueryWrapper();
//            queryWrapper.ne(StandardNovelty::getFindDate,DateUtil.formatDate(new Date()));
//            List<StandardNovelty> standardNovelties = service.list(queryWrapper);
//            for (StandardNovelty standardNovelty :standardNovelties){
//                TestStandardFile standardFiles = standardHander.checkStandard(standardNovelty.getCode());
//                standardNovelty.setStatus(standardFiles.getStandardStatus());
//                standardNovelty.setFindDate(DateUtil.formatDate(new Date()));
//                standardNovelty.setReleaseDate(standardFiles.getReleaseDate());
//                standardNovelty.setImplementationDate(standardFiles.getImplementationDate());
//                if ("现行".equals(standardNovelty.getStatus())){
//                    standardNovelty.setNote("发布日期："+standardNovelty.getReleaseDate()+";实施日期："+standardNovelty.getImplementationDate());
//                }else if ("作废".equals(standardNovelty.getStatus())){
//                    standardNovelty.setNote("发布日期："+standardNovelty.getReleaseDate()+";作废日期："+standardNovelty.getImplementationDate());
//                }
//                //加入缓存
//                CONCURRENT_HASH_MAP.put(standardNovelty.getCode(),standardNovelty);
//            }
//            //批量更新
//            service.updateBatchById(standardNovelties);
//        }
//    }

    /**
     * 轮询查新规范状态
     * @param code
     * @return
     */
    @GetMapping("findCacheInfo")
    public Result findCacheInfo(String code,String isEnd){
        if (StringUtils.isNotEmpty(code)){
            StandardNovelty standardNovelty = (StandardNovelty)CONCURRENT_HASH_MAP.get(code);
            if (standardNovelty != null){
                return ResultUtil.success(standardNovelty);
            }else {
                TestStandardFile standardFiles = standardHander.checkStandard(code);
                if (standardFiles == null){
                    return ResultUtil.error("未查询到结果");
                }
                String note = "";
                if ("现行".equals(standardFiles.getStandardStatus())){
                    note = "发布日期："+standardFiles.getReleaseDate()+";实施日期："+standardFiles.getImplementationDate();
                }else if ("作废".equals(standardFiles.getStandardStatus())){
                    note  = "发布日期："+standardFiles.getReleaseDate()+";作废日期："+standardFiles.getImplementationDate();
                }
                StandardNovelty one = new StandardNovelty();
                one.setCode(code);
                one.setFindDate(DateUtil.formatDate(new Date()));
                one.setStatus(standardFiles.getStandardStatus());
                one.setReleaseDate(standardFiles.getReleaseDate());
                one.setImplementationDate(standardFiles.getImplementationDate());
                one.setNote(note);
                CONCURRENT_HASH_MAP.put(code,one);
                if ("是".equals(isEnd)){
                    Collection<Object> values = CONCURRENT_HASH_MAP.values();
                    List<StandardNovelty> list = Lists.newArrayList();
                    for (Object o :values){
                        StandardNovelty standardNovelty1 = (StandardNovelty)o;
                        list.add(standardNovelty1);
                    }
                    service.updateBatchByCode(list);
                }
                return ResultUtil.success(one);
            }
        }else {
            return ResultUtil.error("缺少参数");
        }
    }

    /**
     * 下载模版
     * @param response
     */
    @GetMapping("download")
    public void download(HttpServletResponse response){
        try {
            InputStream fileStream = MinIoUtil.getFileStream(BucketsConst.controlled_documents, "bzgf.xlsx");
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(fileStream, outputStream);   // copy流数据,i为字节数
            fileStream.close();
            outputStream.close();
        }catch (Exception e){
            log.error("下载模板失败:{}",e);
        }
    }

    /**
     * 导入文件
     * @param file
     * @return
     */
    @PostMapping("importFile")
    public Result importFile(MultipartFile file){
        if (file == null){
            return ResultUtil.error("请上传文件");
        }
        String filename = file.getOriginalFilename();
        String[] split = filename.split("\\.");
        if (!"xls,xlsx".contains(split[split.length - 1])) {
            return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
        }
        //读取文件内容跟新数据到数据库
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new Workbook(inputStream);
            Cells cells = workbook.getWorksheets().get(0).getCells();
            int maxRow = cells.getMaxRow();
            int index = 2;
            List<StandardNovelty> list = Lists.newArrayList();
            for (int i = 0; i < maxRow; i++) {
                Object value = cells.get("A" + index).getValue();
                Object object = cells.get("B" + index).getValue();
                if (value !=null){
                    String code = value.toString();
                    StandardNovelty standardNovelty = new StandardNovelty();
                    standardNovelty.setCode(code);
                    if (object != null){
                        standardNovelty.setName(object.toString());
                    }
                    list.add(standardNovelty);
                }
                index ++;
            }
            //增量数据进行插入
            List<StandardNovelty> novelties = service.list();
            if (CollectionUtils.isEmpty(novelties)){
                if (CollectionUtils.isNotEmpty(list)){
                    service.saveBatch(list);
                }
            }
            if (CollectionUtils.isNotEmpty(list) && CollectionUtils.isNotEmpty(novelties)){
                List<StandardNovelty> addList = list.stream()
                        .filter(item -> !novelties.stream().anyMatch(item2 -> item2.getCode().equals(item.getCode())))
                        .map(item -> new StandardNovelty(item.getId(),item.getCode(),item.getName(),"","","","",""))
                        .collect(Collectors.toList());
                service.saveBatch(addList);
            }
            return ResultUtil.success("导入成功");
        }catch (Exception e){
            log.error("导入数据失败:{}",e);
            return ResultUtil.error("导入数据失败");
        }
    }

    /**
     * 导出规范查新
     * @param response
     * @param code
     * @param name
     * @param status
     */
    @GetMapping("export")
    public void exportFile(HttpServletResponse response,String code,String name,String status){

    }
}
