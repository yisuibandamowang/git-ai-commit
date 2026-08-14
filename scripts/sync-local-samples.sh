#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

bash "$root_dir/scripts/export-commits.sh" /Users/work_project/360/ad-platform-bot "$root_dir/samples/local/ad-platform-bot.full.csv"
bash "$root_dir/scripts/export-commits.sh" /Users/work_project/360/member "$root_dir/samples/local/member.full.csv"
