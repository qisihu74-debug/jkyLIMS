package com.lims.manage.erp.config;

import com.lims.manage.erp.service.FlowableService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
        File file = null;
        ClassPathResource resource = new ClassPathResource("processes" + File.separator);
        try {
            file = resource.getFile();
        }catch (IOException e){
            log.error("读取流程配置文件失败:{}",e);
        }
        File[] files = file.listFiles();
        //部署过的流行不在重复部署
        List<String> list = service.getDeployed();
        if (files!=null && files.length>1){
            for (File file1:files) {
                String path = "processes\\"+file1.getName();
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
