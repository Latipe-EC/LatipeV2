FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/search-1.0.0-SNAPSHOT.jar search-1.0.0.jar
EXPOSE 8092
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "search-1.0.0.jar"]