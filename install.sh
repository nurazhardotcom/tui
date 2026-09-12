#!/usr/bin/env bash
# tui installer — AGPLv3 source offer included per §13.
set -euo pipefail
REPO="nurazhardotcom/tui"
VERSION="${1:-latest}"
echo "Installing tui ($VERSION) from https://github.com/${REPO} ..."
echo "Source available at https://github.com/${REPO} (AGPLv3)."
echo "TODO: fetch native binary from releases. For now: git clone + clj -M:run"
