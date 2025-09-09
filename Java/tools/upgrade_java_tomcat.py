#!/usr/bin/env python3
"""
Upgrade helper for Java and Tomcat in this repo.

Actions (dry-run by default):
- Set Java release (maven.compiler.release) in pom.xml.
- Ensure a modern Surefire plugin (for JUnit 5 on newer JDKs).
- Optionally bump parent version.
- Update Tomcat base image tag in src/main/docker/Dockerfile.

Usage examples:
  python3 tools/upgrade_java_tomcat.py --java-release 21 --tomcat-tag 10.1-jdk21-temurin
  python3 tools/upgrade_java_tomcat.py --apply --java-release 21 --tomcat-tag 10.1-jdk21-temurin
  python3 tools/upgrade_java_tomcat.py --apply --bedrock-bom-version 2.6.0
  python3 tools/upgrade_java_tomcat.py --apply --auto   # discover compatible latest set

Notes:
- No Java source changes are made.
- Use --switch-servlet-api to suggest/prepare dependency changes (no-op by default).
"""

import argparse
import pathlib
import re
import sys
from typing import Tuple, Optional
import subprocess
import json
import urllib.request
import urllib.error

ROOT = pathlib.Path(__file__).resolve().parents[1]
# Resolve target module: prefer current ROOT if it has Dockerfile, else try known module 'site'
ROOT_TARGET = ROOT
if not (ROOT / "src/main/docker/Dockerfile").exists():
    candidate = ROOT / "site"
    if (candidate / "src/main/docker/Dockerfile").exists():
        ROOT_TARGET = candidate

POM = ROOT_TARGET / "pom.xml"
DOCKERFILE = ROOT_TARGET / "src/main/docker/Dockerfile"


def read_text(p: pathlib.Path) -> str:
    return p.read_text(encoding="utf-8")


def write_text(p: pathlib.Path, s: str) -> None:
    p.write_text(s, encoding="utf-8")


def diff_str(old: str, new: str, path: str) -> str:
    import difflib
    lines = difflib.unified_diff(old.splitlines(True), new.splitlines(True), fromfile=path+" (old)", tofile=path+" (new)")
    return "".join(lines)


def ensure_property(pom: str, name: str, value: str) -> Tuple[str, bool]:
    changed = False
    # Ensure <properties> exists
    if "<properties>" not in pom:
        # Insert <properties> after <name> or before <dependencyManagement>/</parent>
        insertion_point = pom.find("</name>")
        if insertion_point == -1:
            insertion_point = pom.find("</parent>")
        if insertion_point == -1:
            insertion_point = pom.find("<dependencies>")
        if insertion_point == -1:
            insertion_point = 0
        block = f"\n    <properties>\n        <{name}>{value}</{name}>\n    </properties>\n"
        pom = pom[:insertion_point+len("</name>") if insertion_point else 0] + block + pom[insertion_point+len("</name>") if insertion_point else 0:]
        return pom, True

    # Properties exists: replace or insert
    prop_re = re.compile(rf"(<properties>[\s\S]*?)(<{name}>)(.*?)(</{name}>)([\s\S]*?</properties>)", re.M)
    m = prop_re.search(pom)
    if m:
        if m.group(3) != value:
            pom = pom[: m.start(3) ] + value + pom[ m.end(3) : ]
            changed = True
    else:
        # Insert before </properties>
        pom = pom.replace("</properties>", f"        <{name}>{value}</{name}>\n    </properties>")
        changed = True
    return pom, changed


def ensure_surefire(pom: str, version: str = "3.2.5") -> Tuple[str, bool]:
    changed = False
    # Ensure a surefire plugin block in <build><plugins>
    # Very lightweight: if surefire present, bump version; else add minimal plugin
    if "maven-surefire-plugin" in pom:
        pom_new = re.sub(r"(<artifactId>maven-surefire-plugin</artifactId>\s*<version>)([^<]+)(</version>)",
                         rf"\g<1>{version}\g<3>", pom, flags=re.M)
        if pom_new != pom:
            changed = True
            pom = pom_new
    else:
        build_plugins_re = re.compile(r"<build>\s*<finalName>[\s\S]*?</finalName>\s*<plugins>", re.M)
        m = build_plugins_re.search(pom)
        plugin_xml = (
            f"\n            <plugin>\n"
            f"                <groupId>org.apache.maven.plugins</groupId>\n"
            f"                <artifactId>maven-surefire-plugin</artifactId>\n"
            f"                <version>{version}</version>\n"
            f"            </plugin>\n"
        )
        if m:
            insert_at = m.end()
            pom = pom[:insert_at] + plugin_xml + pom[insert_at:]
            changed = True
        else:
            # Fallback: append at end of <plugins>
            pom = pom.replace("</plugins>", plugin_xml + "\n        </plugins>")
            changed = True
    return pom, changed


