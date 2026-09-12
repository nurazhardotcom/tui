#!/usr/bin/env bash
# tui installer (MVP) — AGPLv3 source offer per LICENSE §13.
set -euo pipefail
REPO="nurazhardotcom/tui"
echo "Source: https://github.com/${REPO} (AGPLv3)."

command -v java >/dev/null || { echo "error: java 21+ required" >&2; exit 1; }
command -v clojure >/dev/null || { echo "error: clojure CLI required (https://clojure.org/guides/install_clojure)" >&2; exit 1; }

if [ ! -d ".git" ]; then
  git clone "https://github.com/${REPO}.git"
  cd tui
fi
echo "Running smoke check..."
clojure -M:test
echo "Launch with: clojure -M:run"
