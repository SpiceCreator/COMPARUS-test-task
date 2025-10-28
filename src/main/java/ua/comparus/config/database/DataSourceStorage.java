package ua.comparus.config.database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "test-task")
public class DataSourceStorage {

    private List<DataSourceConfig> dataSources;

    @Data
    public static class DataSourceConfig {
        private String name;
        private String strategy;
        private String url;
        private String table;
        private String tag;
        private String user;
        private String password;
        private Map<String, String> mapping;
    }
}
