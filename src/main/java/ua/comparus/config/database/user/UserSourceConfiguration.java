package ua.comparus.config.database.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.comparus.config.database.DataSourceStorage;
import ua.comparus.config.database.SourceTable;

import java.util.List;
import java.util.Map;

@Configuration
public class UserSourceConfiguration {

    private final DataSourceStorage dataSourceStorage;
    private final Map<String, JdbcTemplate> jdbcTemplates;

    UserSourceConfiguration(DataSourceStorage dataSourceStorage, @Qualifier("jdbcTemplates") Map<String, JdbcTemplate> jdbcTemplates) {
        this.dataSourceStorage = dataSourceStorage;
        this.jdbcTemplates = jdbcTemplates;
    }

    @Bean
    @Qualifier("user")
    public List<SourceTable> userSourceTables() {
        return dataSourceStorage.getDataSources().stream()
                .filter(source -> source.getTag().equals("user"))
                .map(source -> new SourceTable(source, jdbcTemplates.get(source.getName())))
                .toList();
    }
}
