# syntax=docker/dockerfile:1.7@sha256:a57df69d0ea827fb7266491f2813635de6f17269be881f696fbfdf2d83dda33e

FROM eclipse-temurin:25-jdk-jammy@sha256:89565961a318534f01c971c7b1d030e60713c66995b887c94010cef938dbc53e AS builder

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
COPY src ./src

RUN chmod 0755 ./gradlew && \
    ./gradlew installDist --no-daemon --no-build-cache --console=plain

FROM eclipse-temurin:25-jre-jammy@sha256:10c251954d0bfe1a59ba93505f8c628d755919412400aa98685764c9353605d6

RUN groupadd --gid 10001 studyassistant && \
    useradd --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin studyassistant

WORKDIR /app

COPY --from=builder --chown=10001:10001 \
    /workspace/build/install/studyassistant-backend/ ./

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["/app/bin/studyassistant-backend"]
