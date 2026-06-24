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

    # Deploy tar (jars under lib/) for the CodeDeploy pipeline: extracted into the deploy
    # package, then `java -cp 'lib/*'` from the module's prod.dockerfile.
    pkg_tar(
        name = name + "_deploy_tar",
        srcs = [":" + name + "_runtime_jars"],
        package_dir = "lib",
        visibility = ["//visibility:public"],
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

def datagsm_kotest_test(name, associate, spring_test = False, extra_artifacts = []):
    """Runs a module's Kotest (+MockK) suite on the JUnit Platform via ConsoleLauncher.

    `associate` makes the test a friend module of the target under test, granting access
    to `internal` declarations (parity with Gradle's single main+test Kotlin module).

    Test classes are selected explicitly with `--select-class`, auto-derived from the
    `**/*Test.kt` paths (package = dir, class = filename, per project convention). Explicit
    selection is required because Bazel passes the classpath via a manifest jar (java.class.path
    holds only the launcher jar), so `--scan-classpath` / `--select-package` discover nothing.
    """
    select_args = [
        "--select-class=" + f[len("src/test/kotlin/"):-len(".kt")].replace("/", ".")
        for f in native.glob(["src/test/kotlin/**/*Test.kt"])
    ]
    spring_test_artifacts = [
        "org.springframework.boot:spring-boot-starter-test",
        "org.springframework.security:spring-security-test",
    ] if spring_test else []
    kt_jvm_test(
        name = name,
        srcs = native.glob(["src/test/kotlin/**/*.kt"]),
        resources = native.glob(["src/test/resources/**"], allow_empty = True),
        main_class = "org.junit.platform.console.ConsoleLauncher",
        args = [
            "execute",
            "--details=summary",
            "--fail-if-no-tests",
        ] + select_args,
        associates = [associate],
        deps = [
            artifact("io.kotest:kotest-runner-junit5-jvm"),
            artifact("io.kotest:kotest-assertions-core-jvm"),
            artifact("io.kotest:kotest-framework-engine-jvm"),
            artifact("io.mockk:mockk-jvm"),
            artifact("org.junit.platform:junit-platform-console"),
        ] + [artifact(a) for a in spring_test_artifacts] + [artifact(a) for a in extra_artifacts],
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
