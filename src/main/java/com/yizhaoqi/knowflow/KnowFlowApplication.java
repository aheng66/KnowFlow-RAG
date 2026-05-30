package com.yizhaoqi.knowflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class KnowFlowApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(KnowFlowApplication.class);
        Environment env = app.run(args).getEnvironment();
        
        // 验证 .env 文件是否被正确读取
        System.out.println("========== .env 配置验证 ==========");
        System.out.println("SPRING_DATASOURCE_URL: " + env.getProperty("SPRING_DATASOURCE_URL"));
        System.out.println("SPRING_DATASOURCE_USERNAME: " + env.getProperty("SPRING_DATASOURCE_USERNAME"));
        System.out.println("SPRING_DATASOURCE_PASSWORD: " + (env.getProperty("SPRING_DATASOURCE_PASSWORD") != null ? "***已配置***" : "未配置"));
        System.out.println("Active Profiles: " + String.join(",", env.getActiveProfiles()));
        System.out.println("====================================");
    }

}
