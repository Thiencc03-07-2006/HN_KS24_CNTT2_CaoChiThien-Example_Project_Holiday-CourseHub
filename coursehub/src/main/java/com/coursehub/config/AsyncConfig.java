package com.coursehub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // @EnableAsync enables @Async in EmailServiceImpl, etc.
    // @EnableScheduling enables @Scheduled for token cleanup tasks (future)
}
