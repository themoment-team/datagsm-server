FROM eclipse-temurin:24-jre

EXPOSE 8080

WORKDIR /datagsm-server

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} datagsm-prod-server.jar

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "datagsm-prod-server.jar"]
