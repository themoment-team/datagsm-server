// NOTE: This Kotlin Multiplatform module is the source of the configuration cache
// WARNINGs reported during the build — the KMP plugin serializes org.gradle.api.Project,
// which the configuration cache does not support. This is a KMP plugin limitation, not
// our code. It is why gradle.properties keeps configuration-cache.problems=warn instead
// of fail. JVM service modules are unaffected and cache fully.
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "team.themoment"
version = (findProperty("SHARED_VERSION") as? String)
    ?: System.getenv("SHARED_VERSION")
    ?: "local"

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalJsExport")
    }

    // Compile with the project-wide JDK 25 toolchain, but emit JVM 17 bytecode. This is the
    // only externally published artifact, so a JVM 17 target keeps it consumable by any
    // JDK 17+ client while the build itself stays aligned with the service modules.
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(
                project(":datagsm-common")
                    .layout.buildDirectory
                    .dir("generated/kmp-export/main/kotlin"),
            )
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            }
        }
    }
}

tasks.named("compileKotlinJvm") {
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
            .file("generated/kmp-export/main/ts/index.d.ts")
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

        // npm requires semver; CI passes "<yyyyMMdd>-<run>" → normalize to "<yyyyMMdd>.0.<run>".
        // Kept as a release version (no prerelease suffix) so `npm publish` doesn't require --tag.
        val npmVersion =
            Regex("""^(\d+)-(\d+)$""")
                .matchEntire(packageVersion)
                ?.let { "${it.groupValues[1]}.0.${it.groupValues[2]}" }
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
