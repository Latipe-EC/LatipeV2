FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/rating-1.0.0-SNAPSHOT.jar rating-1.0.0.jar
EXPOSE 8777
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "rating-1.0.0.jar"]