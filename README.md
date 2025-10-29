# COMPARUS-test-task

Service is fetching data from 3 databases. (h2, sqlite and postgresql)
All of them are run and initialized in docker-compose.yml

You can start it with: `docker-compose up` or `docker-compose up -d` (as a daemon)

After initialization was completed, you can access it on http://localhost:8080/users in your browser or using postman.

Initially, each database has one record in each.
Application doesn't provide any endpoint to insert new records or update existing ones.
You can change data in each database manually after run or by updating init.sql files in ./src/main/resources/db

You can access databases:

h2: jdbc:h2:tcp://localhost:9092/testdb
user: sa
password: secret

postgresql: jdbc:postgresql://localhost:5432/testdb
user: testuser
password: testpass

sqlite:
Run in terminal:
docker exec -it sqlite-db sh
sqlite3 /data/sqlite/test.db
Here you can run any sql queries.

You can stop it with: `ctrl+c` or `docker-compose down` (for daemon)

# Project structure
## Resources
- /src/main/resources/application.yml - has all database info
- /src/main/resources/db - Docker images and sql scripts for initializing databases
- /src/main/resources/openapi.yml - openapi contract

## Main
- ua.comparus.config.database package is in charge of parsing database info from application.yml and creating a connection pool.
- ua.comparus.domain.user package is in charge of retrieving and mapping data from database to User object. 
It also has the UserController that implements an openapi.yml contract with the code-first approach.
There is also additional filter parameters were added to controller that allows you to perform search in particular database or with specific user info.

## Tests
- ua.comparus.domain.user.UserControllerIntegrationTest - has several tests for UserController. 
It uses Testcontainers to start a postgres database in a docker container.