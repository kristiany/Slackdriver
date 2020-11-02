FROM openjdk:8-jre-slim
COPY ./build/libs/slackdriver-0.0.2-all.jar /app/
WORKDIR /app
ENTRYPOINT ["java","-jar","slackdriver-0.0.2-all.jar"]
