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
    alias(libs.plugins.skie)
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "ru.aleshin.studyassistant.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true

            binaryOption("bundleId", "ru.aleshin.studyassistant.shared")

            export(libs.decompose.core)
            export(libs.decompose.essenty.statekeeper)
            export(libs.decompose.essenty.instancekeeper)
            export(libs.decompose.essenty.backhandler)
            export(libs.decompose.essenty.lifecycle)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:features:preview:api"))
            implementation(project(":shared:features:preview:impl"))
            implementation(project(":shared:features:schedule:api"))
            implementation(project(":shared:features:schedule:impl"))
            implementation(project(":shared:features:tasks:api"))
            implementation(project(":shared:features:tasks:impl"))
            implementation(project(":shared:features:info:api"))
            implementation(project(":shared:features:info:impl"))
            implementation(project(":shared:features:profile:api"))
            implementation(project(":shared:features:profile:impl"))
            implementation(project(":shared:features:settings:api"))
            implementation(project(":shared:features:settings:impl"))
            implementation(project(":shared:features:users:api"))
            implementation(project(":shared:features:users:impl"))
            implementation(project(":shared:features:editor:api"))
            implementation(project(":shared:features:editor:impl"))
            implementation(project(":shared:features:chat:api"))
            implementation(project(":shared:features:chat:impl"))
            implementation(project(":shared:features:analytics:api"))
            implementation(project(":shared:features:analytics:impl"))

            api(project(":shared:core:common"))
            api(project(":shared:core:presentation"))
            api(project(":shared:core:domain"))
            api(project(":shared:core:data"))
            api(project(":shared:core:database"))
            api(project(":shared:core:remote"))

            api(libs.bundles.decompose)
            api(libs.bundles.essenty)
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
