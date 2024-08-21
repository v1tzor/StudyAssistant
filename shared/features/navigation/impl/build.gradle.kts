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
            baseName = "navigationImpl"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:features:navigation:api"))
            implementation(project(":shared:features:schedule:api"))
            implementation(project(":shared:features:tasks:api"))
            implementation(project(":shared:features:info:api"))
            implementation(project(":shared:features:profile:api"))
//            // Implementation features impl module for get access to their DIHolder
//            // This module is a shared submodule, so it is forbidden to refer to it from other modules except shared
//            implementation(project(":shared:features:schedule:impl"))
//            implementation(project(":shared:features:info:impl"))
//            implementation(project(":shared:features:tasks:impl"))
//            implementation(project(":shared:features:profile:impl"))

            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:ui"))

            implementation(compose.components.resources)
        }
    }

    task("testClasses")
}

android {
    namespace = "ru.aleshin.studyassistant.navigation.impl"
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