FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
RUN apk add --no-cache bash
ADD target/discovery-server-0.0.1-SNAPSHOT.jar discovery-server-0.0.1-SNAPSHOT.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh
ENTRYPOINT ["java", "-jar", "/discovery-server-0.0.1-SNAPSHOT.jar"]