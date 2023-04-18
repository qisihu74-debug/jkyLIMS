package com.lims.manage.erp.config;


import com.lims.manage.erp.shiro.ShiroRealm;
import com.lims.manage.erp.shiro.ShiroSessionIdGenerator;
import com.lims.manage.erp.shiro.ShiroSessionManager;
import com.lims.manage.erp.util.SHA256Util;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description Shiro配置类
 * @Author gjl
 * @CreateTime 2021/11/09 17:42
 */
@Configuration
public class ShiroConfig {

    private final String CACHE_KEY = "shiro:cache:";
    private final String SESSION_KEY = "shiro:session:";

    //Redis配置
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
  /*  @Value("${spring.redis.timeout}")
    private int timeout;*/
    @Value("${spring.redis.password}")
    private  String password;
    /**
     * 开启Shiro-aop注解支持
     * @Attention 使用代理方式所以需要开启代码支持
     * @Author gjl
     * @CreateTime 2021/11/09 8:38
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * Shiro基础配置
     * @Author gjl
     * @CreateTime 2021/11/09 8:42
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactory(SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 注意过滤器配置顺序不能颠倒
        filterChainDefinitionMap.put("/source/**", "anon");
        filterChainDefinitionMap.put("/word/**", "anon");
        filterChainDefinitionMap.put("/sample/sampleInfo", "anon");
        filterChainDefinitionMap.put("/sample/downloadNewSampleTab", "anon");
        filterChainDefinitionMap.put("/userLogin/**", "anon");
        filterChainDefinitionMap.put("/qiyuesuo/**", "anon");
        filterChainDefinitionMap.put("/report/previewDownLoad", "anon");
        filterChainDefinitionMap.put("/report/preReportUrl", "anon");
        filterChainDefinitionMap.put("/task/downloadEntrust_two", "anon");
        filterChainDefinitionMap.put("/entrust/previewEntrust", "anon");
        filterChainDefinitionMap.put("/task/previewOriginalRecord", "anon");
        filterChainDefinitionMap.put("/testInstrument/exportInstrumentRecord", "anon");
        filterChainDefinitionMap.put("/testInstrument/batchExportInstrumentRecord", "anon");
        filterChainDefinitionMap.put("/app/testInstrument/taskList", "anon");
        filterChainDefinitionMap.put("/**/web/file_output_stream/getEntrustFileUrls", "anon");
        filterChainDefinitionMap.put("/app/accountUsage/*", "anon");
        filterChainDefinitionMap.put("/app/testInstrument/getDetails", "anon");
        //#静态资源放行
        filterChainDefinitionMap.put("/css/**","anon");
        filterChainDefinitionMap.put("/img/**","anon");
        filterChainDefinitionMap.put("/js/**","anon");
        filterChainDefinitionMap.put("/**/favicon.ico","anon");
        filterChainDefinitionMap.put("/**/index.html","anon");
        //pageOffice放行
        filterChainDefinitionMap.put("/**/poserver.zz","anon");
        filterChainDefinitionMap.put("/**/posetup.exe","anon");
        filterChainDefinitionMap.put("/**/pageoffice.js","anon");
        filterChainDefinitionMap.put("/**/jquery.min.js","anon");
        filterChainDefinitionMap.put("/**/pobstyle.css","anon");
        filterChainDefinitionMap.put("/**/sealsetup.exe","anon");

        // 配置shiro默认登录界面地址，前后端分离中登录界面跳转应由前端路由控制，后台仅返回json数据
        shiroFilterFactoryBean.setLoginUrl("index.html");
        filterChainDefinitionMap.put("/**", "authc");
        //shiroFilterFactoryBean.setLoginUrl("/userLogin/unauth");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
}

    /**
     * 安全管理器
     * @Author gjl
     * @CreateTime 2021/11/09 10:34
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 自定义Ssession管理
        securityManager.setSessionManager(sessionManager());
        // 自定义Cache实现
        securityManager.setCacheManager(cacheManager());
        // 自定义Realm验证
        securityManager.setRealm(shiroRealm());
        return securityManager;
    }

    /**
     * 身份验证器
     * @Author gjl
     * @CreateTime 2021/11/09 10:37
     */
    @Bean
    public ShiroRealm shiroRealm() {
        ShiroRealm shiroRealm = new ShiroRealm();
        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroRealm;
    }

    /**
     * 凭证匹配器
     * 将密码校验交给Shiro的SimpleAuthenticationInfo进行处理,在这里做匹配配置
     * @Author gjl
     * @CreateTime 2021/11/09 10:48
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher shaCredentialsMatcher = new HashedCredentialsMatcher();
        // 散列算法:这里使用SHA256算法;
        shaCredentialsMatcher.setHashAlgorithmName(SHA256Util.HASH_ALGORITHM_NAME);
        // 散列的次数，比如散列两次，相当于 md5(md5(""));
        shaCredentialsMatcher.setHashIterations(SHA256Util.HASH_ITERATIONS);
        return shaCredentialsMatcher;
    }

    /**
     * 配置Redis管理器
     * @Attention 使用的是shiro-redis开源插件
     * @Author gjl
     * @CreateTime 2021/11/09 11:06
     */
    @Bean
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host);
        redisManager.setPort(port);
        //redisManager.setTimeout(timeout);
        redisManager.setPassword(password);
        return redisManager;
    }

    /**
     * 配置Cache管理器
     * 用于往Redis存储权限和角色标识
     * @Attention 使用的是shiro-redis开源插件
     * @Author gjl
     * @CreateTime 2021/11/09 12:37
     */
    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        redisCacheManager.setKeyPrefix(CACHE_KEY);
        // 配置缓存的话要求放在session里面的实体类必须有个id标识
        redisCacheManager.setPrincipalIdFieldName("userId");
        return redisCacheManager;
    }

    /**
     * SessionID生成器
     * @Author gjl
     * @CreateTime 2021/11/09 13:12
     */
    @Bean
    public ShiroSessionIdGenerator sessionIdGenerator(){
        return new ShiroSessionIdGenerator();
    }

    /**
     * 配置RedisSessionDAO
     * @Attention 使用的是shiro-redis开源插件
     * @Author gjl
     * @CreateTime 2021/11/09 13:44
     */
    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        redisSessionDAO.setSessionIdGenerator(sessionIdGenerator());
        redisSessionDAO.setKeyPrefix(SESSION_KEY);
        redisSessionDAO.setExpire(2*3600);
        return redisSessionDAO;
    }

    /**
     * 配置Session管理器
     * @Author gjl
     * @CreateTime 2021/11/09 14:25
     */
    @Bean
    public SessionManager sessionManager() {
        ShiroSessionManager shiroSessionManager = new ShiroSessionManager();
        shiroSessionManager.setSessionDAO(redisSessionDAO());
        shiroSessionManager.setGlobalSessionTimeout(2*3600*1000);// 会话过期时间 ms
        shiroSessionManager.setSessionValidationSchedulerEnabled(true);
        shiroSessionManager.setSessionIdCookieEnabled(true);
        //设置session失效的扫描时间, 清理用户直接关闭浏览器造成的孤立会话 默认为 1个小时
        shiroSessionManager.setSessionValidationInterval(3600);
        return shiroSessionManager;
    }
}
