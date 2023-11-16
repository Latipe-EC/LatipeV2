FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/paypal-1.0.0-SNAPSHOT.jar paypal-1.0.0.jar
EXPOSE 8083
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "paypal-1.0.0.jar"]