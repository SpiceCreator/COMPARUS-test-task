ALTER USER sa SET PASSWORD 'secret';

CREATE TABLE IF NOT EXISTS users (
     user_id VARCHAR(36) PRIMARY KEY,
     login VARCHAR(50),
     first_name VARCHAR(50),
     last_name VARCHAR(50)
);

INSERT INTO users (user_id, login, first_name, last_name) VALUES
    ('1', 'h2_user', 'H2', 'Tester');