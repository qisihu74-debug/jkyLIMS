package com.lims.manage.erp;

//import com.lims.manage.erp.config.FastDFSConfig;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.MultipartConfigElement;
import java.io.FileNotFoundException;

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
public class MyApplication {
    @Value("${server.port}")
    private int serverPort;
    @Value("${http.port}")
    private int httpPort;
    @Value("${posyspath}")
    private String poSysPath;
    @Value("${popassword}")
    private String poPassword;

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
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
        factory.setMaxFileSize("40960KB"); //KB,MB
        /// 设置总上传数据总大小
        factory.setMaxRequestSize("409600KB");
        return factory.createMultipartConfig();
    }

    /**
     * 添加PageOffice的服务器端授权程序Servlet（必须）
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean pageofficeRegistrationBean() {
        com.zhuozhengsoft.pageoffice.poserver.Server poserver = new com.zhuozhengsoft.pageoffice.poserver.Server();
        poserver.setSysPath(poSysPath);//设置PageOffice注册成功后,license.lic文件存放的目录
        ServletRegistrationBean srb = new ServletRegistrationBean(poserver);
        srb.addUrlMappings("/poserver.zz");
        srb.addUrlMappings("/posetup.exe");
        srb.addUrlMappings("/pageoffice.js");
        srb.addUrlMappings("/jquery.min.js");
        srb.addUrlMappings("/pobstyle.css");
        srb.addUrlMappings("/sealsetup.exe");
        return srb;//
    }


    /**
     * 添加印章管理程序Servlet（可选）
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean zoomsealRegistrationBean() throws FileNotFoundException {
        com.zhuozhengsoft.pageoffice.poserver.AdminSeal adminSeal = new com.zhuozhengsoft.pageoffice.poserver.AdminSeal();
        adminSeal.setAdminPassword(poPassword);//设置印章管理员admin的登录密码（为了安全起见，强烈建议修改此密码）
        /**如果当前项目是打成jar或者war包运行，强烈建议将poseal.db文件的路径更换成某个固定的绝对路径下,不要放当前项目文件夹下,为了防止每次重新打包程序导致poseal.db被替换的问题。
         * 比如windows服务器下：D:/lic/，linux服务器下:/root/lic/
         */
        //设置印章数据库文件poseal.db存放的目录
        adminSeal.setSysPath(poSysPath);
        ServletRegistrationBean srb = new ServletRegistrationBean(adminSeal);
        srb.addUrlMappings("/adminseal.zz");
        srb.addUrlMappings("/sealimage.zz");
        srb.addUrlMappings("/loginseal.zz");
        return srb;//
    }
}
