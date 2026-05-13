FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/api-gateway-*.jar app.jar
RUN apk add --no-cache curl
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=30s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
