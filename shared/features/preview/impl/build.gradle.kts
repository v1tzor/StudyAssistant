import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.parcelize)
}

kotlin {
    explicitApi = ExplicitApiMode.Warning

    jvmToolchain(17)

    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "previewImpl"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:features:navigation:api"))
            implementation(project(":shared:features:editor:api"))
            implementation(project(":shared:features:preview:api"))
            implementation(project(":shared:features:billing:api"))
            implementation(project(":shared:features:auth:api"))

            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:data"))
            implementation(project(":shared:core:domain"))
            implementation(project(":shared:core:ui"))

            implementation(compose.components.resources)
        }
    }

    task("testClasses")
}

android {
    namespace = "ru.aleshin.studyassistant.preview.impl"
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