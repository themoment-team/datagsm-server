FROM eclipse-temurin:25-jre

EXPOSE 8082

WORKDIR /datagsm-server

# Bazel-built layered runtime classpath (jars under lib/), run with `java -cp` — avoids the
# Spring Data multi-store collision a merged fat-jar would cause.
COPY lib/ lib/

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=stage -cp 'lib/*' team.themoment.datagsm.openapi.DatagsmResourceApplicationKt"]
