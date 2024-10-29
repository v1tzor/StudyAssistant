
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kapt)
    alias(libs.plugins.compose.compiler)
}

val hasFdroid = gradle.startParameter.taskNames.any {
    it.contains("FdroidDebug", ignoreCase = true) || it.contains("FdroidRelease", ignoreCase = true)
}

if (!hasFdroid) {
    apply(plugin = libs.plugins.gms.get().pluginId)
    apply(plugin = libs.plugins.crashlytics.get().pluginId)
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            if (!hasFdroid) configure<CrashlyticsExtension> { mappingFileUploadEnabled = false }
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    productFlavors {
        create("google") {
            dimension = "production"
        }
        create("github") {
            dimension = "production"
        }
        create("fdroid") {
            dimension = "production"
            val firebaseProjectId = localProperties.getProperty("firebaseProjectId")
            val firebaseApplicationId = localProperties.getProperty("firebaseApplicationId")
            val firebaseStorageBucket = localProperties.getProperty("firebaseStorageBucket")
            val firebaseApiKey = localProperties.getProperty("firebaseApiKey")

            buildConfigField("String", "PROJECT_ID", "\"$firebaseProjectId\"")
            buildConfigField("String", "APPLICATION_ID", "\"$firebaseApplicationId\"")
            buildConfigField("String", "STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
            buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
        }
        create("rustore") {
            dimension = "production"

            val projectId = localProperties.getProperty("rustoreProjectId")
            val serviceAuthToken = localProperties.getProperty("rustoreServiceAuthToken")

            buildConfigField("String", "PROJECT_ID", "\"$projectId\"")
            buildConfigField("String", "SERVICE_AUTH_TOKEN", "\"$serviceAuthToken\"")
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
val googleImplementation = "googleImplementation"
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

    googleImplementation(libs.google.gms.services)
    rustoreImplementation(libs.google.gms.services)
    githubImplementation(libs.google.gms.services)

    implementation(platform(libs.firebase.bom.android))
    implementation(libs.firebase.auth.android)
    implementation(libs.firebase.messaging.android)
    implementation(libs.firebase.messaging.directboot.android)
    googleImplementation(libs.firebase.crashlytics.android)
    rustoreImplementation(libs.firebase.crashlytics.android)
    githubImplementation(libs.firebase.crashlytics.android)

    implementation(platform(libs.rustore.bom))
    implementation(libs.rustore.universalpush.core)
    googleImplementation(libs.rustore.universalpush.fcm)
    rustoreImplementation(libs.rustore.universalpush.fcm)
    githubImplementation(libs.rustore.universalpush.fcm)
    rustoreImplementation(libs.rustore.universalpush.rustore)
}