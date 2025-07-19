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
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.konfig) apply false
    alias(libs.plugins.tracer) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.kotlin.atomic) apply false
}