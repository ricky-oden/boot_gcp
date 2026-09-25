FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY backend-api/build.gradle backend-api/build.gradle
COPY openapi openapi
RUN ./gradlew :backend-api:dependencies --no-daemon
COPY backend-api/src backend-api/src
RUN ./gradlew :backend-api:bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/backend-api/build/libs/backend-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
