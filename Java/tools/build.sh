#!/usr/bin/env bash
set -euo pipefail

# Build the project and run the site container via docker compose.
# Also opens the default browser to the localhost URL derived from compose + Dockerfile.

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SITE_DIR="$REPO_ROOT/site"
COMPOSE_DIR="$SITE_DIR/target/docker"
COMPOSE_YML="$SITE_DIR/src/main/docker/docker-compose.yml"
DOCKERFILE="$SITE_DIR/src/main/docker/Dockerfile"
TEST_SERVER_DIR="$(cd "$REPO_ROOT/.." && pwd)/Bedrock-1.x"

TEST_SERVER_ENABLED=1
KEEP_TEST_SERVER=0
TEST_SERVER_BASE_URL_ARG=""
COMPILE_ONLY=0
DEPLOY=0
RUN_SITE=0

for arg in "$@"; do
  case "$arg" in
    --no-test-server) TEST_SERVER_ENABLED=0;;
    --keep-test-server) KEEP_TEST_SERVER=1;;
    --test-server-url=*) TEST_SERVER_BASE_URL_ARG="${arg#*=}";;
    --compile-only) COMPILE_ONLY=1; TEST_SERVER_ENABLED=0;;
    --deploy) DEPLOY=1;;
    --run-site) RUN_SITE=1;;
  esac
done

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

echo "Building Docker-based build environment image..."
docker build -f "$REPO_ROOT/tools/Dockerfile.build" -t bedrock-build:latest "$REPO_ROOT"

start_test_server() {
  if [[ $TEST_SERVER_ENABLED -eq 0 ]]; then return; fi
  if [[ -f "$TEST_SERVER_DIR/docker-compose.yml" ]]; then
    echo "Starting test support server (Bedrock-1.x) for unit tests..."
    pushd "$TEST_SERVER_DIR" >/dev/null
    docker compose up -d
    popd >/dev/null
  else
    echo "WARN: Test support server compose not found at $TEST_SERVER_DIR. Skipping.";
    TEST_SERVER_ENABLED=0
  fi
}

wait_for_port() {
  local host="$1"; local port="$2"; local label="$3"; local timeout="${4:-30}"
  local start="$(date +%s)"
  echo "Waiting for $label at $host:$port (timeout ${timeout}s)..."
  while true; do
    if (echo >/dev/tcp/$host/$port) >/dev/null 2>&1; then
      echo "$label is ready."
      return 0
    fi
    sleep 1
    now="$(date +%s)"; (( now - start >= timeout )) && { echo "ERROR: $label not ready in ${timeout}s"; return 1; }
  done
}

stop_test_server() {
  if [[ $TEST_SERVER_ENABLED -eq 1 && $KEEP_TEST_SERVER -eq 0 ]]; then
    echo "Stopping test support server..."
    pushd "$TEST_SERVER_DIR" >/dev/null
    docker compose down || true
    popd >/dev/null
    TEST_SERVER_ENABLED=0
  fi
}

show_test_server_logs() {
  if [[ -f "$TEST_SERVER_DIR/docker-compose.yml" ]]; then
    echo "--- Test support server logs (last 200 lines) ---"
    pushd "$TEST_SERVER_DIR" >/dev/null
    docker compose logs --tail=200 || true
    popd >/dev/null
    echo "--- end logs ---"
  fi
}

start_test_server
if [[ $TEST_SERVER_ENABLED -eq 1 ]]; then
  # Default ports expected by tests
  if ! wait_for_port 127.0.0.1 8081 "Bedrock-1.x (HTTP)" 60; then
    show_test_server_logs
    exit 1
  fi
  if ! wait_for_port 127.0.0.1 27017 "MongoDB" 60; then
    show_test_server_logs
    exit 1
  fi
fi

echo "Running build inside Docker container..."
DOCKER_UID="$(id -u)"; DOCKER_GID="$(id -g)"
DOCKER_ADD_HOST=("--add-host" "host.docker.internal:host-gateway")

# Pass through optional test server base URL to Maven
MVN_URL_PROP=""
# Default: skip GPG signing during regular builds; enable only on --deploy
MVN_SIGN_PROP="-Dgpg.skip=true -Dgpg.homedir=/tmp/.gnupg"
if [[ $COMPILE_ONLY -eq 0 ]]; then
  if [[ -n "$TEST_SERVER_BASE_URL_ARG" ]]; then MVN_URL_PROP="-DTEST_SERVER_BASE_URL=$TEST_SERVER_BASE_URL_ARG";
  elif [[ -n "${TEST_SERVER_BASE_URL:-}" ]]; then MVN_URL_PROP="-DTEST_SERVER_BASE_URL=$TEST_SERVER_BASE_URL"; fi
fi

