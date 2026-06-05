#!/bin/sh
find . -type d -path "*/domain/*" \
  ! -path "*/build/*" \
  ! -path "*/test/*" \
  ! -path "*/.gradle/*" \
  | awk -F'/domain/' 'NF>1 { split($NF, a, "/"); print a[1] }' \
  | sort -u
