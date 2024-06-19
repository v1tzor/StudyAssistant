pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "StudyAssistant"

include(":androidApp")

include(":shared")

include(":shared:core:common")
include(":shared:core:ui")
include(":shared:core:domain")
include(":shared:core:data")

include(":shared:features:preview:api")
include(":shared:features:preview:impl")
include(":shared:features:auth:api")
include(":shared:features:auth:impl")
include(":shared:features:navigation:api")
include(":shared:features:navigation:impl")
include(":shared:features:schedule:api")
include(":shared:features:schedule:impl")
include(":shared:features:tasks:api")
include(":shared:features:tasks:impl")
include(":shared:features:info:api")
include(":shared:features:info:impl")
include(":shared:features:schedule:impl")
include(":shared:features:profile:api")
include(":shared:features:profile:impl")
include(":shared:features:settings:api")
include(":shared:features:settings:impl")
include(":shared:features:editor:api")
include(":shared:features:editor:impl")