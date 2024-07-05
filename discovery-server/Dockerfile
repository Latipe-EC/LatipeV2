FROM amazoncorretto:17
MAINTAINER Latipe
RUN yum install -y tzdata libstdc++ libc6-compat
ADD target/discovery-server-1.0.0-SNAPSHOT.jar discovery-server-1.0.0.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh
ENTRYPOINT ["java", "-jar", "/discovery-server-1.0.0.jar"]