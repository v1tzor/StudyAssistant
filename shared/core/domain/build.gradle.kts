plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
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
            baseName = "coreDomain"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:core:common"))
            implementation(libs.bundles.firebase)
        }
    }
}

android {
    namespace = "ru.aleshin.studyassistant.core.domain"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toIntOrNull()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
