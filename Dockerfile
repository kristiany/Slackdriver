FROM openjdk:8-jre-slim
COPY ./build/libs/gerror-0.0.1-all.jar /app/
WORKDIR /app
ENTRYPOINT ["java","-jar","gerror-0.0.1-all.jar"]
