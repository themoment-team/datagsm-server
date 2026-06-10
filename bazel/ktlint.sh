#!/usr/bin/env bash
# Runs the ktlint CLI from the workspace root (replaces the Gradle ktlint plugin).
#   bazel run //bazel:ktlint        -> check
#   bazel run //bazel:ktlint -- -F  -> format
set -euo pipefail

jar="$(find "${RUNFILES_DIR:-$0.runfiles}" -name '*ktlint-cli-*-all.jar' -print -quit 2>/dev/null)"
[ -n "$jar" ] || { echo "ktlint-cli -all jar not found in runfiles" >&2; exit 1; }

cd "${BUILD_WORKSPACE_DIRECTORY:?must be run via 'bazel run'}"
exec java -jar "$jar" "$@" '**/*.kt' '!**/build/**' '!bazel-*/**'
