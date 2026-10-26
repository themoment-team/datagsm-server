import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SourcesJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id(plugin.Plugins.MAVEN_PUBLISH)
}

group = "io.github.themoment-team"
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
    // Response DTOs expose kotlinx.datetime.LocalDate in their public properties, so consumers
    // need it on the compile classpath.
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
}

tasks.named("compileKotlin") {
    dependsOn(":datagsm-common:kspKotlin")
}

// The main source set includes the KSP-generated srcDir above, so ktlint reads
// :datagsm-common:kspKotlin's output without Gradle inferring that dependency.
tasks.named("runKtlintCheckOverMainSourceSet") {
    dependsOn(":datagsm-common:kspKotlin")
}

// The only source root is KSP-generated, so sourcesJar silently produces an empty archive
// unless the generator has already run. Gradle infers no dependency from srcDir alone.
// The task is registered by mavenPublishing.configure() below, so match it lazily
// instead of using tasks.named, which would fail before it exists.
tasks.matching { it.name == "sourcesJar" }.configureEach {
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
              "repository": {
                "type": "git",
                "url": "git+https://github.com/themoment-team/datagsm-server.git"
              },
              "files": [
                "index.d.ts"
              ],
              "license": "MIT"
            }
            """.trimIndent() + "\n",
        )
    }
}

// Published to Maven Central. Credentials and the signing key come from ORG_GRADLE_PROJECT_*
// environment variables in CI.
mavenPublishing {
    // Central rejects deployments without a javadoc jar. The sources are generated DTOs with
    // no KDoc, so an empty javadoc jar satisfies the requirement without adding Dokka.
    configure(KotlinJvm(javadocJar = JavadocJar.Empty(), sourcesJar = SourcesJar.Sources()))
    publishToMavenCentral()
    // Sign only when a key is provided, so publishToMavenLocal works without one. An unsigned
    // upload to Central fails Portal validation, so this cannot release unsigned artifacts.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
    coordinates(group.toString(), "datagsm-shared", version.toString())

    pom {
        name = "DataGSM Shared"
        description = "Type definitions for the datagsm-server API"
        url = "https://github.com/themoment-team/datagsm-server"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "themoment"
                name = "themoment-team"
                email = "datagsm.oauth@gmail.com"
            }
        }

        scm {
            connection = "scm:git:git://github.com/themoment-team/datagsm-server.git"
            developerConnection = "scm:git:ssh://github.com:themoment-team/datagsm-server.git"
            url = "https://github.com/themoment-team/datagsm-server"
        }
    }
}
