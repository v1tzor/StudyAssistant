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

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://artifactory-external.vkpartner.ru/artifactory/maven")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://artifactory-external.vkpartner.ru/artifactory/maven")
        maven("https://jitpack.io")
    }
}

rootProject.name = "StudyAssistant"

include(":androidApp")
include(":widget")

include(":shared")

include(":shared:core:common")
include(":shared:core:ui")
include(":shared:core:domain")
include(":shared:core:data")
include(":shared:core:database")
include(":shared:core:remote")

include(":shared:features:preview:api")
include(":shared:features:preview:impl")
include(":shared:features:schedule:api")
include(":shared:features:schedule:impl")
include(":shared:features:tasks:api")
include(":shared:features:tasks:impl")
include(":shared:features:info:api")
include(":shared:features:info:impl")
include(":shared:features:profile:api")
include(":shared:features:profile:impl")
include(":shared:features:settings:api")
include(":shared:features:settings:impl")
include(":shared:features:users:api")
include(":shared:features:users:impl")
include(":shared:features:editor:api")
include(":shared:features:editor:impl")
include(":shared:features:chat:api")
include(":shared:features:chat:impl")
include(":shared:features:analytics:api")
include(":shared:features:analytics:impl")
include(":shared:core:presentation")
