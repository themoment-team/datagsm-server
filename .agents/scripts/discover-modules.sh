#!/bin/sh
# Discover module names from settings.gradle.kts.
# Strips the 'datagsm-' prefix. Works on Linux and macOS (POSIX-compatible).
grep 'include' settings.gradle.kts \
  | grep -oE '"[^"]+"' \
  | tr -d '"' \
  | sed 's/^datagsm-//'
