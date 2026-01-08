import dependency.Dependencies.AWS_SDK_BOM
import dependency.Dependencies.SPRING_CLOUD_BOM

plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
}

group = "team.themoment"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

tasks.whenTaskAdded {
    if (name == "bootJar" || name == "jar") {
        enabled = false
    }
}

dependencyManagement {
    imports {
        mavenBom(SPRING_CLOUD_BOM)
        mavenBom(AWS_SDK_BOM)
    }
}
