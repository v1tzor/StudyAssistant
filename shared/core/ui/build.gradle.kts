plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.parcelize)
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
            baseName = "coreUi"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:domain"))

            api(libs.bundles.voyager)

            api(libs.kodein.compose)

            api(compose.ui)
            api(compose.runtime)
            api(compose.material)
            api(compose.material3)
            api(compose.foundation)
            api(compose.material3AdaptiveNavigationSuite)
            api(compose.materialIconsExtended)
            api(compose.components.resources)

            api(libs.google.accompanist)
            api(libs.placeholder)

            api(libs.koalaplot.charts)
            api(libs.bundles.filekit)

            api(libs.bundles.sketch)

            implementation(libs.firebase.storage)
        }
    }

    task("testClasses")
}

android {
    namespace = "ru.aleshin.studyassistant.core.ui"
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