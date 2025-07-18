buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven") }
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
    dependencies {
        classpath(libs.androidx.tools)
        classpath(libs.hms.agcp)
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.sqlitedelight) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.konfig) apply false
    alias(libs.plugins.tracer) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.kotlinAtomic) apply false
}