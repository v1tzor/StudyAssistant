plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.atomic)
}

kotlin {
    jvmToolchain(17)

    androidTarget()

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
            implementation(libs.rustore.universalpush.core)
        }
        androidMain.dependencies {
            api(libs.androidx.core.ktx)
            api(libs.androidx.activity.compose)
            api(libs.androidx.workmanager.ktx)
            api(libs.androidx.guava)
            api(libs.kodein.android)

            implementation(libs.rustore.universalpush.core)
        }
        commonMain.dependencies {
            api(libs.kotlin.atomicfu)
            api(libs.kotlin.coroutines)
            api(libs.kotlin.datetime)
            api(libs.kotlin.serialization)
            api(libs.kotlin.serialization.json)
            api(libs.moko.parcelize)
            api(libs.bignum)
            api(libs.logger)

            api(libs.networkcheker)

            api(libs.ktor.client.core)

            api(libs.kodein.core)
            implementation(libs.kodein.compose)

            implementation(libs.filekit.core)

            api(libs.androidx.paging.common)

            implementation(libs.bundles.voyager)
            implementation(libs.bundles.decompose)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.components.resources)

            implementation(libs.sqldelight.core)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.async)
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