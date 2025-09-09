# Repository Guidelines

## Project Structure & Modules
- `src/main/java`: Java service code (`us.irdev.bedrock.site`). Entry: `Service`.
- `src/test/java`: JUnit 5 tests (e.g., `Service_Test.java`).
- `src/main/webapp`: JSP, `WEB-INF`, static assets; built JS published under `dist/`.
- `src/main/dist`: JavaScript source bundled to `*.mjs` during build.
- `src/main/docker`: `Dockerfile` and `docker-compose.yml` for Tomcat.
- `bin`: build/test scripts invoked by Maven (see below).
- `target`: build output (WAR, logs, docker context).

## Build, Test, and Run
- Build: `mvn clean install` — compiles Java, runs tests, generates JS (`bin/generate-sources.sh`), creates `target/bedrock.war` and updates `src/main/webapp/dist/<version>` and `latest`.
- Local run (Docker): `./build.sh` or `pushd target/docker && docker compose up --build && popd` → open `http://localhost:8082/bedrock/`.
- Unit tests: `mvn test` (calls `bin/test.sh` and JUnit). Single test: `mvn -Dtest=Service_Test test`.
- Clean: `mvn clean` (runs `bin/clean.sh`).

## Coding Style & Naming
- Java (JDK 17): 4‑space indent; packages lowercase (`us.irdev...`); classes `CamelCase`; constants `UPPER_SNAKE`; methods/fields `camelCase`. Event handlers in `Service` follow `handleEventX`.
- JavaScript (`src/main/dist`): small modules; debug/release outputs become `<name>-debug.mjs` and `<name>.mjs`. Conventions: `$` for namespace, `_` for object declaration.

## Testing Guidelines
- Framework: JUnit 5 (`org.junit.jupiter`). Place tests under mirrored packages in `src/test/java`.
- Naming: suffix `_Test.java` (example: `Service_Test.java`).
- Patterns: use `us.irdev.bedrock.servlet.Tester` to exercise servlet endpoints; keep tests fast and deterministic.

## Commit & Pull Request Guidelines
- Commits: imperative subject (“Add headers endpoint”), focused scope, reference modules/paths when helpful.
- PRs: clear description, linked issues, verification steps (e.g., curl URL) and screenshots if UI changes. Update `readme.MD` when endpoints or behavior change.

## Security & Deployment
- Do not commit secrets. For deploy to AWS ECR/ECS, create `~/.aws/bedrock.sh` exporting `AWS_ACCOUNT_ID`, `AWS_REGION`, `AWS_PROFILE`.
- Deploy: `mvn deploy` or `bin/deploy.sh` (pushes Docker image to ECR and triggers ECS service update).

