FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY target/*.jar app.jar
RUN mkdir -p /data

ENTRYPOINT ["java","-jar","/app/app.jar"]
EXPOSE 8080
