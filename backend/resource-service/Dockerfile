FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/resource-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
