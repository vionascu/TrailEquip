package com.trailequip.trail.adapter.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Render/Railway EnvironmentPostProcessor.
 *
 * Converts DATABASE_URL environment variable to Spring datasource properties
 * BEFORE Spring Boot creates beans. This allows standard Spring datasource
 * autoconfiguration to work without custom bean creation.
 *
 * Runs very early in Spring Boot startup - before property sources are processed.
 */
public class RenderEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DATABASE_URL_ENV = "DATABASE_URL";
    private static final String RENDER_DATASOURCE_PROPS = "render-datasource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("🔍 RenderEnvironmentPostProcessor.postProcessEnvironment() called");

        // Check if DATABASE_URL is set (Render/Railway environment)
        String databaseUrl = System.getenv(DATABASE_URL_ENV);

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            System.out.println("ℹ️  DATABASE_URL not set - using local/default configuration");
            return;
        }

        System.out.println("✅ DATABASE_URL detected - configuring for Render/Railway");

        // Convert DATABASE_URL to JDBC format if needed
        String jdbcUrl = databaseUrl;
        if (!databaseUrl.startsWith("jdbc:")) {
            jdbcUrl = "jdbc:" + databaseUrl;
        }
        if (!jdbcUrl.contains("?")) {
            jdbcUrl += "?sslmode=require";
        }

        // Create Spring datasource properties from DATABASE_URL
        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", jdbcUrl);
        props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");

        // Add property source with high priority so it overrides application.yml
        environment.getPropertySources().addFirst(new MapPropertySource(RENDER_DATASOURCE_PROPS, props));

        String maskedUrl = jdbcUrl.replaceAll(":[^@]*@", ":***@");
        System.out.println("🔌 Render/Railway deployment - SPRING_DATASOURCE_URL set to: " + maskedUrl);
        System.out.println("✅ EnvironmentPostProcessor completed successfully");
    }
}
