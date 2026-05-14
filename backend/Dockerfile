FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src/ src/
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=build /app/build/libs/resource-manager-*.jar app.jar

RUN mkdir -p /data && chown -R appuser:appgroup /data
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
