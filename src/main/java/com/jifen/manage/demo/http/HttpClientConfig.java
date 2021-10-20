package com.jifen.manage.demo.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * HttpClient配置
 * @author gjl
 */
@Configuration
@Slf4j
public class HttpClientConfig {

    @Value("${http.pool.maxTotal:1000}")
    private int maxTotal;

    @Value("${http.pool.defaultMaxPerRoute:500}")
    private int defaultMaxPerRoute;

    @Value("${http.responseKeepAlive:30000}")
    private long httpResponseKeepAlive;

    @Value("${http.request.connectionRequestTimeout:30000}")
    private int httpRequestConnectionRequestTimeout;

    @Value("${http.request.connectTimeout:30000}")
    private int httpRequestConnectTimeout;

    @Value("${http.request.socketTimeout:30000}")
    private int httpRequestSocketTimeout;

    @Value("${http.request.validateAfterInactivity:5000}")
    private int httpRequestValidateAfterInactivity;

    @Bean
    public RequestConfig requestConfig(){
        return RequestConfig.custom()
                .setSocketTimeout(httpRequestSocketTimeout)
                .setConnectTimeout(httpRequestConnectTimeout)
                .setConnectionRequestTimeout(httpRequestConnectionRequestTimeout)
                .build();
    }

    /**
     * 配置HTTP与HTTPS
     * @return
     * @throws Exception
     */
    @Bean
    public Registry<ConnectionSocketFactory> registry() throws Exception {

        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(ctx))
                .build();
        return socketFactoryRegistry;
    }


    /**
     * 定义httpclient连接池
     *
     * @param registry
     * @return
     */
    @Bean
    @Autowired
    public PoolingHttpClientConnectionManager httpClientConnectionManager(Registry registry) {
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        httpClientConnectionManager.setMaxTotal(maxTotal);
        httpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        httpClientConnectionManager.setValidateAfterInactivity(httpRequestValidateAfterInactivity);
        return httpClientConnectionManager;
    }

    /**
     * 定义 HttpClient工厂，使用HttpClientBuilder构建
     *
     * @return
     */
    @Bean
    public HttpClientBuilder httpClientBuilder(PoolingHttpClientConnectionManager connectionManager) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(connectionManager);
        return httpClientBuilder;
    }

    /**
     * 得到httpClient的实例
     *
     * @return
     */
    @Bean
    @Autowired
    public CloseableHttpClient httpClient(HttpClientBuilder httpClientBuilder,
                                          ConnectionKeepAliveStrategy  connectionKeepAliveStrategy) {
        CloseableHttpClient client = httpClientBuilder.setKeepAliveStrategy(connectionKeepAliveStrategy).build();
        /**
         * 添加关闭钩子 : JVM停止或重启时，关闭连接池释放掉连接
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("closing http client");
                client.close();
                log.info("http client closed");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }));
        return client;
    }

    /**
     * 长链接配置
     * @return
     */
    @Bean
    public ConnectionKeepAliveStrategy  connectionKeepAliveStrategy() {

        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                // Honor 'keep-alive' header
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && "timeout".equalsIgnoreCase(param)) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch(NumberFormatException ignore) {
                        }
                    }
                }
                return httpResponseKeepAlive;
            }

        };
        return myStrategy;
    }
}
