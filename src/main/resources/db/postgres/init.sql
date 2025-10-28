CREATE TABLE IF NOT EXISTS postgres_users (
     id VARCHAR(36) PRIMARY KEY,
     username VARCHAR(50),
     name VARCHAR(50),
     surname VARCHAR(50)
);

INSERT INTO postgres_users (id, username, name, surname) VALUES
    ('1', 'pg_user', 'Postgres', 'Tester') ON CONFLICT DO NOTHING;