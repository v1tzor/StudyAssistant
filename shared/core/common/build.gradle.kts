import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.konfig)
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
        }
    }

    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.core.ktx)
            api(libs.androidx.activity.compose)
            api(libs.androidx.workmanager)

            implementation(project.dependencies.platform(libs.rustore.bom))
            implementation(libs.rustore.universalpush.core)
        }
        commonMain.dependencies {
            api(libs.kotlin.coroutines)
            api(libs.kotlin.datetime)
            api(libs.kotlin.serialization)
            api(libs.moko.parcelize)
            api(libs.bignumn)
            api(libs.logger)

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

buildkonfig {
    packageName = "ru.aleshin.studyassistant.core.common"

    val rustoreProjectId = gradleLocalProperties(rootDir, providers).getProperty("rustoreProjectId")
    val rustoreAuthToken = gradleLocalProperties(rootDir, providers).getProperty("rustoreServiceAuthToken")

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "RUSTORE_PROJECT_ID", rustoreProjectId)
        buildConfigField(FieldSpec.Type.STRING, "RUSTORE_SERVICE_AUTH_TOKEN", rustoreAuthToken)
    }
}