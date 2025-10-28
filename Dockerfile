# ==== BUILD STAGE ====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /test-task
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ==== RUNTIME STAGE ====
FROM eclipse-temurin:17-jre
WORKDIR /test-task
COPY --from=build /test-task/target/test-task.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]