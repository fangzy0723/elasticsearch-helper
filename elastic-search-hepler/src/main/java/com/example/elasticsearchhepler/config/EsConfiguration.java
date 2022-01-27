package com.example.elasticsearchhepler.config;

import com.example.elasticsearchhepler.utils.EmptyUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.ArrayList;

/**
 * 暂时不用  todo
 */
//@Configuration
public class EsConfiguration implements EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(EsConfiguration.class);
    private Environment environment;
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    //private static String hosts = "192.168.21.124"; // 集群地址，多个用,隔开
    private static String hosts; // 集群地址，多个用,隔开
    private static String username;
    private static String password;

    private static int port = 9200; // 使用的端口号
    private static String schema = "http"; // 使用的协议
    private static ArrayList<HttpHost> hostList = null;
    private static int connectTimeOut = 1000; // 连接超时时间
    private static int socketTimeOut = 30000; // 连接超时时间
    private static int connectionRequestTimeOut = 500; // 获取连接的超时时间
    private static int maxConnectNum = 100; // 最大连接数
    private static int maxConnectPerRoute = 100; // 最大路由连接数

    private RestClientBuilder builder;

//    static {
//        hostList = new ArrayList<>();
//        String[] hostStrs = hosts.split(",");
//        for (String host : hostStrs) {
//            hostList.add(new HttpHost(host, port, schema));
//        }
//    }

    @Bean(name = "restLowLevelClient")
    public RestClient restLowLevelClient(){
        hosts = environment.getProperty("spring.data.elasticsearch.cluster-nodes");
        username = environment.getProperty("spring.data.elasticsearch.username");
        password = environment.getProperty("spring.data.elasticsearch.password");
        if(EmptyUtil.isEmpty(hosts)){
            logger.error("==================ES地址为空！！！！");
        }else {
            hostList = new ArrayList<>();
            String[] hostStrs = hosts.split(",");
            for (String host : hostStrs) {
                if(host.contains(":")){
                    host = host.split(":")[0].trim();
                }
                hostList.add(new HttpHost(host, port, schema));
            }
        }
        builder = RestClient.builder(hostList.toArray(new HttpHost[0]));
        setConnectTimeOutConfig();
        setMutiConnectConfig();
        setFailureListenerConfig();
        return builder.build();
    }

    @Bean(name = "restHighLevelClient", destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(@Qualifier("restLowLevelClient") RestClient restClient) {
        return new RestHighLevelClient(builder);
    }

    /**
     * 配置请求超时，将连接超时（默认为1秒）和套接字超时（默认为30秒）
     * 修改為自己的配置
     */
    public void setConnectTimeOutConfig() {
        builder.setRequestConfigCallback(new RequestConfigCallback() {
            @Override
            public Builder customizeRequestConfig(Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(connectTimeOut);
                requestConfigBuilder.setSocketTimeout(socketTimeOut);
                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
                return requestConfigBuilder;
            }
        });
    }

    /**
     * 配置异步请求的线程数量，Apache Http Async Client默认启动一个调度程序线程，以及由连接管理器使用的许多工作线程
     * （与本地检测到的处理器数量一样多，取决于Runtime.getRuntime().availableProcessors()返回的数量）。
     * 默认值是1
     *
     */
    public void setMutiConnectConfig() {
        builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(maxConnectNum);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                //认证机制
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                return httpClientBuilder;
            }
        });
    }

    /**
     * 设置监听器，每次节点失败都可以监听到，可以作额外处理
     */
    public void setFailureListenerConfig() {
        builder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                super.onFailure(node);
                logger.info("===================ES节点操作失败 host:{},hostName:{}" , node.getHost(), node.getName());
            }
        });
    }

    /**
     * ES初始化操作
     * @return
     */
    @Bean
    public ElasticsearchListener elasticsearchListener(){
        return new ElasticsearchListener();
    }
}
