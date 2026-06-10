"""Collects a target's full transitive runtime classpath as a flat set of jars.

Used to containerize a Spring Boot service as base-image-java + arch-independent jars
(`java -cp /app/lib/*`), avoiding both the flat fat-jar (breaks Spring Data multi-store)
and JDK-bundling platform transitions (host macOS JDK can't run in a linux container).
"""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def _runtime_classpath_impl(ctx):
    jars = depset(transitive = [dep[JavaInfo].transitive_runtime_jars for dep in ctx.attr.deps])
    return [DefaultInfo(files = jars)]

runtime_classpath = rule(
    implementation = _runtime_classpath_impl,
    doc = "Exposes deps' transitive_runtime_jars as DefaultInfo files (for pkg_tar).",
    attrs = {
        "deps": attr.label_list(providers = [JavaInfo], mandatory = True),
    },
)
