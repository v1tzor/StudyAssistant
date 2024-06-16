plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    alias(libs.plugins.sqlitedelight)
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
            baseName = "coreData"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
        }
        commonMain.dependencies {
            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:domain"))

            api(libs.bundles.firebase)
            implementation(libs.sqldelight.core)
            implementation(libs.sqldelight.prmimitiveAdapters)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.logger)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }
    }

    task("testClasses")
}

android {
    namespace = "ru.aleshin.studyassistant.core.data"
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

sqldelight {
    databases {
        create("Database") {
            packageName.set("ru.aleshin.studyassistant.core.data")
        }
    }
}