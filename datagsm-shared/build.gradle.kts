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

// Toolchain compiles under JDK 25, but Kotlin targets JVM 17 bytecode (compilerOptions above).
// compileJava defaults to the toolchain version (25), so the Java target must be pinned to 17
// to match — otherwise Gradle fails with "Inconsistent JVM Target Compatibility".
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
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

// The KMP plugin used to register the `jvm` publication implicitly; after the KMP layer was
// removed the `kotlin("jvm")` plugin registers none, so it must be declared explicitly.
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
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
