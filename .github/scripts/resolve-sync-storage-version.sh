#!/usr/bin/env bash
# Resolve newest Sync Storage tag on the floor's major.minor line + channel.
# Floor examples: v0.4.4 (numeric only) | v0.4.3-tms-6.0 (only *-tms-6.0)
set -euo pipefail

FLOOR="${1:?floor version required, e.g. v0.4.3-tms-6.0}"

if [[ "$FLOOR" =~ ^v?([0-9]+)\.([0-9]+)\.([0-9]+)(-tms-([0-9.]+))?$ ]]; then
  MAJOR="${BASH_REMATCH[1]}"
  MINOR="${BASH_REMATCH[2]}"
  PATCH="${BASH_REMATCH[3]}"
  TMS_CHANNEL="${BASH_REMATCH[5]:-}"
else
  echo "Unsupported Sync Storage floor: $FLOOR" >&2
  exit 1
fi

TAGS_JSON="$(curl -fsSL \
  -H 'Accept: application/vnd.github+json' \
  -H 'User-Agent: testit-java-adapter-ci' \
  'https://api.github.com/repos/testit-tms/sync-storage-public/releases?per_page=100')"

BEST=""
BEST_PATCH=-1

while IFS= read -r TAG; do
  [[ -z "$TAG" ]] && continue
  if [[ -n "$TMS_CHANNEL" ]]; then
    [[ "$TAG" =~ ^v?([0-9]+)\.([0-9]+)\.([0-9]+)-tms-${TMS_CHANNEL}$ ]] || continue
  else
    [[ "$TAG" =~ ^v?([0-9]+)\.([0-9]+)\.([0-9]+)$ ]] || continue
  fi
  T_MAJOR="${BASH_REMATCH[1]}"
  T_MINOR="${BASH_REMATCH[2]}"
  T_PATCH="${BASH_REMATCH[3]}"
  [[ "$T_MAJOR" == "$MAJOR" && "$T_MINOR" == "$MINOR" ]] || continue
  (( T_PATCH >= PATCH )) || continue
  if (( T_PATCH > BEST_PATCH )); then
    BEST_PATCH="$T_PATCH"
    BEST="v${T_MAJOR}.${T_MINOR}.${T_PATCH}"
    if [[ -n "$TMS_CHANNEL" ]]; then
      BEST="${BEST}-tms-${TMS_CHANNEL}"
    fi
  fi
done < <(echo "$TAGS_JSON" | grep -oE '"tag_name"[[:space:]]*:[[:space:]]*"[^"]+"' | sed -E 's/.*"([^"]+)"/\1/')

if [[ -z "$BEST" ]]; then
  BEST="$FLOOR"
  [[ "$BEST" == v* ]] || BEST="v$BEST"
fi

echo "$BEST"
