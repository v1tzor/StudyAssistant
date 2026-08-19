# syntax=docker/dockerfile:1.7@sha256:a57df69d0ea827fb7266491f2813635de6f17269be881f696fbfdf2d83dda33e

FROM eclipse-temurin:22-jdk-jammy@sha256:d8e6ba486df17bf758888d2b1b608133d1eedca8daf69d3fc6bf78d8be81e07e AS builder

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
COPY src ./src

RUN chmod 0755 ./gradlew && \
    ./gradlew installDist --no-daemon --no-build-cache --console=plain

FROM eclipse-temurin:22-jre-jammy@sha256:dbcae8b5dd4d63f81739a538ec2c09797735f04a21d814f9071b62f018326043

RUN groupadd --gid 10001 studyassistant && \
    useradd --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin studyassistant

WORKDIR /app

COPY --from=builder --chown=10001:10001 \
    /workspace/build/install/studyassistant-backend/ ./

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["/app/bin/studyassistant-backend"]
