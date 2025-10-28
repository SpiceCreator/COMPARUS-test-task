package ua.comparus.user;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.comparus.config.database.DataSourceStorage;
import ua.comparus.config.database.SourceTable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

    static PostgreSQLContainer<?> postgres;

    static {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        Properties props = yaml.getObject();

        assert props != null;
        String dbName = props.getProperty("test-task.data-sources[2].url").split("/")[3];
        String user = props.getProperty("test-task.data-sources[2].user");
        String pass = props.getProperty("test-task.data-sources[2].password");

        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName(dbName)
                .withUsername(user)
                .withPassword(pass);
    }

    @LocalServerPort
    private int port;

    @SpyBean
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataSourceStorage dataSourceStorage;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = postgres;

    @BeforeEach
    void setUp() {
        List<SourceTable> sourceTables = dataSourceStorage.getDataSources().stream().map(cfg -> {
            JdbcTemplate jdbcTemplate;

            switch (cfg.getStrategy()) {
                case "h2" -> jdbcTemplate = getH2JdbcTemplate(cfg);
                case "sqlite" -> jdbcTemplate = getSqliteJdbcTemplate(cfg);
                case "postgres" -> jdbcTemplate = getPostgresJdbcTemplate(cfg);
                default -> throw new IllegalArgumentException("Unknown strategy: " + cfg.getStrategy());
            }

            return new SourceTable(cfg, jdbcTemplate);
        }).toList();

        //Override the sourceTables field since we don't know the testcontainers port in advance
        ReflectionTestUtils.setField(userService, "sourceTables", sourceTables);
    }

    @Test
    void testGetAllUsersWithoutParams() {
        String url = "http://localhost:" + port + "/users";
        User[] response = restTemplate.getForObject(url, User[].class);

        assertThat(response).hasSize(6);
    }

    @Test
    void testGetUsersFilteredByUsername() {
        String url = "http://localhost:" + port + "/users?username=user1&username=user3&username=user5";
        User[] response = restTemplate.getForObject(url, User[].class);

        assertThat(response).hasSize(3);
        assertThat(List.of(response)).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user3", "user5");
    }

    @Test
    void testGetUsersFilteredByDataSource() {
        String url = "http://localhost:" + port + "/users?dataSourceName=h2-database";
        User[] response = restTemplate.getForObject(url, User[].class);

        assertThat(response).hasSize(2);
        assertThat(List.of(response)).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void testGetUsersFilteredByName() {
        String url = "http://localhost:" + port + "/users?name=John";
        User[] response = restTemplate.getForObject(url, User[].class);

        assertThat(response).hasSize(2);
        assertThat(List.of(response)).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user6");
    }

    @Test
    void testGetUsersFilteredBySurname() {
        String url = "http://localhost:" + port + "/users?surname=Smith";
        User[] response = restTemplate.getForObject(url, User[].class);

        assertThat(response).hasSize(2);
        assertThat(List.of(response)).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user3", "user4");
    }

    private JdbcTemplate getH2JdbcTemplate(DataSourceStorage.DataSourceConfig config) {
        JdbcTemplate jdbcTemplate;
        try {
            Connection connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + config.getTable() +
                " (" +
                config.getMapping().get("id") + " VARCHAR(50), " +
                config.getMapping().get("username") + " VARCHAR(50), " +
                config.getMapping().get("name") + " VARCHAR(50), " +
                config.getMapping().get("surname") + " VARCHAR(50)" +
                ")");
        jdbcTemplate.execute("DELETE FROM " + config.getTable());
        jdbcTemplate.update("INSERT INTO " + config.getTable() + " VALUES ('1','user1','John','Doe')");
        jdbcTemplate.update("INSERT INTO " + config.getTable() + " VALUES ('2','user2','Jane','Doe')");
        return jdbcTemplate;
    }

    private JdbcTemplate getSqliteJdbcTemplate(DataSourceStorage.DataSourceConfig config) {
        JdbcTemplate jdbcTemplate;
        try {
            Connection connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + config.getTable() +
                " (" +
                config.getMapping().get("username") + " VARCHAR(50), " +
                config.getMapping().get("name") + " VARCHAR(50), " +
                config.getMapping().get("surname") + " VARCHAR(50)" +
                ")");
        jdbcTemplate.execute("DELETE FROM " + config.getTable());
        jdbcTemplate.update("INSERT INTO " + config.getTable() + " VALUES ('user3','Alice','Smith')");
        jdbcTemplate.update("INSERT INTO " + config.getTable() + " VALUES ('user4','Bob','Smith')");
        return jdbcTemplate;
    }

    private JdbcTemplate getPostgresJdbcTemplate(DataSourceStorage.DataSourceConfig config) {
        try {
            Connection connection = DriverManager.getConnection(
                    postgresContainer.getJdbcUrl(),
                    postgresContainer.getUsername(),
                    postgresContainer.getPassword()
            );
            JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + config.getTable() +
                    " (" +
                    config.getMapping().get("id") + " VARCHAR(50) PRIMARY KEY, " +
                    config.getMapping().get("username") + " VARCHAR(50), " +
                    config.getMapping().get("name") + " VARCHAR(50), " +
                    config.getMapping().get("surname") + " VARCHAR(50)" +
                    ")");
            jdbcTemplate.update("DELETE FROM " + config.getTable());
            jdbcTemplate.update("INSERT INTO " + config.getTable() + " VALUES ('3','user5','Carol','King')");
            jdbcTemplate.update("INSERT INTO " + config.getTable() + " VALUES ('4','user6','John','Brown')");
            return jdbcTemplate;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
