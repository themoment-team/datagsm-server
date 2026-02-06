plugins {
    id(plugin.Plugins.KOTLIN_JVM) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_SPRING) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.KOTLIN_JPA) version plugin.PluginVersions.KOTLIN_VERSION apply false
    id(plugin.Plugins.SPRING_BOOT) version plugin.PluginVersions.SPRING_BOOT_VERSION apply false
    id(plugin.Plugins.SPRING_DEPENDENCY_MANAGEMENT) version plugin.PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION apply false
    id(plugin.Plugins.KSP) version plugin.PluginVersions.KSP_VERSION apply false
    id(plugin.Plugins.KTLINT) version plugin.PluginVersions.KTLINT_VERSION apply false
}

group = "team.themoment"
version = "v20260206.0"

subprojects {
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
