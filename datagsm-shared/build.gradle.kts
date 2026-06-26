plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "team.themoment"
version = (findProperty("SHARED_VERSION") as? String)
    ?: System.getenv("SHARED_VERSION")
    ?: "local"

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

sourceSets {
    main {
        kotlin.srcDir(
            project(":datagsm-common")
                .layout.buildDirectory
                .dir("generated/sdk-export/main/kotlin"),
        )
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
}

tasks.named("compileKotlin") {
    dependsOn(":datagsm-common:kspKotlin")
}

// Assembles a runtime-free, type-only npm package from the KSP-generated index.d.ts.
// Replaces the former Kotlin/JS IR output (see plan-04): no kotlinx-serialization /
// kotlinx-datetime / @js-joda runtime dependency, plain TypeScript types only.
val tsPackageDir = layout.buildDirectory.dir("npm-ts")

tasks.register("assembleTsPackage") {
    group = "publishing"
    description = "Assembles the type-only npm package (@themoment-team/datagsm-shared) from generated TypeScript definitions."
    dependsOn(":datagsm-common:kspKotlin")

    val generatedTs =
        project(":datagsm-common")
            .layout.buildDirectory
            .file("generated/sdk-export/main/ts/index.d.ts")
    val outputDir = tsPackageDir
    val packageVersion = project.version.toString()

    outputs.dir(outputDir)

    doLast {
        val tsFile = generatedTs.get().asFile
        check(tsFile.exists()) {
            "Generated index.d.ts not found at ${tsFile.path}. Run :datagsm-common:kspKotlin first."
        }
        val dir = outputDir.get().asFile
        dir.mkdirs()
        tsFile.copyTo(dir.resolve("index.d.ts"), overwrite = true)

        // npm requires semver; CI passes "<yyyyMMdd>-<run>" → normalize to "<yyyyMMdd>.0.0-<run>".
        val npmVersion =
            Regex("""^(\d+)-(\d+)$""")
                .find(packageVersion)
                ?.let { "${it.groupValues[1]}.0.0-${it.groupValues[2]}" }
                ?: packageVersion

        dir.resolve("package.json").writeText(
            """
            {
              "name": "@themoment-team/datagsm-shared",
              "version": "$npmVersion",
              "description": "Type definitions for the datagsm-server API",
              "types": "index.d.ts",
              "files": [
                "index.d.ts"
              ],
              "license": "MIT"
            }
            """.trimIndent() + "\n",
        )
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/themoment-team/datagsm-server")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
