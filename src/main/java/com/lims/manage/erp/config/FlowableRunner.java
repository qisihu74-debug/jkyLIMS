package com.lims.manage.erp.config;

import com.alibaba.fastjson.JSON;
import com.google.common.io.Resources;
import com.lims.manage.erp.service.FlowableService;
import com.lims.manage.erp.util.FileAndFolderUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.config
 * @desc
 * @date 2021/11/17 15:42
 * @Copyright © 河南交科院
 */
@Service
@Slf4j
public class FlowableRunner implements ApplicationRunner {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private FlowableService service;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //支持多个bpmn文件自动部署
        Resource resource1 = new ClassPathResource("processes//");
        File file = null;
        try {
            file = resource1.getFile();
            log.debug("processes文件对象获取成功:{}", JSON.toJSONString(file));
        }catch (Exception e){
            log.error("processes文件对象内容失败:{}", e);
        }
        if (file != null){
            String[] Strings = file.list();
            //部署过的流行不在重复部署
            List<String> list = service.getDeployed();
            if (Strings!=null && Strings.length>1){
                for (String s:Strings) {
                    String path = "processes\\"+s;
                    if (list.contains(path)){
                        continue;
                    }else {
                        try {
                            repositoryService.createDeployment()
                                    .addClasspathResource(path)
                                    .deploy();
                            log.info("成功：部署工作流成功：{}",path);
                        } catch (Exception e) {
                            log.error("失败：部署工作流失败：" + path);
                        }
                    }
                }
            }
        }
    }
}
