FROM openjdk:11-jre-slim-buster

COPY ./deploy/run.sh run.sh
COPY ./target/aircraft-maintenance-planning-1.0-SNAPSHOT.jar application.jar
RUN chmod +x /run.sh

CMD /run.sh
