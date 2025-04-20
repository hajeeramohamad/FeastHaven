# Build stage using Maven and JDK 21
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage using JDK 21
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/FeastHaven-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/FeastHaven-0.0.1-SNAPSHOT.jar"]
