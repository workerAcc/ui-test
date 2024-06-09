FROM maven:3.9.7-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:22-jdk
WORKDIR /app
COPY --from=build /app/target/ui-test-1.0-SNAPSHOT.jar /app/ui-test.jar
RUN apt-get update && apt-get install -y maven
COPY pom.xml /app
COPY src /app/src
CMD ["mvn", "clean", "test"]