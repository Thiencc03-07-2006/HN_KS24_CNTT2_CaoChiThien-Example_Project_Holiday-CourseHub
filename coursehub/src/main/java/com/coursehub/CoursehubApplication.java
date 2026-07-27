package com.coursehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
public class CoursehubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoursehubApplication.class, args);
    }
}
