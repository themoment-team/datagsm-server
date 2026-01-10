FROM eclipse-temurin:25-jre

EXPOSE 8081

WORKDIR /datagsm-server

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} datagsm-authorization.jar

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "datagsm-authorization.jar"]
