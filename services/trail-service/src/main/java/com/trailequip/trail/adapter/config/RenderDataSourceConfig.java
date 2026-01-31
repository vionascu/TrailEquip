package com.trailequip.trail.adapter.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Render deployment DataSource configuration.
 * Reads DATABASE_URL environment variable and constructs a proper JDBC URL.
 */
@Configuration
@Profile("render")
public class RenderDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }

        // Render provides: postgresql://user:password@host:port/dbname
        // Convert to JDBC format: jdbc:postgresql://user:password@host:port/dbname?sslmode=require
        String jdbcUrl = databaseUrl;
        if (!databaseUrl.startsWith("jdbc:")) {
            jdbcUrl = "jdbc:" + databaseUrl;
        }
        if (!jdbcUrl.contains("?")) {
            jdbcUrl += "?sslmode=require";
        }

        // DataSource with embedded credentials in URL
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .build();
    }
}
