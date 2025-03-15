package com.pansophicmind.server.aidog.common.ws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 5;
    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 20;
    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 1000;
    /**
     * 线程空闲时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;
    /**
     * 线程名称前缀
     */
    private static final String THREAD_NAME_PREFIX = "WsThreadPool-";

    @Bean(name = "wsThreadPoolExecutor")
    public ThreadPoolTaskExecutor wsThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        // 设置最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        // 设置队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        // 设置线程空闲时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        // 设置线程名称前缀
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化线程池
        executor.initialize();
        return executor;
    }
}