FROM java:openjdk-8-jre-alpine

LABEL version="0.2.0"

MAINTAINER Sim<8966188@qq.com>

ENV ACTIVE test
ENV JAVA_OPTS -Xms1024m -Xmx1024m -Xmn320m

ADD target/acku-schedule-0.2.0.jar /data/deploy/acku-schedule.jar


CMD ["mkdir -p /data/logs"]
VOLUME /data/logs /data/logs

EXPOSE 80

ENTRYPOINT ["java","-Dspring.profiles.active=${ACTIVE}","-jar", "/data/deploy/acku-schedule.jar"]