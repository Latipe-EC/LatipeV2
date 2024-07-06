# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package

# Package stage
FROM amazoncorretto:17
MAINTAINER Latipe
RUN yum install -y tzdata libstdc++ libc6-compat
ARG JAR_FILE
COPY target/api-gateway-1.0.0-SNAPSHOT.jar api-gateway-1.0.0.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "api-gateway-1.0.0.jar"]