FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
RUN apk add --no-cache bash
ARG JAR_FILE
COPY target/payment-1.0.0-SNAPSHOT.jar payment-1.0.0.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "payment-1.0.0.jar"]