package ua.comparus.user;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.comparus.config.database.SourceTable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepository {

    public List<User> findAllFromDataSource(@Nonnull SourceTable sourceTable, @Nonnull Map<String, List<String>> parameters) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + sourceTable.getDataSourceConfig().getTable() + " WHERE 1=1");
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sql.append(" AND ")
                    .append(sourceTable.getDataSourceConfig().getMapping().get(entry.getKey()))
                    .append(" IN (")
                    .append( entry.getValue().stream().map(value -> "'" + value + "'").collect(Collectors.joining(",")))
                    .append(")");
            }
        }
        return getQueryResult(sourceTable, sql.toString());
    }

    private List<User> getQueryResult(SourceTable sourceTable, String query) {
        if (sourceTable.getJdbcTemplate() == null) throw new IllegalArgumentException("Unknown datasource: " + sourceTable.getDataSourceConfig().getName());
        return sourceTable.getJdbcTemplate().queryForList(query).stream()
                .map(row -> new User(
                    row.get(sourceTable.getDataSourceConfig().getMapping().get("id")).toString(),
                    row.get(sourceTable.getDataSourceConfig().getMapping().get("username")).toString(),
                    row.get(sourceTable.getDataSourceConfig().getMapping().get("name")).toString(),
                    row.get(sourceTable.getDataSourceConfig().getMapping().get("surname")).toString()
                )).toList();
    }
}
