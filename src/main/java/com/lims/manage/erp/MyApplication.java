package com.lims.manage.erp;

//import com.lims.manage.erp.config.FastDFSConfig;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.MultipartConfigElement;

/**
 * @author gjl
 * EnableTransactionManagement开启事务
 * EnableScheduling开启定时任务
 */
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
//@Import({FastDFSConfig.class})
@EnableTransactionManagement
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class
})
@EnableScheduling
@MapperScan("com.lims.manage.erp.mapper")
public class MyApplication extends SpringBootServletInitializer {
    @Value("${server.port}")
    private int serverPort;
    @Value("${http.port}")
    private int httpPort;

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MyApplication.class);
    }

    /**
     * SpringBoot2.x配置HTTPS,并实现HTTP访问自动转向HTTPS
     * @return
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory(){
            @Override
            protected void postProcessContext(Context context) {
                //注释放开就是强制开启https
               /* SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);*/
            }
        };
        tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]\\");
            }
        });
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }
    /**
     * 解决异常信息：
     *  java.lang.IllegalArgumentException:
     *      Invalid character found in the request target. The valid characters are defined in RFC 7230 and RFC 3986
     * @return
     */
    /*@Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]\\");
            }
        });
        return factory;
    }*/
    @Bean
    public Connector httpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        // 监听Http的端口
        connector.setPort(httpPort);
        connector.setSecure(false);
        // 监听Http端口后转向Https端口
        connector.setRedirectPort(serverPort);
        return connector;
    }

    /**
     * 文件上传配置
     * @return
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大
        factory.setMaxFileSize("10240KB"); //KB,MB
        /// 设置总上传数据总大小
        factory.setMaxRequestSize("102400KB");
        return factory.createMultipartConfig();
    }
}
