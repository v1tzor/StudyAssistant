# syntax=docker/dockerfile:1.7@sha256:a57df69d0ea827fb7266491f2813635de6f17269be881f696fbfdf2d83dda33e

FROM eclipse-temurin:25-jdk-jammy@sha256:f122992af75e61d87892f8a37c60f7cfa498b18748c1c9f8563da9a3b1893278 AS builder

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
COPY src ./src

RUN chmod 0755 ./gradlew && \
    ./gradlew installDist --no-daemon --no-build-cache --console=plain

FROM eclipse-temurin:25-jre-jammy@sha256:5bd5dbe00f40ea149de434a75029713765a2912cfc1fd770cc7c7aff007384ea

RUN groupadd --gid 10001 studyassistant && \
    useradd --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin studyassistant

WORKDIR /app

COPY --from=builder --chown=10001:10001 \
    /workspace/build/install/studyassistant-backend/ ./

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["/app/bin/studyassistant-backend"]
