package ua.comparus.user;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.comparus.config.database.SourceTable;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final List<SourceTable> sourceTables;
    private final UserRepository userRepository;

    UserService(@Qualifier("user") List<SourceTable> sourceTables, UserRepository userRepository) {
        this.sourceTables = sourceTables;
        this.userRepository = userRepository;
    }

    public List<User> findAll(@Nullable List<String> dataSourceList,  @Nonnull Map<String, List<String>> userDataParameters) {
        return sourceTables.stream()
                .filter(table -> dataSourceList == null || dataSourceList.contains(table.getDataSourceConfig().getName()))
                .map(table -> userRepository.findAllFromDataSource(table, userDataParameters))
                .flatMap(List::stream)
                .toList();
    }
}
