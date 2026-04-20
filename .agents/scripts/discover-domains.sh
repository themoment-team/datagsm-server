#!/bin/sh
# Discover domain names from the project's domain directory structure.
# Works on Linux and macOS (POSIX-compatible).
find . -type d -path "*/domain/*" \
  ! -path "*/build/*" \
  ! -path "*/test/*" \
  ! -path "*/.gradle/*" \
  | awk -F'/domain/' 'NF>1 { split($NF, a, "/"); print a[1] }' \
  | sort -u
