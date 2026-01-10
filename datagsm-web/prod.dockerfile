FROM eclipse-temurin:25-jre

EXPOSE 8080

WORKDIR /datagsm-server

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} datagsm-web.jar

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "datagsm-web.jar"]
