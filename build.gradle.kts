plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION apply false
}

group = "team.themoment"
version = "v20260207.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
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

    configurations.all {
        exclude(group = "org.bouncycastle")
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
