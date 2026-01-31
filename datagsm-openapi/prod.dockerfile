FROM eclipse-temurin:25-jre

EXPOSE 8082

WORKDIR /datagsm-server

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} datagsm-openapi.jar

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=prod -jar datagsm-openapi.jar"]
