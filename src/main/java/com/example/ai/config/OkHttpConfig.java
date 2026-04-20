package com.example.ai.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class OkHttpConfig {

    @Value("${okhttp.connect-timeout:30}")
    private long connectTimeout;

    @Value("${okhttp.read-timeout:60}")
    private long readTimeout;

    @Value("${okhttp.write-timeout:60}")
    private long writeTimeout;

    @Value("${okhttp.max-idle-connections:20}")
    private int maxIdleConnections;

    @Value("${okhttp.keep-alive-duration:5}")
    private long keepAliveDuration;

    @Value("${okhttp.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${okhttp.proxy.host:127.0.0.1}")
    private String proxyHost;

    @Value("${okhttp.proxy.port:10808}")
    private int proxyPort;

    @Bean
    public OkHttpClient okHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(
                maxIdleConnections,
                keepAliveDuration,
                TimeUnit.MINUTES
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .readTimeout(Duration.ofSeconds(readTimeout))
                .writeTimeout(Duration.ofSeconds(writeTimeout))
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)
                .build();

        log.info("OkHttpClient initialized - connectTimeout: {}s, readTimeout: {}s, writeTimeout: {}s, maxIdleConnections: {}, keepAliveDuration: {}min",
                connectTimeout, readTimeout, writeTimeout, maxIdleConnections, keepAliveDuration);

        return client;
    }

    @Bean("proxyOkHttpClient")
    public OkHttpClient proxyOkHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(
                maxIdleConnections,
                keepAliveDuration,
                TimeUnit.MINUTES
        );

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .readTimeout(Duration.ofSeconds(readTimeout))
                .writeTimeout(Duration.ofSeconds(writeTimeout))
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true);

        if (proxyEnabled) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);
            log.info("ProxyOkHttpClient initialized with proxy: {}:{}, connectTimeout: {}s, readTimeout: {}s, writeTimeout: {}s",
                    proxyHost, proxyPort, connectTimeout, readTimeout, writeTimeout);
        } else {
            log.info("ProxyOkHttpClient initialized without proxy (proxy.enabled=false)");
        }

        return builder.build();
    }
}
