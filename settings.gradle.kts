rootProject.name = "datagsm-server"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

buildCache {
    local {
        directory = file("$rootDir/.gradle/build-cache")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include("datagsm-common")
include("datagsm-authorization")
include("datagsm-resource")
include("datagsm-web")
