package ua.comparus.config.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceTable {

    private DataSourceStorage.DataSourceConfig dataSourceConfig;
    private JdbcTemplate jdbcTemplate;
}