def maybe_update_parent(pom: str, parent_version: Optional[str]) -> Tuple[str, bool]:
    changed = False
    if not parent_version:
        return pom, changed
    parent_re = re.compile(r"(<parent>[\s\S]*?<version>)([^<]+)(</version>[\s\S]*?</parent>)", re.M)
    m = parent_re.search(pom)
    if m and m.group(2) != parent_version:
        pom = pom[: m.start(2) ] + parent_version + pom[ m.end(2) : ]
        changed = True
    return pom, changed


def update_dockerfile(df: str, tag: str) -> Tuple[str, bool]:
    changed = False
    new_line = f"FROM tomcat:{tag}"
    df_new = re.sub(r"^FROM\s+tomcat:.*$", new_line, df, flags=re.M)
    if df_new != df:
        changed = True
        df = df_new
    return df, changed


def ensure_enforcer(pom: str, java_release: str, maven_version: str = "3.9.6") -> Tuple[str, bool]:
    changed = False
    if "maven-enforcer-plugin" in pom:
        pom_new = re.sub(r"(<requireJavaVersion>\s*<version>)([^<]+)(</version>)",
                         rf"\g<1>[{java_release},)\g<3>", pom, flags=re.M)
        if pom_new != pom:
            changed = True
            pom = pom_new
        return pom, changed
    plugin_xml = (
        "\n            <plugin>\n"
        "                <groupId>org.apache.maven.plugins</groupId>\n"
        "                <artifactId>maven-enforcer-plugin</artifactId>\n"
        "                <version>3.5.0</version>\n"
        "                <executions>\n"
        "                    <execution>\n"
        "                        <id>enforce</id>\n"
        "                        <goals><goal>enforce</goal></goals>\n"
        "                        <configuration>\n"
        "                            <rules>\n"
        "                                <requireJavaVersion>\n"
        f"                                    <version>[{java_release},)</version>\n"
        "                                </requireJavaVersion>\n"
        "                                <requireMavenVersion>\n"
        f"                                    <version>[{maven_version},)</version>\n"
        "                                </requireMavenVersion>\n"
        "                            </rules>\n"
        "                        </configuration>\n"
        "                    </execution>\n"
        "                </executions>\n"
        "            </plugin>\n"
    )
    build_plugins_re = re.compile(r"<build>\s*<finalName>[\s\S]*?</finalName>\s*<plugins>", re.M)
    m = build_plugins_re.search(pom)
    if m:
        insert_at = m.end()
        pom = pom[:insert_at] + plugin_xml + pom[insert_at:]
        changed = True
    else:
        pom = pom.replace("</plugins>", plugin_xml + "\n        </plugins>")
        changed = True
    return pom, changed


