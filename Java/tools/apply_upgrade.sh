#!/usr/bin/env bash
set -euo pipefail

# Apply the upgrade, run tests, and create a commit. Optionally open a PR using gh.
# Usage: tools/apply_upgrade.sh [--parent 2.6.0] [--bom 2.6.0] [--discover] [--no-tests] [--no-pr]

PARENT_VERSION=""
BOM_VERSION=""
RUN_TESTS=1
OPEN_PR=1
DISCOVER=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --parent) PARENT_VERSION="$2"; shift 2;;
    --bom) BOM_VERSION="$2"; shift 2;;
    --discover) DISCOVER=1; shift;;
    --no-tests) RUN_TESTS=0; shift;;
    --no-pr) OPEN_PR=0; shift;;
    *) echo "Unknown arg: $1"; exit 2;;
  esac
done

branch="$(git branch --show-current || true)"
if [[ -z "$branch" ]]; then
  echo "Error: not on a branch. Run tools/preflight_upgrade.sh first." >&2
  exit 1
fi

echo "Applying upgrade changes on branch: $branch"
# Prefer Dockerized environment for the upgrade (python + mvn inside container)
if command -v docker >/dev/null 2>&1; then
  # Ensure build image exists
  docker image inspect bedrock-build:latest >/dev/null 2>&1 || docker build -f tools/Dockerfile.build -t bedrock-build:latest .
  DOCKER_UID="$(id -u)"; DOCKER_GID="$(id -g)"
  docker run --rm -u "$DOCKER_UID:$DOCKER_GID" \
    -v "$PWD":/workspace -v "$HOME/.m2":/m2 -e MAVEN_CONFIG=/m2 -w /workspace \
    bedrock-build:latest \
    python3 tools/upgrade_java_tomcat.py \
      --apply \
      --auto \
      ${PARENT_VERSION:+--parent-version "$PARENT_VERSION"} \
      ${BOM_VERSION:+--bedrock-bom-version "$BOM_VERSION"} \
      ${DISCOVER:+--discover-tomcat}
else
  python3 tools/upgrade_java_tomcat.py \
    --apply \
    --auto \
    ${PARENT_VERSION:+--parent-version "$PARENT_VERSION"} \
    ${BOM_VERSION:+--bedrock-bom-version "$BOM_VERSION"} \
    ${DISCOVER:+--discover-tomcat}
fi

echo "Staging changes"
# Determine paths (top-level module or 'site' submodule)
POM_PATH="pom.xml"
DOCKER_PATH="src/main/docker/Dockerfile"
if [[ ! -f "$DOCKER_PATH" && -f "site/src/main/docker/Dockerfile" ]]; then
  POM_PATH="site/pom.xml"
  DOCKER_PATH="site/src/main/docker/Dockerfile"
fi
git add "$POM_PATH" "$DOCKER_PATH"
git commit -m "chore: auto-upgrade Java/Tomcat${BOM_VERSION:+; set bedrock.version to ${BOM_VERSION}}"

if [[ $RUN_TESTS -eq 1 ]]; then
  echo "Running build and tests"
  if command -v docker >/dev/null 2>&1; then
    DOCKER_UID="$(id -u)"; DOCKER_GID="$(id -g)"
    docker run --rm -u "$DOCKER_UID:$DOCKER_GID" \
      -v "$PWD":/workspace -v "$HOME/.m2":/m2 -e MAVEN_CONFIG=/m2 -w /workspace \
      bedrock-build:latest \
      mvn -q -e -Dmaven.repo.local=/m2/repository -DskipTests=false clean test
  else
    mvn -q -e -DskipTests=false clean test
  fi
fi

if [[ $OPEN_PR -eq 1 ]]; then
  if command -v gh >/dev/null 2>&1; then
    base_branch="$(git remote show origin 2>/dev/null | awk '/HEAD branch/ {print $NF}')"
    base_branch="${base_branch:-main}"
    echo "Opening PR to $base_branch via gh"
    gh pr create -B "$base_branch" -H "$branch" \
      -t "chore: Auto-upgrade Java/Tomcat" \
      -b "This PR updates maven.compiler.release, ensures a modern Surefire and Enforcer, updates Dockerfile base image (optionally discovered latest), and refreshes parent and bedrock.version. No Java source changes."
  else
    echo "gh CLI not found. Push and open a PR manually:"
    echo "  git push -u origin $branch"
  fi
fi

echo "Done."
