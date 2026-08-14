#!/usr/bin/env bash
set -euo pipefail

repo_path="${1:-}"
output_file="${2:-}"

if [[ -z "$repo_path" || -z "$output_file" ]]; then
  echo "usage: export-commits.sh <repo-path> <output-file>" >&2
  exit 1
fi

git -C "$repo_path" log --reverse --pretty=format:'%H|%ad|%an|%s' --date=short \
  | awk -F'|' -v repo="$(basename "$repo_path")" 'BEGIN { print "source,commit,date,author,subject" } {
      gsub(/"/, "\"\"", $4);
      printf "%s,%s,%s,%s,\"%s\"\n", repo, $1, $2, $3, $4
    }' > "$output_file"
