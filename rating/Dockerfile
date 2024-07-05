FROM amazoncorretto:17
MAINTAINER Latipe
RUN yum install -y tzdata libstdc++ libc6-compat
ARG JAR_FILE
COPY target/rating-1.0.0-SNAPSHOT.jar rating-1.0.0.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "rating-1.0.0.jar"]