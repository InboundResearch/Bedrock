#!/usr/bin/env bash
set -euo pipefail

# Build the project and run the site container via docker compose.
# Also opens the default browser to the localhost URL derived from compose + Dockerfile.

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SITE_DIR="$REPO_ROOT/site"
COMPOSE_DIR="$SITE_DIR/target/docker"
COMPOSE_YML="$SITE_DIR/src/main/docker/docker-compose.yml"
DOCKERFILE="$SITE_DIR/src/main/docker/Dockerfile"

open_browser() {
  local url="$1"
  if command -v open >/dev/null 2>&1; then
    # macOS
    open "$url" || true
  elif command -v xdg-open >/dev/null 2>&1; then
    # Linux
    xdg-open "$url" || true
  elif command -v powershell.exe >/dev/null 2>&1; then
    # WSL/Windows
    powershell.exe start "$url" || true
  else
    echo "Open your browser to: $url"
  fi
}

derive_port() {
  # Extract first host port from compose file mapping like "8082:8080"
  if [[ -f "$COMPOSE_YML" ]]; then
    local mapping
    mapping=$(grep -Eo '"[0-9]+:[0-9]+"' "$COMPOSE_YML" | head -n1 | tr -d '"') || true
    if [[ -n "${mapping:-}" ]]; then
      echo "${mapping%%:*}"
      return 0
    fi
  fi
  echo "8082"
}

derive_path() {
  # If Dockerfile copies ROOT.war, the context is '/'; else default to '/bedrock/'.
  if [[ -f "$DOCKERFILE" ]] && grep -qE 'COPY\s+.*ROOT\.war' "$DOCKERFILE"; then
    echo "/"
  else
    echo "/bedrock/"
  fi
}

echo "Building root project..."
mvn -q -e -DskipTests=false clean install

echo "Building site module..."
pushd "$SITE_DIR" >/dev/null
mvn -q -e -DskipTests=false clean install
popd >/dev/null

PORT="$(derive_port)"
PATH_SUFFIX="$(derive_path)"
URL="http://localhost:${PORT}${PATH_SUFFIX}"
echo "Launching browser at: $URL"
open_browser "$URL"

echo "Starting Docker (interactive)..."
pushd "$COMPOSE_DIR" >/dev/null
docker compose up --build --remove-orphans
popd >/dev/null

