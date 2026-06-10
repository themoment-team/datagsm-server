#!/usr/bin/env bash
# Publishes datagsm-shared to GitHub Packages (replaces the Gradle `publishing` block).
# Credentials from env: GITHUB_ACTOR / GITHUB_TOKEN. Requires `mvn` on PATH.
#   bazel run //datagsm-shared:publish -- <version>
set -euo pipefail

version="${1:?usage: publish <version>}"
group="team.themoment"
artifact="datagsm-shared"
repo_url="https://maven.pkg.github.com/themoment-team/datagsm-server"

jar="$(find "${RUNFILES_DIR:-$0.runfiles}" -name 'shared.jar' 2>/dev/null | head -1)"
[ -n "$jar" ] || { echo "shared.jar not found in runfiles" >&2; exit 1; }

settings="$(mktemp)"
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

exec mvn -s "$settings" deploy:deploy-file \
  -DrepositoryId=github \
  -Durl="$repo_url" \
  -DgroupId="$group" \
  -DartifactId="$artifact" \
  -Dversion="$version" \
  -Dpackaging=jar \
  -Dfile="$jar"
