import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    alias(libs.plugins.konfig)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

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

            implementation(project.dependencies.platform(libs.rustore.bom))
            implementation(libs.rustore.universalpush.core)

            implementation(project.dependencies.platform(libs.google.oauth.bom.android))
            implementation(libs.google.oauth.android)
            implementation(libs.google.oauth.credentials.android)
        }
        commonMain.dependencies {
            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:domain"))

            api(libs.bundles.firebase)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlin.serialization.json)
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

    val firebaseProjectId = gradleLocalProperties(rootDir, providers).getProperty("firebaseProjectId")
    val rustoreProjectId = gradleLocalProperties(rootDir, providers).getProperty("rustoreProjectId")
    val rustoreAuthToken = gradleLocalProperties(rootDir, providers).getProperty("rustoreServiceAuthToken")

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "FIREBASE_PROJECT_ID", firebaseProjectId)
        buildConfigField(FieldSpec.Type.STRING, "RUSTORE_PROJECT_ID", rustoreProjectId)
        buildConfigField(FieldSpec.Type.STRING, "RUSTORE_SERVICE_AUTH_TOKEN", rustoreAuthToken)
    }
}