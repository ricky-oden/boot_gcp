FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY batch/build.gradle batch/build.gradle
RUN ./gradlew :batch:dependencies --no-daemon
COPY batch/src batch/src
RUN ./gradlew :batch:bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/batch/build/libs/batch-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
