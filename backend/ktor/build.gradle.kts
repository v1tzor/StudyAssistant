/*
 * Copyright 2026 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:13.3.0")
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.flyway)

    application
}

group = "ru.aleshin.studyassistant"
version = "1.0.0"

application {
    mainClass.set(
        "ru.aleshin.studyassistant.backend.ApplicationKt",
    )
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.serialization.json)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.di)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.body.limit)
    implementation(libs.ktor.server.forwarded.header)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    implementation(libs.hikari)
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    runtimeOnly(libs.logback)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.mock)
    testImplementation(kotlin("test"))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
}

flyway {
    locations = arrayOf("filesystem:src/main/resources/db/migration")

    validateMigrationNaming = true
    cleanDisabled = true
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runCleanup") {
    group = "maintenance"
    description = "Removes expired temporary sharing data"

    classpath = sourceSets["main"].runtimeClasspath

    mainClass.set("ru.aleshin.studyassistant.backend.maintenance.CleanupMainKt")
}

tasks.register<JavaExec>("runMigrations") {
    group = "maintenance"
    description = "Validates and applies production database migrations"

    classpath = sourceSets["main"].runtimeClasspath

    mainClass.set("ru.aleshin.studyassistant.backend.maintenance.MigrationMainKt")
}
