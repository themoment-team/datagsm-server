#!/usr/bin/env bash
# Publishes datagsm-shared to GitHub Packages (replaces the Gradle `publishing` block).
# Credentials from env: GITHUB_ACTOR / GITHUB_TOKEN. Requires `mvn` on PATH.
# Build with the JVM 17 target flag so the SDK stays consumable on JDK 17+:
#   bazel run //datagsm-shared:publish --//bazel:kt_jvm_target=17 -- <version>
set -euo pipefail

version="${1:?usage: publish <version>}"
group="team.themoment"
artifact="datagsm-shared"
repo_url="https://maven.pkg.github.com/themoment-team/datagsm-server"

# -print -quit stops at the first match (no `| head`, which can SIGPIPE under pipefail).
jar="$(find "${RUNFILES_DIR:-$0.runfiles}" -name 'shared.jar' -print -quit 2>/dev/null)"
[ -n "$jar" ] || { echo "shared.jar not found in runfiles" >&2; exit 1; }

# Temp files hold the GITHUB_TOKEN — trap guarantees cleanup even on error/interrupt
# (no `exec mvn`, which would replace the shell and skip the trap).
settings="$(mktemp)"
pom="$(mktemp)"
trap 'rm -f "$settings" "$pom"' EXIT

cat > "$settings" <<XML
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>${GITHUB_ACTOR:?}</username>
      <password>${GITHUB_TOKEN:?}</password>
    </server>
  </servers>
</settings>
XML

# Declare the runtime dependencies the generated @Serializable classes need, so consumers
# resolve them transitively (a bare deploy-file would publish a dependency-less POM, the
# regression vs the Gradle KMP publication). Versions mirror MODULE.bazel.
cat > "$pom" <<XML
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${group}</groupId>
  <artifactId>${artifact}</artifactId>
  <version>${version}</version>
  <packaging>jar</packaging>
  <dependencies>
    <dependency>
      <groupId>org.jetbrains.kotlinx</groupId>
      <artifactId>kotlinx-serialization-json-jvm</artifactId>
      <version>1.8.1</version>
    </dependency>
    <dependency>
      <groupId>org.jetbrains.kotlinx</groupId>
      <artifactId>kotlinx-datetime-jvm</artifactId>
      <version>0.6.2</version>
    </dependency>
  </dependencies>
</project>
XML

mvn -s "$settings" deploy:deploy-file \
  -DrepositoryId=github \
  -Durl="$repo_url" \
  -DpomFile="$pom" \
  -Dfile="$jar"
