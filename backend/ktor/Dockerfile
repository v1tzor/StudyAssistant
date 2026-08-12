# syntax=docker/dockerfile:1.7@sha256:a57df69d0ea827fb7266491f2813635de6f17269be881f696fbfdf2d83dda33e

FROM eclipse-temurin:17-jdk-jammy@sha256:29467857e8bde40ab1f7befecbda0ea764b95afec1cc7f89aa90f7a766577e19 AS builder

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
COPY src ./src

RUN chmod 0755 ./gradlew && \
    ./gradlew installDist --no-daemon --no-build-cache --console=plain

FROM eclipse-temurin:17-jre-jammy@sha256:89e68b9bb83713510b63e2059a415792a7fc77e14b739a7d7ede97f6d9ca2c38

RUN groupadd --gid 10001 studyassistant && \
    useradd --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin studyassistant

WORKDIR /app

COPY --from=builder --chown=10001:10001 \
    /workspace/build/install/studyassistant-backend/ ./

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["/app/bin/studyassistant-backend"]
