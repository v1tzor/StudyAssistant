plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
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
            baseName = "shared"
            isStatic = true
            binaryOption("bundleId", "ru.aleshin.studyassistant.shared")
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
            implementation(project(":shared:features:tasks:api"))
            implementation(project(":shared:features:tasks:impl"))
            implementation(project(":shared:features:info:api"))
            implementation(project(":shared:features:info:impl"))
            implementation(project(":shared:features:profile:api"))
            implementation(project(":shared:features:profile:impl"))
            implementation(project(":shared:features:editor:api"))
            implementation(project(":shared:features:editor:impl"))

            implementation(project(":shared:core:domain"))
            implementation(project(":shared:core:data"))
            api(project(":shared:core:common"))
            api(project(":shared:core:ui"))

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