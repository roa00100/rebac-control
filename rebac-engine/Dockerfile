# syntax=docker/dockerfile:1
#
# 빌드 컨텍스트는 저장소 루트(.)여야 합니다. 예: docker compose up --build
# 또는: docker build -f rebac-engine/Dockerfile -t rebac-engine .

FROM gradle:8.12.1-jdk21 AS build
WORKDIR /home/gradle/project
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY rebac-core ./rebac-core
COPY rebac-openfga-adapter ./rebac-openfga-adapter
COPY rebac-engine ./rebac-engine
# 런타임 JAR에는 테스트 코드가 들어가지 않지만, 이미지 빌드 시점에 Gradle 테스트를 돌려 실패하면 빌드가 중단됩니다.
RUN gradle test --no-daemon
RUN gradle :rebac-engine:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/project/rebac-engine/build/libs/rebac-engine.jar /app/app.jar
EXPOSE 21001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
