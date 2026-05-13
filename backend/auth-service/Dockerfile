FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/auth-service-*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
