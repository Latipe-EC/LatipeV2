FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/user-1.0.0-SNAPSHOT.jar user-1.0.0.jar
EXPOSE 8888
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "user-1.0.0.jar"]