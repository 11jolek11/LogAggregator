FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
LABEL authors="jolek"

WORKDIR /app

COPY mvn* .

COPY pom.xml .
RUN ["./mvnw", "dependecies:resolve"]


FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar .

EXPOSE 3000

ENTRYPOINT ["java", "-jar", ""]