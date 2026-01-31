package com.trailequip.trail.adapter.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * Render/Railway deployment DataSource configuration.
 *
 * Activates when spring.datasource.render-url property is set.
 * This is set from DATABASE_URL environment variable via application properties.
 *
 * Converts PostgreSQL URL to JDBC format with SSL.
 */
@Configuration
public class RenderDataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(RenderDataSourceConfig.class);
    private final Environment environment;

    public RenderDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.render-url")
    public DataSource dataSource() {
        // Try to get from property first (set from DATABASE_URL env var)
        String databaseUrl = environment.getProperty("spring.datasource.render-url");

        // Fallback to environment variable directly
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            databaseUrl = System.getenv("DATABASE_URL");
        }

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            logger.error("DATABASE_URL not available - Render deployment misconfigured");
            throw new IllegalStateException("DATABASE_URL must be set for Render deployment");
        }

        // DATABASE_URL format: postgresql://user:password@host:port/dbname
        // Convert to JDBC format: jdbc:postgresql://user:password@host:port/dbname?sslmode=require
        String jdbcUrl = databaseUrl;
        if (!databaseUrl.startsWith("jdbc:")) {
            jdbcUrl = "jdbc:" + databaseUrl;
        }
        if (!jdbcUrl.contains("?")) {
            jdbcUrl += "?sslmode=require";
        }

        String maskedUrl = jdbcUrl.replaceAll(":[^@]*@", ":***@");
        logger.info("✅ Render deployment - Using DATABASE_URL: {}", maskedUrl);

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .build();
    }
}