def fetch_all_pages(url: str) -> list[dict]:
    """Fetch paginated results from Docker Hub v2 API."""
    results: list[dict] = []
    while url:
        req = urllib.request.Request(url, headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            results.extend(data.get("results", []))
            url = data.get("next")
    return results


def discover_latest_tomcat_tag(prefer_java: Optional[str] = None) -> Optional[str]:
    """Return best tomcat tag like '10.1-jdk21-temurin'. Prefers highest stable line and JDK.

    Strategy:
    - Query Docker Hub library/tomcat tags.
    - Consider tags matching 'X.Y-jdkNN-temurin' (no suffixes).
    - Choose highest X.Y; within that, choose highest NN.
    - If prefer_java provided, try to honor it within highest X.Y; else pick highest NN.
    """
    base = "https://registry.hub.docker.com/v2/repositories/library/tomcat/tags?page_size=100"
    try:
        tags = fetch_all_pages(base)
    except (urllib.error.URLError, TimeoutError, urllib.error.HTTPError):
        return None

    pat = re.compile(r"^(?P<ver>\d+\.\d+)-jdk(?P<jdk>\d+)-temurin$")
    candidates: list[tuple[tuple[int, int], int, str]] = []
    for t in tags:
        name = t.get("name", "")
        m = pat.match(name)
        if not m:
            continue
        maj, minr = m.group("ver").split(".")
        jdk = int(m.group("jdk"))
        candidates.append(((int(maj), int(minr)), jdk, name))
    if not candidates:
        return None
    # Sort by version desc then jdk desc
    candidates.sort(key=lambda x: (x[0][0], x[0][1], x[1]), reverse=True)
    if prefer_java:
        pj = int(prefer_java)
        # find highest tomcat ver that has pj; else fallback to top
        for ver_tuple, jdk, name in candidates:
            if jdk == pj:
                # Ensure this ver is the highest available ver that has pj
                chosen_ver = ver_tuple
                # find max jdk within that ver; if pj exists, choose pj
                return name
    # Otherwise pick the first (max ver, max jdk)
    return candidates[0][2]


def run(cmd: list[str]) -> int:
    try:
        res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False, text=True)
        print(res.stdout)
        return res.returncode
    except FileNotFoundError:
        print(f"WARN: Command not found: {' '.join(cmd)}")
        return 127


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--java-release", type=str, default="21", help="Java release to target (maven.compiler.release)")
    parser.add_argument("--tomcat-tag", type=str, default="", help="Docker base image tag for tomcat (omit to auto-derive from pom + java)")
    parser.add_argument("--parent-version", type=str, default=None, help="Optionally set <parent><version>")
    parser.add_argument("--switch-servlet-api", action="store_true", help="Suggest switching to jakarta.servlet-api (no code changes)")
    parser.add_argument("--bedrock-bom-version", type=str, default=None, help="Override <bedrock.version> property used by BOM")
    parser.add_argument("--apply", action="store_true", help="Write changes to disk (default dry-run)")
    parser.add_argument("--auto", action="store_true", help="Discover compatible latest versions and verify with a build")
    parser.add_argument("--discover-tomcat", action="store_true", help="Discover latest stable tomcat tag (requires network)")
    args = parser.parse_args()

    if not POM.exists():
        print(f"ERROR: pom.xml not found at {POM}", file=sys.stderr)
        return 2
    if not DOCKERFILE.exists():
        print(f"ERROR: Dockerfile not found at {DOCKERFILE}", file=sys.stderr)
        return 2

    pom_old = read_text(POM)
    df_old = read_text(DOCKERFILE)
    changed_any = False

    pom_new, ch1 = ensure_property(pom_old, "maven.compiler.release", args.java_release)
    pom_new, ch2 = ensure_surefire(pom_new)
    pom_new, ch_enf = ensure_enforcer(pom_new, args.java_release)
    pom_new, ch3 = maybe_update_parent(pom_new, args.parent_version)
    if args.bedrock_bom_version:
        pom_new, chb = ensure_property(pom_new, "bedrock.version", args.bedrock_bom_version)
    else:
        chb = False

    # Determine Java release baseline from POM if present
    mcr_match = re.search(r"<maven\.compiler\.release>\s*([^<]+)\s*</maven\.compiler\.release>", pom_old)
    if mcr_match and not (args.apply and args.auto):
        # If not in auto-apply flow, prefer existing declared release as baseline
        args.java_release = mcr_match.group(1).strip()

    # Discover Tomcat tag optionally, else derive from pom
    tomcat_tag = args.tomcat_tag.strip()
    if args.discover_tomcat:
        discovered = discover_latest_tomcat_tag(prefer_java=args.java_release)
        if discovered:
            tomcat_tag = discovered
            # align java release with the tag's jdk
            m = re.match(r"^\d+\.\d+-jdk(\d+)-temurin$", discovered)
            if m:
                args.java_release = m.group(1)
        else:
            print("WARN: Unable to fetch latest tomcat tag; using provided/default.")
    elif not tomcat_tag:
        # Offline derivation from pom property tomcat.version
        # Try root pom first (may contain <tomcat.version>), then current target pom
        tomcat_version = None
        try:
            root_pom_text = read_text(ROOT / "pom.xml")
        except Exception:
            root_pom_text = ""
        for txt in (root_pom_text, pom_old):
            if not txt:
                continue
            m = re.search(r"<tomcat\.version>\s*([0-9]+\.[0-9]+)\.[0-9]+\s*</tomcat\.version>", txt)
            if m:
                tomcat_version = m.group(1)
                break
        if not tomcat_version:
            # Fallback to current Dockerfile if it matches expected pattern
            m = re.search(r"^FROM\s+tomcat:([^\n]+)$", df_old, flags=re.M)
            tomcat_tag = m.group(1) if m else f"10.1-jdk{args.java_release}-temurin"
        else:
            tomcat_tag = f"{tomcat_version}-jdk{args.java_release}-temurin"

    df_new, ch4 = update_dockerfile(df_old, tomcat_tag)

    # Optional guidance: servlet API
    if args.switch_servlet_api:
        print("Note: You enabled --switch-servlet-api. If the project still depends on org.apache.tomcat:tomcat-servlet-api,\n"
              "consider switching to jakarta.servlet:jakarta.servlet-api (scope provided). This may require import updates\n"
              "to jakarta.servlet.* in dependent modules.")

    changed_any = ch1 or ch2 or ch_enf or ch3 or ch4 or chb

    if not changed_any and not args.apply:
        # Dry run with no diffs to show
        print("No changes detected (files already match requested settings).")
        return 0

    pom_diff = diff_str(pom_old, pom_new, str(POM))
    df_diff = diff_str(df_old, df_new, str(DOCKERFILE))

    if not args.apply:
        print(pom_diff)
        print(df_diff)
        return 0

    # Apply file changes if any
    if changed_any:
        write_text(POM, pom_new)
        write_text(DOCKERFILE, df_new)
        print("Applied updates:")
        if ch1:
            print(f"- Set maven.compiler.release={args.java_release}")
        if ch2:
            print("- Ensured maven-surefire-plugin version >= 3.2.5")
        if ch_enf:
            print("- Ensured maven-enforcer-plugin with Java/Maven requirements")
        if ch3:
            print(f"- Updated parent version to {args.parent_version}")
        if ch4:
            print(f"- Updated Dockerfile base image to tomcat:{tomcat_tag}")
        if chb:
            print(f"- Set bedrock.version={args.bedrock_bom_version}")

    # Auto-discovery/upgrade flow: run even if no direct file edits were needed
    if args.auto:
        print("[auto] Attempting build with current settings...")
        rc = run(["mvn", "-U", "-q", "-DskipTests", "clean", "verify"])
        if rc != 0:
            print("[auto] Build failed. Falling back to Java 17 + Tomcat 10.1-jdk17-temurin...")
            pom_txt = read_text(POM)
            pom_txt, _ = ensure_property(pom_txt, "maven.compiler.release", "17")
            pom_txt, _ = ensure_enforcer(pom_txt, "17")
            write_text(POM, pom_txt)
            df_txt = read_text(DOCKERFILE)
            df_txt, _ = update_dockerfile(df_txt, "10.1-jdk17-temurin")
            write_text(DOCKERFILE, df_txt)
            rc = run(["mvn", "-U", "-q", "-DskipTests", "clean", "verify"])  # try again
            if rc != 0:
                print("[auto] Build failed on fallback as well. Inspect logs above.", file=sys.stderr)
                return rc
        print("[auto] Updating parent to latest release...")
        run(["mvn", "-U", "-q", "-DgenerateBackupPoms=false", "versions:update-parent", "-DallowSnapshots=false"])  # latest
        print("[auto] Updating bedrock.version property to latest release...")
        run(["mvn", "-U", "-q", "-DgenerateBackupPoms=false", "versions:update-properties", "-DincludeProperties=bedrock.version", "-DallowSnapshots=false"])
        print("[auto] Final build and tests...")
        rc = run(["mvn", "-U", "-q", "-DskipTests=false", "clean", "test"])
        if rc != 0:
            print("[auto] Tests failed after upgrade. Consider pinning previous minor versions.", file=sys.stderr)
            return rc
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
