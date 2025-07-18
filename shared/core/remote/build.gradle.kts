import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    alias(libs.plugins.konfig)
    alias(libs.plugins.kotlinAtomic)
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "coreRemote"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)

            implementation(libs.rustore.universalpush.core)
            implementation(libs.androidx.browser)

            implementation(project.dependencies.platform(libs.google.oauth.bom.android))
            implementation(libs.google.oauth.android)
            implementation(libs.google.oauth.credentials.android)
        }
        commonMain.dependencies {
            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:domain"))

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.auth)
            implementation(libs.ktor.websockets)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.kotlin.atomicfu)
            implementation(libs.kotlin.io)
            implementation(libs.networkcheker)
            implementation(libs.settings.core)
            implementation(libs.settings.noargs)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }

    task("testClasses")
}

android {
    namespace = "ru.aleshin.studyassistant.core.remote"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toIntOrNull()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

buildkonfig {
    packageName = "ru.aleshin.studyassistant.core.remote"

    val isDebug = gradle.startParameter.taskNames.any { it.contains("debug", ignoreCase = true) }
    val firebaseProjectId = gradleLocalProperties(rootDir, providers).getProperty("firebaseProjectId")
    val rustoreProjectId = gradleLocalProperties(rootDir, providers).getProperty("rustoreProjectId")
    val rustoreAuthToken = gradleLocalProperties(rootDir, providers).getProperty("rustoreServiceAuthToken")
    val hmsAppId = gradleLocalProperties(rootDir, providers).getProperty("hmsAppId")
    val hmsProjectId = gradleLocalProperties(rootDir, providers).getProperty("hmsProjectId")
    val hmsClientSecret = gradleLocalProperties(rootDir, providers).getProperty("hmsClientSecret")
    val deepSeekKey = gradleLocalProperties(rootDir, providers).getProperty("deepSeekKey")

    defaultConfigs {
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", isDebug.toString())
        buildConfigField(FieldSpec.Type.STRING, "FIREBASE_PROJECT_ID", firebaseProjectId)
        buildConfigField(FieldSpec.Type.STRING, "RUSTORE_PROJECT_ID", rustoreProjectId)
        buildConfigField(FieldSpec.Type.STRING, "RUSTORE_SERVICE_AUTH_TOKEN", rustoreAuthToken)
        buildConfigField(FieldSpec.Type.STRING, "HMS_PROJECT_ID", hmsProjectId)
        buildConfigField(FieldSpec.Type.STRING, "HMS_APP_ID", hmsAppId)
        buildConfigField(FieldSpec.Type.STRING, "HMS_CLIENT_SECRET", hmsClientSecret)
        buildConfigField(FieldSpec.Type.STRING, "DEEP_SEEK_KEY", deepSeekKey)
    }
}