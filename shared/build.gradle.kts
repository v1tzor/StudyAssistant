import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.parcelize)
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
            baseName = "shared"
            isStatic = true
            binaryOption("bundleId", "ru.aleshin.studyassistant.shared")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.rustore.universalpush.core)
        }
        commonMain.dependencies {
            implementation(project(":shared:features:navigation:api"))
            implementation(project(":shared:features:navigation:impl"))
            implementation(project(":shared:features:preview:api"))
            implementation(project(":shared:features:preview:impl"))
            implementation(project(":shared:features:auth:api"))
            implementation(project(":shared:features:auth:impl"))
            implementation(project(":shared:features:schedule:api"))
            implementation(project(":shared:features:schedule:impl"))
            implementation(project(":shared:features:tasks:api"))
            implementation(project(":shared:features:tasks:impl"))
            implementation(project(":shared:features:info:api"))
            implementation(project(":shared:features:info:impl"))
            implementation(project(":shared:features:profile:api"))
            implementation(project(":shared:features:profile:impl"))
            implementation(project(":shared:features:settings:api"))
            implementation(project(":shared:features:settings:impl"))
            implementation(project(":shared:features:users:api"))
            implementation(project(":shared:features:users:impl"))
            implementation(project(":shared:features:editor:api"))
            implementation(project(":shared:features:editor:impl"))
            implementation(project(":shared:features:billing:api"))
            implementation(project(":shared:features:billing:impl"))

            api(project(":shared:core:common"))
            api(project(":shared:core:ui"))
            api(project(":shared:core:domain"))
            api(project(":shared:core:data"))
            api(project(":shared:core:database"))
            api(project(":shared:core:remote"))

            implementation(compose.components.resources)
            implementation(libs.firebase.firestore)
            implementation(libs.google.auth)
        }
        iosMain.dependencies {
            implementation(libs.bundles.firebase)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    task("testClasses")
}

android {
    namespace = "ru.aleshin.studyassistant.shared"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toIntOrNull()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

buildkonfig {
    packageName = "ru.aleshin.studyassistant"

    val webClientId: String = gradleLocalProperties(rootDir, providers).getProperty("web_client_id")

    require(webClientId.isNotEmpty()) {
        "Enter your Google server's client ID (not your Android client ID) in local.properties"
    }

    defaultConfigs {
        buildConfigField(STRING, "WEB_CLIENT_ID", webClientId)
    }
}