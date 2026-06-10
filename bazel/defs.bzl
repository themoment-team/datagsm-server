"""Shared macro for datagsm Spring Boot service modules.

Encapsulates the root build.gradle.kts `isServiceModule` block: kotlin-spring/jpa
compiler plugins, the common-module dependency, the disender starter, and a runnable
kt_jvm_binary. Each service's build.gradle.kts only adds module-specific deps, mirrored
here via extra_artifacts / extra_runtime_artifacts.
"""

load("@rules_jvm_external//:defs.bzl", "artifact")
load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library", "kt_jvm_test")
load("@rules_oci//oci:defs.bzl", "oci_image", "oci_load")
load("@rules_pkg//pkg:tar.bzl", "pkg_tar")
load("//bazel:jars.bzl", "runtime_classpath")

def datagsm_service_image(name, main_class):
    """Containerizes a service's `<name>_lib`: base JRE + transitive runtime jars (arch-
    independent) under /app/lib, run via `java -cp`. Defines `<name>_image` and a
    `<name>_image_load` runnable that loads it into the local Docker daemon."""
    runtime_classpath(
        name = name + "_runtime_jars",
        deps = [":" + name + "_lib"],
    )
    pkg_tar(
        name = name + "_layer",
        srcs = [":" + name + "_runtime_jars"],
        package_dir = "/app/lib",
    )
    oci_image(
        name = name + "_image",
        base = "@temurin_jre",
        entrypoint = ["java", "-cp", "/app/lib/*", main_class],
        tars = [":" + name + "_layer"],
    )
    oci_load(
        name = name + "_image_load",
        image = ":" + name + "_image",
        repo_tags = ["datagsm-" + name + ":bazel"],
    )

def datagsm_kotest_test(name, associate, test_classes, extra_artifacts = []):
    """Runs a module's Kotest (+MockK) suite on the JUnit Platform via ConsoleLauncher.

    `associate` makes the test a friend module of the target under test, granting access
    to `internal` declarations (parity with Gradle's single main+test Kotlin module).

    `test_classes` lists the fully-qualified Kotest spec classes. Explicit `--select-class`
    is required because Bazel passes the classpath via a manifest jar (java.class.path holds
    only the launcher jar), so `--scan-classpath` / `--select-package` discover nothing —
    only direct class loading by name works. At scale, generate this list (or adopt
    contrib_rules_jvm's java_test_suite, which derives per-class test targets from a glob).
    """
    kt_jvm_test(
        name = name,
        srcs = native.glob(["src/test/kotlin/**/*.kt"]),
        resources = native.glob(["src/test/resources/**"], allow_empty = True),
        main_class = "org.junit.platform.console.ConsoleLauncher",
        args = [
            "execute",
            "--details=summary",
            "--fail-if-no-tests",
        ] + ["--select-class=" + c for c in test_classes],
        associates = [associate],
        deps = [
            artifact("io.kotest:kotest-runner-junit5-jvm"),
            artifact("io.kotest:kotest-assertions-core-jvm"),
            artifact("io.kotest:kotest-framework-engine-jvm"),
            artifact("io.mockk:mockk-jvm"),
            artifact("org.junit.platform:junit-platform-console"),
        ] + [artifact(a) for a in extra_artifacts],
    )

def datagsm_service(name, main_class, extra_artifacts = [], extra_runtime_artifacts = []):
    """Defines a `<name>_lib` library and a runnable `<name>` binary for a service module.

    Args:
      name: target/module name (e.g. "openapi").
      main_class: JVM main class of the @SpringBootApplication (top-level main → `...Kt`).
      extra_artifacts: module-specific compile Maven coordinates (implementation).
      extra_runtime_artifacts: module-specific runtime-only coordinates (runtimeOnly).
    """
    kt_jvm_library(
        name = name + "_lib",
        srcs = native.glob(["src/main/kotlin/**/*.kt"]),
        resources = native.glob(["src/main/resources/**"]),
        plugins = [
            "//bazel:allopen_spring",
            "//bazel:noarg_jpa",
        ],
        deps = [
            "//datagsm-common:common",
            artifact("io.github.zaman0806:disender-spring-boot-4-starter"),
        ] + [artifact(a) for a in extra_artifacts],
    )

    kt_jvm_binary(
        name = name,
        main_class = main_class,
        visibility = ["//visibility:public"],
        runtime_deps = [":" + name + "_lib"] + [artifact(a) for a in extra_runtime_artifacts],
    )
