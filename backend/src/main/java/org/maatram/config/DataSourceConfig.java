package org.maatram.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/maatram}")
    private String pgUrl;

    @Value("${spring.datasource.username:postgres}")
    private String pgUser;

    @Value("${spring.datasource.password:postgres}")
    private String pgPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Checking PostgreSQL database connection at {}...", pgUrl);
        boolean pgAvailable = false;
        try {
            DriverManager.setLoginTimeout(3);
            try (Connection conn = DriverManager.getConnection(pgUrl, pgUser, pgPassword)) {
                if (conn.isValid(3)) {
                    pgAvailable = true;
                }
            }
        } catch (Exception e) {
            log.warn("PostgreSQL connection check failed: {}", e.getMessage());
        }

        if (pgAvailable) {
            log.info("================================================================================");
            log.info(" CONNECTED TO POSTGRESQL DATABASE: {}", pgUrl);
            log.info("================================================================================");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(pgUrl);
            config.setUsername(pgUser);
            config.setPassword(pgPassword);
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(5000);
            return new HikariDataSource(config);
        } else {
            log.warn("================================================================================");
            log.warn(" NOTICE: PostgreSQL is not reachable with credentials (user: '{}').", pgUser);
            log.warn(" Default credentials were used or the credentials in .env do not match.");
            log.warn(" ");
            log.warn(" To connect to your PostgreSQL:");
            log.warn("   1. Open 'backend/.env'");
            log.warn("   2. Update DB_PASSWORD with your real PostgreSQL password");
            log.warn("   3. Restart the backend");
            log.warn(" ");
            log.warn(" Running with in-memory PostgreSQL-compatible H2 database for now");
            log.warn(" so you can test and use the application immediately!");
            log.warn("================================================================================");

            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl("jdbc:h2:mem:maatram;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setMaximumPoolSize(10);
            return new HikariDataSource(h2Config);
        }
    }
}
