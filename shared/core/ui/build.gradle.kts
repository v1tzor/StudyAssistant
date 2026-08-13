/*
 * Copyright 2026 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "ru.aleshin.studyassistant.core.ui"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "coreUi"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.zxing.core)
        }
        commonMain.dependencies {
            implementation(project(":shared:core:common"))
            implementation(project(":shared:core:domain"))
            implementation(libs.yandex.ads.multiplatform)

            api(libs.bundles.decompose)

            api(libs.kodein.compose)

            api(compose.ui)
            api(compose.runtime)
            api(compose.material)
            api(compose.material3)
            api(compose.foundation)
            api(compose.materialIconsExtended)
            api(compose.components.resources)
            api(libs.compose.material3.adaptive)
            api(libs.compose.material3.adaptive.layout)
            api(libs.richtext)
            api(libs.richtext.material3)

            api(libs.google.accompanist)
            api(libs.placeholder)

            api(libs.koalaplot.charts)
            api(libs.bundles.filekit)
            api(libs.dragAndDrop)

            api(libs.bundles.sketch)
        }
    }

}

compose.resources {
    publicResClass = true
    packageOfResClass = "ru.aleshin.studyassistant.core.ui.resources"
    generateResClass = auto
}
