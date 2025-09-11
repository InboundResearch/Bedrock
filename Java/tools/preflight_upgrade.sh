#!/usr/bin/env bash
set -euo pipefail

# Preflight: show proposed diffs and stage a branch without applying changes.
# Usage: tools/preflight_upgrade.sh [--java 21] [--tomcat 10.1-jdk21-temurin] [--parent 2.6.0] [--bom 2.6.0] [--discover]

JAVA_RELEASE="21"
TOMCAT_TAG=""
PARENT_VERSION=""
BOM_VERSION=""
DISCOVER=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --java) JAVA_RELEASE="$2"; shift 2;;
    --tomcat) TOMCAT_TAG="$2"; shift 2;;
    --parent) PARENT_VERSION="$2"; shift 2;;
    --bom) BOM_VERSION="$2"; shift 2;;
    --discover) DISCOVER=1; shift;;
    *) echo "Unknown arg: $1"; exit 2;;
  esac
done

branch="chore/upgrade-auto-java${JAVA_RELEASE}-tomcat-${TOMCAT_TAG//[^a-zA-Z0-9]/}";

echo "Ensuring clean working tree..."
if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Error: uncommitted changes present. Please commit or stash first." >&2
  exit 1
fi

echo "Creating branch: $branch"
git switch -c "$branch"

echo "Running dry-run upgrade to display diffs..."
# Prefer Dockerized environment if available
if command -v docker >/dev/null 2>&1 && docker image inspect bedrock-build:latest >/dev/null 2>&1 || docker build -q -f tools/Dockerfile.build -t bedrock-build:latest . >/dev/null; then
  DOCKER_UID="$(id -u)"; DOCKER_GID="$(id -g)"
  if [[ $DISCOVER -eq 1 ]]; then
    docker run --rm -u "$DOCKER_UID:$DOCKER_GID" \
      -v "$PWD":/workspace -v "$HOME/.m2":/m2 -e MAVEN_CONFIG=/m2 -w /workspace \
      bedrock-build:latest \
      python3 tools/upgrade_java_tomcat.py \
        ${JAVA_RELEASE:+--java-release "$JAVA_RELEASE"} \
        ${PARENT_VERSION:+--parent-version "$PARENT_VERSION"} \
        ${BOM_VERSION:+--bedrock-bom-version "$BOM_VERSION"} \
        --discover-tomcat
  else
    docker run --rm -u "$DOCKER_UID:$DOCKER_GID" \
      -v "$PWD":/workspace -v "$HOME/.m2":/m2 -e MAVEN_CONFIG=/m2 -w /workspace \
      bedrock-build:latest \
      python3 tools/upgrade_java_tomcat.py \
        --java-release "$JAVA_RELEASE" \
        ${TOMCAT_TAG:+--tomcat-tag "$TOMCAT_TAG"} \
        ${PARENT_VERSION:+--parent-version "$PARENT_VERSION"} \
        ${BOM_VERSION:+--bedrock-bom-version "$BOM_VERSION"}
  fi
else
  if [[ $DISCOVER -eq 1 ]]; then
    python3 tools/upgrade_java_tomcat.py \
      ${JAVA_RELEASE:+--java-release "$JAVA_RELEASE"} \
      ${PARENT_VERSION:+--parent-version "$PARENT_VERSION"} \
      ${BOM_VERSION:+--bedrock-bom-version "$BOM_VERSION"} \
      --discover-tomcat
  else
    python3 tools/upgrade_java_tomcat.py \
      --java-release "$JAVA_RELEASE" \
      ${TOMCAT_TAG:+--tomcat-tag "$TOMCAT_TAG"} \
      ${PARENT_VERSION:+--parent-version "$PARENT_VERSION"} \
      ${BOM_VERSION:+--bedrock-bom-version "$BOM_VERSION"}
  fi
fi

echo
echo "Preflight complete. If the diff looks good, run:"
echo "  tools/apply_upgrade.sh ${PARENT_VERSION:+--parent $PARENT_VERSION} ${BOM_VERSION:+--bom $BOM_VERSION} ${DISCOVER:+--discover}"
