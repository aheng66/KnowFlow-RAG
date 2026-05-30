package com.yizhaoqi.knowflow.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "knowflowDotenv";
    private static final String DOTENV_FILE = ".env";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isSurefireTestRun()) {
            return;
        }

        Path dotenvPath = Path.of(System.getProperty("user.dir"), DOTENV_FILE);
        System.out.println("[DotenvEnvironmentPostProcessor] 尝詴加轲 .env 文件: " + dotenvPath.toAbsolutePath());
        System.out.println("[DotenvEnvironmentPostProcessor] 文件是否存在: " + Files.isRegularFile(dotenvPath));
        
        if (!Files.isRegularFile(dotenvPath)) {
            System.out.println("[DotenvEnvironmentPostProcessor] .env 文件不存在，跳过加轲");
            return;
        }

        Map<String, Object> properties = loadDotenv(dotenvPath);
        System.out.println("[DotenvEnvironmentPostProcessor] 加轲的配置项数量: " + properties.size());
        System.out.println("[DotenvEnvironmentPostProcessor] SPRING_DATASOURCE_USERNAME: " + properties.get("SPRING_DATASOURCE_USERNAME"));
        System.out.println("[DotenvEnvironmentPostProcessor] SPRING_DATASOURCE_PASSWORD: " + (properties.containsKey("SPRING_DATASOURCE_PASSWORD") ? "***已配置***" : "未配置"));
        
        if (properties.isEmpty()) {
            System.out.println("[DotenvEnvironmentPostProcessor] 没有加轲到任何配置");
            return;
        }

        applyActiveProfiles(environment, properties);

        SystemEnvironmentPropertySource propertySource = new SystemEnvironmentPropertySource(PROPERTY_SOURCE_NAME, properties);
        MutablePropertySources propertySources = environment.getPropertySources();
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            propertySources.remove(PROPERTY_SOURCE_NAME);
        }

        if (propertySources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
            propertySources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource);
            System.out.println("[DotenvEnvironmentPostProcessor] .env 配置已成功加轲并注入到环境中");
            return;
        }

        propertySources.addLast(propertySource);
        System.out.println("[DotenvEnvironmentPostProcessor] .env 配置已成功加轲");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isSurefireTestRun() {
        return System.getProperty("surefire.test.class.path") != null;
    }

    private Map<String, Object> loadDotenv(Path dotenvPath) {
        Map<String, Object> properties = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(dotenvPath, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();
                if (key.isEmpty()) {
                    continue;
                }

                properties.put(key, unquote(value));
            }
        } catch (IOException ignored) {
            // Ignore malformed or unreadable .env files and continue with normal environment resolution.
        }
        return properties;
    }

    private void applyActiveProfiles(ConfigurableEnvironment environment, Map<String, Object> properties) {
        Object rawProfiles = properties.get("SPRING_PROFILES_ACTIVE");
        if (!(rawProfiles instanceof String profilesValue) || profilesValue.isBlank()) {
            return;
        }

        String[] profiles = profilesValue.split(",");
        for (int i = 0; i < profiles.length; i++) {
            profiles[i] = profiles[i].trim();
        }
        environment.setActiveProfiles(profiles);
    }

    private String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
