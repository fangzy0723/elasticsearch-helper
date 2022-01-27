package com.example.elasticsearchhepler.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


@Configuration
@ConfigurationProperties(prefix = "es")
@Data
@Slf4j
public class EsClientConfig {
    private String[] host;
    private int port;
    private String scheme;
    private String username;
    private String password;

    /**
     * 初始化 RestHighLevelClient bean
     *
     * @return
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        log.info("初始化ES客户端连接。。。");
        HttpHost[] hosts = new HttpHost[host.length];
        for (int i = 0; i < host.length; i++) {
            hosts[i] = new HttpHost(host[i], port, scheme);
        }
        RestClientBuilder builder = RestClient.builder(hosts);

        if (StringUtils.hasLength(username)){
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(f -> f.setDefaultCredentialsProvider(credentialsProvider));
        }
        builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                .setConnectTimeout(5000)
                .setSocketTimeout(150000));
        return new RestHighLevelClient(builder);
    }
}
