CREATE TABLE IF NOT EXISTS user_table (
     ldap_login TEXT PRIMARY KEY,
     name TEXT,
     surname TEXT
);

INSERT INTO user_table (ldap_login, name, surname) VALUES
    ('sqlite_user', 'SQLite', 'Tester') ON CONFLICT DO NOTHING;