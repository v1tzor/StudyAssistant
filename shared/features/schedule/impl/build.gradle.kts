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
            baseName = "scheduleImpl"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:features:schedule:api"))

            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:data"))
            implementation(project(":shared:core:domain"))
            implementation(project(":shared:core:ui"))

            implementation(compose.components.resources)
            implementation(libs.logger)
        }
    }
}

android {
    namespace = "ru.aleshin.studyassistant.schedule.impl"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/composeResources")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toIntOrNull()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}