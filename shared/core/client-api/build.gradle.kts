import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.atomic)
    alias(libs.plugins.konfig)
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
            baseName = "coreApi"
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
            implementation(libs.ktor.client.content.negotiation)
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
    namespace = "ru.aleshin.studyassistant.core.api"
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
    packageName = "ru.aleshin.studyassistant.core.api"

    val isDebug = gradle.startParameter.taskNames.any { it.contains("debug", ignoreCase = true) }

    defaultConfigs {
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", isDebug.toString())
    }
}