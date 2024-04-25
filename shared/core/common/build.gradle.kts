plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.serialization)
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
            baseName = "coreCommon"
            isStatic = true
            transitiveExport = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.core.ktx)
            api(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            api(libs.kotlin.coroutines)
            api(libs.kotlin.datetime)
            api(libs.kotlin.serialization)
            api(libs.bignumn)

            api(libs.kodein.core)
            implementation(libs.kodein.compose)

            implementation(libs.bundles.firebase)

            implementation(libs.bundles.voyager)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            api(libs.kotlin.test)
        }
    }
    task("testClasses")
}

android {
    namespace = "ru.aleshin.studyassistant.core.common"
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