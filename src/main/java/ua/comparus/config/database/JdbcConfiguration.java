package ua.comparus.config.database;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcConfiguration {

    @Getter
    enum DataSourceType {
        H2("h2", "org.h2.Driver"),
        SQLITE("sqlite", "org.sqlite.JDBC"),
        POSTGRESQL("postgres", "org.postgresql.Driver"),
        MYSQL("mysql", "com.mysql.jdbc.Driver"),
        ORACLE("oracle", "oracle.jdbc.OracleDriver");

        private final String strategyType;
        private final String driverClassName;

        public static String getDriverClassName(String strategyType) {
            String loweredStrategyType = strategyType.toLowerCase();
            for (DataSourceType type : DataSourceType.values()) {
                if (type.getStrategyType().equals(loweredStrategyType)) {
                    return type.driverClassName;
                }
            }
            throw new IllegalArgumentException("Unsupported data source type: " + strategyType);
        }

        DataSourceType(String strategyType, String driverClassName) {
            this.strategyType = strategyType;
            this.driverClassName = driverClassName;
        }
    }

    private final DataSourceStorage dataSourceStorage;

    /**
     * Create a map of JdbcTemplate for each data source
     */
    @Bean
    public Map<String, JdbcTemplate> jdbcTemplates() {
        Map<String, JdbcTemplate> map = new HashMap<>();
        for (DataSourceStorage.DataSourceConfig cfg : dataSourceStorage.getDataSources()) {
            String driverClassName = DataSourceType.getDriverClassName(cfg.getStrategy());

            // Force load the JDBC driver to fix fat-jar classloader issue
            try {
                Class.forName(driverClassName);
                log.info("Loaded JDBC driver: {}", driverClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load JDBC driver: " + driverClassName, e);
            }

            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setUrl(cfg.getUrl());
            ds.setUsername(cfg.getUser());
            ds.setPassword(cfg.getPassword());
            ds.setDriverClassName(driverClassName);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
            map.put(cfg.getName(), jdbcTemplate);
            log.info("Registered JdbcTemplate for datasource: {}", cfg.getName());
        }
        return map;
    }
}
