"""Collects a target's full transitive runtime classpath as a flat set of jars.

Used to containerize a Spring Boot service as base-image-java + arch-independent jars
(`java -cp /app/lib/*`), avoiding both the flat fat-jar (breaks Spring Data multi-store)
and JDK-bundling platform transitions (host macOS JDK can't run in a linux container).
"""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load("@rules_kotlin//kotlin/internal:defs.bzl", "KtJvmInfo")

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

# KtJvmInfo.annotation_processing.source_jar is the KSP `-ksp-gensrc.jar` — ONLY the
# KSP-generated sources (unlike JavaInfo.source_jars, which also bundles the original srcs).
# Requesting it runs just the KSP action, not the generator library's kotlinc compile.
def _ksp_generated_srcjar_impl(ctx):
    # Re-expose under a .srcjar name so kt_jvm_library's `srcs` treats it as a source jar.
    src = ctx.attr.target[KtJvmInfo].annotation_processing.source_jar
    out = ctx.actions.declare_file(ctx.label.name + ".srcjar")
    ctx.actions.symlink(output = out, target_file = src)
    return [DefaultInfo(files = depset([out]))]

ksp_generated_srcjar = rule(
    implementation = _ksp_generated_srcjar_impl,
    doc = "Exposes a target's clean KSP-generated source jar (-ksp-gensrc.jar).",
    attrs = {
        "target": attr.label(providers = [KtJvmInfo], mandatory = True),
    },
)

# The `-ksp-genclasses.jar` holds KSP-generated resources (here: index.d.ts emitted via
# createNewFileByPath).
def _ksp_generated_classes_impl(ctx):
    jars = [j for j in ctx.attr.target[KtJvmInfo].all_output_jars if j.basename.endswith("-ksp-genclasses.jar")]
    return [DefaultInfo(files = depset(jars))]

ksp_generated_classes = rule(
    implementation = _ksp_generated_classes_impl,
    doc = "Exposes a target's KSP-generated classes/resources jar (-ksp-genclasses.jar).",
    attrs = {
        "target": attr.label(providers = [KtJvmInfo], mandatory = True),
    },
)
