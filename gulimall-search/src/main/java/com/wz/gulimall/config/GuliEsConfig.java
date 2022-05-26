package com.wz.gulimall.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuliEsConfig {

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        COMMON_OPTIONS = builder.build();
    }

//    @Bean
//    public RestHighLevelClient restHighLevelClient() {
//
//        //es 有用户名和密码
//        RestHighLevelClient esClient = null;
//        //初始化ES操作客户端
//        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY,
//                new UsernamePasswordCredentials("elastic", "bb8371465@"));  //es账号密码
//        esClient = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("es-0d1te96k.public.tencentelasticsearch.com", 9200)
//                ).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//                        httpClientBuilder.disableAuthCaching();
//                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
//                    }
//                }));
//        return esClient;
//    }


    @Bean
    public RestHighLevelClient restHighLevelClient() {

        //es 有用户名和密码
        RestHighLevelClient esClient = null;
        esClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("124.221.206.12", 9200,"http")
                ));
        return esClient;
    }
}
