package com.trailequip.trail.infrastructure.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Validates application startup configuration and dependencies.
 * Runs immediately after Spring context initialization.
 * Fails fast if any critical validation fails.
 */
@Slf4j
@Component
public class StartupValidator implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${overpass.timeout:60000}")
    private int overpassTimeout;

    @Value("${overpass.rate-limit:3000}")
    private int overpassRateLimit;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("Starting TrailEquip Application");
        log.info("========================================");

        try {
            validateEnvironmentVariables();
            validateDatabaseConnection();
            validatePostGISExtension();
            validateDatabaseSchema();
            validateConfiguration();

            log.info("========================================");
            log.info("✓ All startup validations passed");
            log.info("✓ Application is ready");
            log.info("========================================");
        } catch (Exception e) {
            log.error("✗ Startup validation failed", e);
            log.error("Application will not start");
            throw new RuntimeException("Startup validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate all required environment variables are set.
     */
    private void validateEnvironmentVariables() {
        log.info("Validating environment variables...");

        String[] requiredVars = {
            "DATABASE_URL",
            "POSTGRES_DB",
            "POSTGRES_USER",
            "POSTGRES_PASSWORD"
        };

        for (String var : requiredVars) {
            String value = System.getenv(var);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalStateException("Required environment variable not set: " + var);
            }
            log.debug("  ✓ {} set", var);
        }

        log.info("✓ Environment variables validated");
    }

    /**
     * Validate PostgreSQL connection is working.
     */
    private void validateDatabaseConnection() {
        log.info("Validating PostgreSQL connection...");

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new RuntimeException("Database connection is closed");
            }

            log.debug("  ✓ Connected to PostgreSQL");
            log.debug("  ✓ Database: {}", conn.getCatalog());
            log.debug("  ✓ User: {}", conn.getMetaData().getUserName());

            log.info("✓ PostgreSQL connection validated");
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to PostgreSQL: " + e.getMessage(), e);
        }
    }

    /**
     * Validate PostGIS extension is installed and active.
     */
    private void validatePostGISExtension() {
        log.info("Validating PostGIS extension...");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT PostGIS_version();");
            if (rs.next()) {
                String version = rs.getString(1);
                log.debug("  ✓ PostGIS version: {}", version);
                log.info("✓ PostGIS extension validated");
            } else {
                throw new RuntimeException("PostGIS extension not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("PostGIS validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate database schema exists and tables are created.
     */
    private void validateDatabaseSchema() {
        log.info("Validating database schema...");

        String[] requiredTables = {
            "trails",
            "trail_markings",
            "trail_waypoints",
            "trail_segments"
        };

        try (Connection conn = dataSource.getConnection()) {
            for (String table : requiredTables) {
                if (!tableExists(conn, table)) {
                    throw new RuntimeException("Required table not found: " + table);
                }
                log.debug("  ✓ Table exists: {}", table);
            }

            log.info("✓ Database schema validated");
        } catch (Exception e) {
            throw new RuntimeException("Schema validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a table exists in the database.
     */
    private boolean tableExists(Connection conn, String tableName) throws Exception {
        ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
        return rs.next();
    }

    /**
     * Validate application configuration values.
     */
    private void validateConfiguration() {
        log.info("Validating application configuration...");

        // Validate Overpass API settings
        if (overpassTimeout <= 0) {
            throw new IllegalArgumentException("OVERPASS_TIMEOUT must be positive: " + overpassTimeout);
        }
        log.debug("  ✓ Overpass timeout: {}ms", overpassTimeout);

        if (overpassRateLimit < 2000) {
            log.warn("⚠ OVERPASS_RATE_LIMIT < 2000ms may violate Overpass API terms");
        }
        log.debug("  ✓ Overpass rate limit: {}ms", overpassRateLimit);

        // Validate datasource URL
        if (datasourceUrl == null || datasourceUrl.isEmpty()) {
            throw new IllegalArgumentException("DATABASE_URL not configured");
        }
        log.debug("  ✓ Database URL configured");

        // Validate datasource username
        if (datasourceUsername == null || datasourceUsername.isEmpty()) {
            throw new IllegalArgumentException("POSTGRES_USER not configured");
        }
        log.debug("  ✓ Database user configured");

        log.info("✓ Configuration validated");
    }
}
