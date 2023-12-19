FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
RUN apk add --no-cache bash
ARG JAR_FILE
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f $HOME/pom.xml clean package
COPY target/rating-1.0.0-SNAPSHOT.jar rating-1.0.0.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "rating-1.0.0.jar"]