package com.icbc.online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ICBC智能在线银行系统 - 主启动类
 *
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@MapperScan("com.icbc.online.mapper")
public class OnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineApplication.class, args);
        System.out.println("========================================");
        System.out.println("  ICBC智能在线银行系统启动成功！");
        System.out.println("  版本: V1.0.0");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("  API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("========================================");
    }
}
