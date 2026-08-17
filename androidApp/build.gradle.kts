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

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.tracer)
}

android {
    namespace = "ru.aleshin.studyassistant.android"
    flavorDimensions += "production"

    val localProperties = gradleLocalProperties(rootDir, providers)

    val yandexTasksBannerId = localProperties.getProperty("studyassistant.ads.tasks.banner.id")
        ?: providers.gradleProperty("studyassistant.ads.tasks.banner.id").orNull.orEmpty()
    val yandexInfoBannerId = localProperties.getProperty("studyassistant.ads.info.banner.id")
        ?: providers.gradleProperty("studyassistant.ads.info.banner.id").orNull.orEmpty()
    val yandexAiRewardedId = localProperties.getProperty("studyassistant.ads.ai.rewarded.id")
        ?: providers.gradleProperty("studyassistant.ads.ai.rewarded.id").orNull.orEmpty()
    val yandexScheduleRewardedId = localProperties.getProperty("studyassistant.ads.schedule.rewarded.id")
        ?: providers.gradleProperty("studyassistant.ads.schedule.rewarded.id").orNull.orEmpty()
    val yandexScheduleAiRewardedId = localProperties.getProperty("studyassistant.ads.ai.schedule.rewarded.id")
        ?: providers.gradleProperty("studyassistant.ads.ai.schedule.rewarded.id").orNull.orEmpty()

    defaultConfig {
        applicationId = libs.versions.applicationId.get()
        minSdk = libs.versions.minSdk.get().toIntOrNull()
        targetSdk = libs.versions.targetSdk.get().toIntOrNull()
        compileSdk = libs.versions.compileSdk.get().toIntOrNull()

        versionCode = libs.versions.version.code.get().toIntOrNull()
        versionName = libs.versions.version.name.get()

        testInstrumentationRunner = libs.versions.testInstrumentationRunner.get()
        vectorDrawables.useSupportLibrary = true
        val myTrackerKey = localProperties.getProperty("myTrackerKey")

        buildConfigField("String", "MY_TRACKER_KEY", "\"$myTrackerKey\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("storeFile"))
            storePassword = localProperties.getProperty("storePassword")
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")

            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
        getByName("debug") {
            storeFile = file(localProperties.getProperty("storeFile"))
            storePassword = localProperties.getProperty("storePassword")
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            buildConfigField("String", "YANDEX_TASKS_BANNER_ID", "\"$yandexTasksBannerId\"")
            buildConfigField("String", "YANDEX_INFO_BANNER_ID", "\"$yandexInfoBannerId\"")
            buildConfigField("String", "YANDEX_AI_REWARDED_ID", "\"$yandexAiRewardedId\"")
            buildConfigField("String", "YANDEX_SCHEDULE_REWARDED_ID", "\"$yandexScheduleRewardedId\"")
            buildConfigField("String", "YANDEX_SCHEDULE_AI_REWARDED_ID", "\"$yandexScheduleAiRewardedId\"")
        }
        getByName("debug") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "YANDEX_TASKS_BANNER_ID", "\"$yandexTasksBannerId\"")
            buildConfigField("String", "YANDEX_INFO_BANNER_ID", "\"$yandexInfoBannerId\"")
            buildConfigField("String", "YANDEX_AI_REWARDED_ID", "\"$yandexAiRewardedId\"")
            buildConfigField("String", "YANDEX_SCHEDULE_REWARDED_ID", "\"$yandexScheduleRewardedId\"")
            buildConfigField("String", "YANDEX_SCHEDULE_AI_REWARDED_ID", "\"$yandexScheduleAiRewardedId\"")
        }
    }

    productFlavors {
        create("huawei") {
            dimension = "production"
        }
        create("github") {
            dimension = "production"
        }
        create("rustore") {
            dimension = "production"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    androidResources {
        localeFilters += listOf("en", "ru")
        ignoreAssetsPattern = "service-account-file.json"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    packaging {
        resources {
            resources.pickFirsts.add("META-INF/INDEX.LIST")
            resources.merges.add("META-INF/DEPENDENCIES")
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val rustoreImplementation = "rustoreImplementation"

dependencies {
    implementation(project(":shared"))
    implementation(project(":widget"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.workmanager.ktx)
    implementation(libs.androidx.workmanager)
    implementation(libs.compose.material)

    implementation(libs.sqldelight.core)
    implementation(libs.sqldelight.android)

    implementation(libs.kodein.android)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)

    implementation(platform(libs.tracer.bom))
    implementation(libs.bundles.tracer)
    implementation(libs.mytracker.core)

    rustoreImplementation(libs.rustore.review)
    rustoreImplementation(libs.rustore.updates)
}

tracer {
    create("defaultConfig") {
        val localProperties = gradleLocalProperties(rootDir, providers)
        pluginToken = localProperties.getProperty("tracerPluginToken")
        appToken = localProperties.getProperty("tracerAppToken")
        uploadMapping = true
        uploadNativeSymbols = true
    }
}
