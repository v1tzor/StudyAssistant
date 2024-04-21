import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.libsDirectory

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
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
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:features:navigation:api"))
            implementation(project(":shared:features:navigation:impl"))
            implementation(project(":shared:features:preview:api"))
            implementation(project(":shared:features:preview:impl"))
            implementation(project(":shared:features:auth:api"))
            implementation(project(":shared:features:auth:impl"))
            implementation(project(":shared:features:schedule:api"))
            implementation(project(":shared:features:schedule:impl"))

            implementation(project(":shared:core:domain"))
            implementation(project(":shared:core:data"))
            api(project(":shared:core:common"))
            api(project(":shared:core:ui"))

            implementation(libs.bundles.firebase)
            implementation(libs.logger)
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