package com.lims.manage.demo.config;


import com.github.pagehelper.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author gjl
 */

@Configuration
public class MybatisPlusConfig
{
   @Bean
   public PageHelper pageHelper() {
       PageHelper pageHelper = new PageHelper();
       //添加配置，也可以指定文件路径
       Properties p = new Properties();
       p.setProperty("offsetAsPageNum", "true");
       p.setProperty("rowBoundsWithCount", "true");
       p.setProperty("reasonable", "true");
       p.setProperty("params","count=countSql");
       p.setProperty("helper-dialect","mysql");
       pageHelper.setProperties(p);
       return pageHelper;
   }
}