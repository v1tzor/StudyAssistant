
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.huawei.agconnect.agcp.AGCPExtension

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kapt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.tracer)
}

val hasHuawei = gradle.startParameter.taskNames.any {
    it.contains("HuaweiDebug", ignoreCase = true) || it.contains("HuaweiRelease", ignoreCase = true)
}

if (!hasHuawei) {
    plugins.apply(libs.plugins.gms.get().pluginId)
    plugins.apply(libs.plugins.hms.get().pluginId)
    project.extensions.configure<AGCPExtension> { manifest = false }
} else {
    plugins.apply(libs.plugins.hms.get().pluginId)
    project.extensions.configure<AGCPExtension> { manifest = false }
}

android {
    namespace = "ru.aleshin.studyassistant.android"
    flavorDimensions += "production"

    val localProperties = gradleLocalProperties(rootDir, providers)

    defaultConfig {
        applicationId = libs.versions.applicationId.get()
        minSdk = libs.versions.minSdk.get().toIntOrNull()
        targetSdk = libs.versions.targetSdk.get().toIntOrNull()
        compileSdk = libs.versions.compileSdk.get().toIntOrNull()

        versionCode = libs.versions.version.code.get().toIntOrNull()
        versionName = libs.versions.version.name.get()

        testInstrumentationRunner = libs.versions.testInstrumentationRunner.get()
        vectorDrawables.useSupportLibrary = true
        resourceConfigurations.addAll(listOf("en", "ru"))

        val firebaseProjectId = localProperties.getProperty("firebaseProjectId")
        val firebaseApplicationId = localProperties.getProperty("firebaseApplicationId")
        val firebaseStorageBucket = localProperties.getProperty("firebaseStorageBucket")
        val firebaseApiKey = localProperties.getProperty("firebaseApiKey")
        val myTrackerKey = localProperties.getProperty("myTrackerKey")
        val hmsAppId = localProperties.getProperty("hmsAppId")
        val rustoreConsoleAppId = localProperties.getProperty("rustoreConsoleAppId")

        buildConfigField("String", "HMS_APP_ID", "\"$hmsAppId\"")
        buildConfigField("String", "MY_TRACKER_KEY", "\"$myTrackerKey\"")
        buildConfigField("String", "PROJECT_ID", "\"$firebaseProjectId\"")
        buildConfigField("String", "APPLICATION_ID", "\"$firebaseApplicationId\"")
        buildConfigField("String", "STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
        buildConfigField("String", "RUSTORE_CONSOLE_APP_ID", "\"$rustoreConsoleAppId\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("storeFile"))
            storePassword = localProperties.getProperty("storePassword")
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")

            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
        getByName("debug") {
            storeFile = file(localProperties.getProperty("storeFile"))
            storePassword = localProperties.getProperty("storePassword")
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            // applicationIdSuffix = ".debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    productFlavors {
        create("huawei") {
            dimension = "production"
        }
        create("github") {
            dimension = "production"
            val rustoreProjectId = localProperties.getProperty("rustoreProjectId")
            buildConfigField("String", "PROJECT_ID", "\"$rustoreProjectId\"")
        }
        create("rustore") {
            dimension = "production"
            val rustoreProjectId = localProperties.getProperty("rustoreProjectId")
            buildConfigField("String", "PROJECT_ID", "\"$rustoreProjectId\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            resources.pickFirsts.add("META-INF/INDEX.LIST")
            resources.merges.add("META-INF/DEPENDENCIES")
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val rustoreImplementation = "rustoreImplementation"
val huaweiImplementation = "huaweiImplementation"
val githubImplementation = "githubImplementation"

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.workmanager.ktx)
    implementation(libs.androidx.workmanager)
    implementation(libs.compose.material)

    implementation(libs.sqldelight.core)
    implementation(libs.sqldelight.android)

    implementation(libs.kodein.android)

    implementation(platform(libs.tracer.bom))
    implementation(libs.bundles.tracer)
    implementation(libs.mytracker.core)

    implementation(platform(libs.firebase.bom.android))
    implementation(libs.firebase.auth.android)
    implementation(libs.firebase.messaging.android)
    implementation(libs.firebase.messaging.directboot.android)
    implementation(libs.firebase.messaging.android)
    rustoreImplementation(libs.google.gms.services)
    githubImplementation(libs.google.gms.services)
    huaweiImplementation(libs.google.gms.services)

    rustoreImplementation(libs.hms.core)
    githubImplementation(libs.hms.core)
    huaweiImplementation(libs.hms.core)
    rustoreImplementation(libs.hms.push)
    githubImplementation(libs.hms.push)
    huaweiImplementation(libs.hms.push)

    huaweiImplementation(libs.hms.iap)
    rustoreImplementation(libs.rustore.billing)

    implementation(libs.rustore.universalpush.core)
    huaweiImplementation(libs.rustore.universalpush.hms)
    rustoreImplementation(libs.rustore.universalpush.fcm)
    rustoreImplementation(libs.rustore.universalpush.hms)
    rustoreImplementation(libs.rustore.universalpush.rustore)
    githubImplementation(libs.rustore.universalpush.rustore)
    githubImplementation(libs.rustore.universalpush.fcm)
    githubImplementation(libs.rustore.universalpush.hms)
}

tracer {
    create("defaultConfig") {
        val localProperties = gradleLocalProperties(rootDir, providers)
        pluginToken = localProperties.getProperty("tracerPluginToken")
        appToken = localProperties.getProperty("tracerAppToken")
        uploadMapping = true
        uploadNativeSymbols = true
    }
}