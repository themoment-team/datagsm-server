plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION apply false
}

group = "team.themoment"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

allprojects {
    apply(plugin = plugin.Plugins.KTLINT)

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude("**/build/**")
            exclude {
                projectDir
                    .toURI()
                    .relativize(it.file.toURI())
                    .path
                    .contains("/generated/")
            }
        }
    }
}

tasks.whenTaskAdded {
    if (name == "bootJar" || name == "jar") {
        enabled = false
    }
}

dependencyManagement {
    imports {
        mavenBom(dependency.Dependencies.SPRING_CLOUD_BOM)
        mavenBom(dependency.Dependencies.AWS_SDK_BOM)
    }
}
