package com.trailequip.trail.adapter.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Render/Railway deployment DataSource configuration.
 *
 * Automatically detects DATABASE_URL environment variable.
 * If present, creates DataSource and overrides default config.
 * If absent, returns null and lets Spring use application.yml config.
 *
 * Converts PostgreSQL URL to JDBC format with SSL.
 */
@Configuration
public class RenderDataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(RenderDataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        // Check environment variable directly (set by Render/Railway)
        String databaseUrl = System.getenv("DATABASE_URL");

        // If not set, return null - let Spring use default datasource from application.yml
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            logger.debug("DATABASE_URL not set - using default datasource configuration");
            return null;
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
        logger.info("✅ Render/Railway deployment detected - Using DATABASE_URL: {}", maskedUrl);

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .build();
    }
}
