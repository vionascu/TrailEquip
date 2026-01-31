package com.trailequip.trail.adapter.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Render/Railway deployment DataSource configuration.
 *
 * When DATABASE_URL environment variable is present (Render/Railway):
 * - Creates DataSource using DATABASE_URL
 * - Converts PostgreSQL URL to JDBC format
 * - Adds SSL requirement
 *
 * When DATABASE_URL is missing (local development):
 * - Returns null, letting Spring boot use default datasource from application.yml
 */
@Configuration
public class RenderDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        // If DATABASE_URL not set, return null (let Spring use default config)
        // This bean won't override application.yml datasource config
        if (databaseUrl == null || databaseUrl.isEmpty()) {
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

        System.out.println("🔌 Using DATABASE_URL for datasource: " + jdbcUrl.replaceAll(":[^@]*@", ":***@"));

        // DataSource with embedded credentials in URL
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .build();
    }
}
