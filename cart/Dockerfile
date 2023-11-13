FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/cart-1.0.0-SNAPSHOT.jar cart-1.0.0.jar
EXPOSE 7650
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "cart-1.0.0.jar"]