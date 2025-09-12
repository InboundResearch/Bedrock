# Bedrock Dev Guide (Agents)

## Repo Layout
- `libraries/`: core modules (`logger`, `base`, `bag`, `secret`, `servlet-tester`, `service-base`, `database`).
- `bom/`: Bill of Materials; manages versions including `jakarta.servlet-api`.
- `site/`: reference webapp (WAR). Docker at `site/src/main/docker/`.
- `tools/`: build and upgrade tooling (`build.sh`, `preflight_upgrade.sh`, `apply_upgrade.sh`, `Dockerfile.build`, `upgrade_java_tomcat.py`).

## Requirements
- Preferred: Docker Desktop (builds run in a container).
- Optional host tools: Maven 3.9+, JDK 21, gh CLI (for PRs).

## Build & Run
- Containerized (recommended): `tools/build.sh`
  - Builds in Docker image `bedrock-build` (Maven 3.9 + Temurin 21 + Node/uglifyjs/gcc).
  - Opens `http://localhost:8082/` and runs `docker compose up --build` from `site/target/docker`.
  - Flags:
    - `--compile-only` — build without tests; does not start the test support server.
    - `--no-test-server` — skip starting the test support server.
    - `--keep-test-server` — leave the test support server running after the build.
    - `--test-server-url=...` — pass `-DTEST_SERVER_BASE_URL=...` to Maven tests.
    - `--deploy` — after a successful build, run `mvn deploy` inside the container.
    - `--run-site` — after building, stage `site/target/docker` and run the site via Docker compose.
- Direct (host): `mvn clean install` at repo root (requires JDK 21). Then `pushd site/target/docker && docker compose up --build && popd`.

### Test Support Server (during build)
- A test support server (the Bedrock 1.x service using URL-only queries) and MongoDB are required for unit tests in some modules (e.g., `bag`, `database`).
- The repo expects a sibling directory `../Bedrock-1.x` with `docker-compose.yml`, `Dockerfile`, and the WAR. `tools/build.sh` will:
  - Start `../Bedrock-1.x` via `docker compose up -d` before running tests.
  - Wait for HTTP at `http://localhost:8081/` and MongoDB at `localhost:27017`.
  - Proxy those ports into the build container, so tests using `localhost:8081` and `localhost:27017` work unchanged.
  - Tear down the test support server after the build (unless `--keep-test-server` is used).

Flags:
- `tools/build.sh --no-test-server` — skip starting the test support server.
- `tools/build.sh --keep-test-server` — leave the test support server running after the build.

Test configuration knobs:
- `TEST_SERVER_BASE_URL` (system property) — base URL for URL-only HTTP tests (default `http://localhost:8081`).
  - Example: `mvn -DTEST_SERVER_BASE_URL=http://localhost:18081 test`
  - Or via build.sh: `tools/build.sh --test-server-url=http://localhost:18081`

### Deploy from the container
- Pre-reqs on the host (no secrets in repo):
  - `~/.m2/settings.xml` with the `central` server credentials (Sonatype Central token name/secret). If using encrypted passwords, keep your `~/.m2/settings-security.xml` too.
  - `~/.gnupg` with your GPG private key for signing.
- The script mounts `~/.m2` and `~/.gnupg` and runs:
  - `mvn -e -Dmaven.repo.local=/m2/repository -DskipTests=true deploy`
- If signing prompts block in containers, consider GPG loopback mode and passphrase via a server id in `settings.xml`.

## Java/Tomcat/Servlet
- Java: 21 (enforced).
- Tomcat: 11 base image.
- Servlet: Jakarta Servlet 6 (`jakarta.servlet-api`), version managed via root property `jakarta-servlet.version` and the BOM.

## Upgrades
- Dry-run: `tools/preflight_upgrade.sh [--discover] [--parent X] [--bom Y]`
  - Creates a feature branch and shows diffs. Runs inside the build container when available.
- Apply: `tools/apply_upgrade.sh [--discover] [--no-tests] [--no-pr]`
  - Applies changes (Surefire/Enforcer, Dockerfile base image, parent, `bedrock.version`) and tests in the container. If `gh` is present, opens a PR.
  - Both scripts prefer the containerized environment; test support server bring-up is handled by `tools/build.sh` for test runs.

## Testing & Coverage
- `mvn test` runs JUnit 5; coverage via JaCoCo. Reports under `site/target/site/coverage/` for the site module.
- Coverage is scoped to Bedrock packages; JDK/internal classes are excluded.

## Commit & PRs
- Commit messages: imperative (“Upgrade Tomcat to 11.0”).
- PRs: clear description, steps to verify (URL or curl), screenshots for UI.
- Default base branch: `development`.
