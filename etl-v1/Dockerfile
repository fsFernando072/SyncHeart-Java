FROM maven:3.9.9-amazoncorretto-17 AS build

COPY src /app/src
COPY pom.xml /app

WORKDIR /app
RUN mvn clean package

FROM amazoncorretto:17-jre-alpine
COPY --from=build /app/target/etl-v1-1.0-SNAPSHOT.jar /app/app.jar

WORKDIR /app

EXPOSE 8080


