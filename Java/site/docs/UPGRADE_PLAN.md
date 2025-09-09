# Java & Tomcat Upgrade Plan

## Goals
- Track latest Java LTS (default: 21) and compatible Tomcat (default: 10.1-jdk21-temurin).
- Keep build plugins current (Surefire for JUnit 5).
- Provide preflight (branch + diff) and safe apply with tests and optional PR.

## Process
1. Preflight: `tools/preflight_upgrade.sh`
   - Creates a chore branch and shows diffs. No writes.
2. Apply: `tools/apply_upgrade.sh`
   - Auto-discovers a compatible set (prefers Java 21 + Tomcat 10.1 jdk21; falls back to Java 17 on failure), updates parent and `bedrock.version` to latest releases, runs tests, commits, and optionally opens a PR via `gh`.

## What the Tool Updates
- pom.xml: sets `maven.compiler.release`, ensures `maven-surefire-plugin>=3.2.5`, adds `maven-enforcer-plugin` (require Java/Maven), updates `<parent>` to latest, and can override `bedrock.version` (BOM).
- Dockerfile: updates `FROM tomcat:<tag>`.

## Possible Future Code Changes (Not Applied Automatically)
- Servlet API: consider switching dependency to `jakarta.servlet:jakarta.servlet-api` (scope `provided`) if still using `org.apache.tomcat:tomcat-servlet-api`. This could require imports to `jakarta.servlet.*` in dependent modules.
- Java 21 language features: records, switch patterns, and sealed classes can simplify code but should be applied case-by-case.
- Module path (JPMS): optionally add `module-info.java` across Bedrock modules for strong encapsulation.

## Standalone Service (extensible design)
- Design intent: keep the service extensible by adding event handlers via subclassing `Service` (e.g., new `handleEventX` methods). Do not bake a fixed standalone binary in this repo.
- Path forward: provide a separate, optional launcher module/profile (not built by default) that boots an embedded server (Undertow/Jetty) and instantiates your `Service` subclass. Contributors add handlers by deriving from `Service`; the launcher remains generic.
