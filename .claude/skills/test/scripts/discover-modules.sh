#!/bin/sh
grep '^[[:space:]]*include' settings.gradle.kts \
  | grep -oE '"[^"]+"' \
  | tr -d '"' \
  | sed 's/^datagsm-//'
