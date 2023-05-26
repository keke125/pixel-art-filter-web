FROM eclipse-temurin:17-jdk
MAINTAINER admin@keke125.com

COPY opencv/docker-linux-install.sh /app/docker-linux-install.sh

RUN ["chmod", "+x", "/app/docker-linux-install.sh"]
RUN /app/docker-linux-install.sh
COPY target/*.jar /app/pafw.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djava.library.path=/app", "-jar", "/app/pafw.jar"]
