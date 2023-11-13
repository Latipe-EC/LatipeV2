FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/auth-1.0.0-SNAPSHOT.jar auth-1.0.0.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "auth-1.0.0.jar"]