docker run --rm \
  -u "$DOCKER_UID:$DOCKER_GID" \
  -v "$REPO_ROOT":/workspace \
  -v "$HOME/.m2":/m2 \
  -e HOME=/tmp \
  -e GNUPGHOME=/tmp/.gnupg \
  -e MAVEN_CONFIG=/m2 \
  -w /workspace \
  "${DOCKER_ADD_HOST[@]}" \
  bedrock-build:latest \
  bash -lc "\
    set -euo pipefail; \
    GNUPG_DIR=\"${GNUPGHOME:-/tmp/.gnupg}\"; \
    mkdir -p \"\$GNUPG_DIR\"; \
    if command -v socat >/dev/null 2>&1; then \
      (socat TCP-LISTEN:8081,fork,reuseaddr TCP:host.docker.internal:8081 &) >/dev/null 2>&1; \
      (socat TCP-LISTEN:27017,fork,reuseaddr TCP:host.docker.internal:27017 &) >/dev/null 2>&1; \
      sleep 1; \
    fi; \
    mvn -q -e -Dmaven.repo.local=/m2/repository -DskipTests=$([[ $COMPILE_ONLY -eq 1 ]] && echo true || echo false) ${MVN_SIGN_PROP} ${MVN_URL_PROP} clean install \
  "

# Build the site module independently to produce the WAR without running Docker inside the container
docker run --rm \
  -u "$DOCKER_UID:$DOCKER_GID" \
  -v "$REPO_ROOT":/workspace \
  -v "$HOME/.m2":/m2 \
  -e HOME=/tmp \
  -e GNUPGHOME=/tmp/.gnupg \
  -e MAVEN_CONFIG=/m2 \
  -w /workspace/site \
  "${DOCKER_ADD_HOST[@]}" \
  bedrock-build:latest \
  bash -lc "\
    set -euo pipefail; \
    GNUPG_DIR=\"${GNUPGHOME:-/tmp/.gnupg}\"; \
    mkdir -p \"\$GNUPG_DIR\"; \
    mvn -q -e -Dmaven.repo.local=/m2/repository -DskipTests=$([[ $COMPILE_ONLY -eq 1 ]] && echo true || echo false) ${MVN_SIGN_PROP} ${MVN_URL_PROP} clean package \
  "

trap stop_test_server EXIT

# Stage site Docker context if a WAR was produced
if [[ -f "$SITE_DIR/target/bedrock.war" ]]; then
  mkdir -p "$COMPOSE_DIR"
  cp -R "$SITE_DIR/src/main/docker/." "$COMPOSE_DIR/" 2>/dev/null || true
  cp "$SITE_DIR/target/bedrock.war" "$COMPOSE_DIR/ROOT.war" 2>/dev/null || true
fi

if [[ $DEPLOY -eq 1 ]]; then
  echo "Preparing to deploy artifacts to Central..."
  if [[ ! -d "$HOME/.gnupg" ]]; then
    echo "WARN: ~/.gnupg not found. GPG signing may fail inside the container."
  fi
  docker run --rm \
    -u "$DOCKER_UID:$DOCKER_GID" \
    -v "$REPO_ROOT":/workspace \
    -v "$HOME/.m2":/m2 \
    -v "$HOME/.gnupg":/gnupg \
    -e HOME=/root \
    -e MAVEN_CONFIG=/m2 \
    -e GNUPGHOME=/gnupg \
    -w /workspace \
    bedrock-build:latest \
    bash -lc "\
      set -euo pipefail; \
      mvn -e -Dmaven.repo.local=/m2/repository -DskipTests=true deploy \
    "
fi

# Optionally launch the site compose if present (opt-in)
if [[ $RUN_SITE -eq 1 ]]; then
  if [[ -d "$COMPOSE_DIR" && -f "$COMPOSE_DIR/docker-compose.yml" ]]; then
    # Ensure the test support server (Bedrock-1.x + Mongo) is stopped before launching the site
    if [[ -f "$TEST_SERVER_DIR/docker-compose.yml" ]]; then
      echo "Stopping test support server before launching site..."
      pushd "$TEST_SERVER_DIR" >/dev/null
      docker compose down || true
      popd >/dev/null
    fi
    PORT="$(derive_port)"
    PATH_SUFFIX="$(derive_path)"
    URL="http://localhost:${PORT}${PATH_SUFFIX}"
    echo "Launching browser at: $URL"
    open_browser "$URL"

    echo "Starting Docker (interactive)..."
    pushd "$COMPOSE_DIR" >/dev/null
    docker compose up --build --remove-orphans
    popd >/dev/null
  else
    echo "INFO: Site compose directory not found at $COMPOSE_DIR. Use a full build to generate it."
  fi
else
  echo "INFO: Site run skipped. Use --run-site to launch it."
fi
