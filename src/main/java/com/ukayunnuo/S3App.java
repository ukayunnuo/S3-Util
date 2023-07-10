package com.ukayunnuo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 消费者 启动类
 *
 * @author ukayunnuo
 * @since 1.0.0
 */

@SpringBootApplication
public class S3App {
    public static void main(String[] args) {
        SpringApplication.run(S3App.class, args);
    }
}
