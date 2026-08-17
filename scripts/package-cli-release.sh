#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:?Usage: $0 <version>}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_DIR="$ROOT_DIR/idea-plugin"
DIST_DIR="$PROJECT_DIR/build/install/git-ai-commit"
DIST_LIB_DIR="$DIST_DIR/lib"
RELEASE_DIR="$PROJECT_DIR/build/release/cli"
RUNTIME_DIR="$DIST_DIR/runtime"

rm -rf "$RELEASE_DIR" "$RUNTIME_DIR"
mkdir -p "$RELEASE_DIR"

pushd "$PROJECT_DIR" >/dev/null
./gradlew installDist

MAIN_JAR_PATH="$(find "$DIST_LIB_DIR" -maxdepth 1 -name '*-base.jar' -print -quit)"
if [[ -z "$MAIN_JAR_PATH" ]]; then
    echo "Unable to locate the CLI main jar in $DIST_LIB_DIR" >&2
    exit 1
fi
MAIN_JAR="$(basename "$MAIN_JAR_PATH")"

JDEPS_BIN="${JAVA_HOME:-}/bin/jdeps"
if [[ ! -x "$JDEPS_BIN" ]]; then
    JDEPS_BIN="$(command -v jdeps)"
fi
if [[ -z "$JDEPS_BIN" || ! -x "$JDEPS_BIN" ]]; then
    echo "Unable to locate jdeps. Set JAVA_HOME to a JDK 21 installation." >&2
    exit 1
fi

JLINK_BIN="${JAVA_HOME:-}/bin/jlink"
if [[ ! -x "$JLINK_BIN" ]]; then
    JLINK_BIN="$(command -v jlink)"
fi
if [[ -z "$JLINK_BIN" || ! -x "$JLINK_BIN" ]]; then
    echo "Unable to locate jlink. Set JAVA_HOME to a JDK 21 installation." >&2
    exit 1
fi

MODULES="$("$JDEPS_BIN" \
    --ignore-missing-deps \
    --multi-release 21 \
    --print-module-deps \
    --class-path "$DIST_LIB_DIR/*" \
    "$MAIN_JAR_PATH")"
MODULES="${MODULES},jdk.crypto.ec"

"$JLINK_BIN" \
    --add-modules "$MODULES" \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress=2 \
    --output "$RUNTIME_DIR"

python3 - "$DIST_DIR/bin/git-ai-commit" "$DIST_DIR/bin/git-ai-commit.bat" <<'PY'
from pathlib import Path
import sys

unix_script = Path(sys.argv[1])
windows_script = Path(sys.argv[2])

unix_text = unix_script.read_text()
unix_old = """# Determine the Java command to use to start the JVM.\nif [ -n \"$JAVA_HOME\" ] ; then\n"""
unix_new = """# Prefer the bundled runtime when present.\nif [ -x \"$APP_HOME/runtime/bin/java\" ] ; then\n    JAVACMD=$APP_HOME/runtime/bin/java\nelse\n# Determine the Java command to use to start the JVM.\nif [ -n \"$JAVA_HOME\" ] ; then\n"""
if unix_old not in unix_text:
    raise SystemExit("Could not locate the Unix launcher block to patch.")
unix_text = unix_text.replace(unix_old, unix_new, 1)
unix_text = unix_text.replace(
    """# Increase the maximum file descriptors if we can.\n""",
    """fi\n\n# Increase the maximum file descriptors if we can.\n""",
    1,
)
unix_script.write_text(unix_text)

windows_text = windows_script.read_text()
windows_old = """@rem Add default JVM options here. You can also use JAVA_OPTS and GIT_AI_COMMIT_OPTS to pass JVM options to this script.\nset DEFAULT_JVM_OPTS=\n\n@rem Find java.exe\n"""
windows_new = """@rem Add default JVM options here. You can also use JAVA_OPTS and GIT_AI_COMMIT_OPTS to pass JVM options to this script.\nset DEFAULT_JVM_OPTS=\n\n@rem Prefer the bundled runtime when present.\nset BUNDLED_JAVA_EXE=%APP_HOME%\\runtime\\bin\\java.exe\nif exist \"%BUNDLED_JAVA_EXE%\" set JAVA_EXE=%BUNDLED_JAVA_EXE%\nif defined JAVA_EXE goto execute\n\n@rem Find java.exe\n"""
if windows_old not in windows_text:
    raise SystemExit("Could not locate the Windows launcher block to patch.")
windows_text = windows_text.replace(windows_old, windows_new, 1)
windows_script.write_text(windows_text)
PY

case "$(uname -s)" in
    Linux*) OS_NAME="linux" ;;
    Darwin*) OS_NAME="macos" ;;
    MINGW*|MSYS*|CYGWIN*) OS_NAME="windows" ;;
    *) OS_NAME="$(uname -s)" ;;
esac

sanitize_label() {
    printf '%s' "$1" \
        | tr '[:upper:]' '[:lower:]' \
        | tr -cs '[:alnum:]' '-' \
        | sed -e 's/^-//' -e 's/-$//'
}

OS_NAME="$(sanitize_label "$OS_NAME")"
ARCH_NAME="$(sanitize_label "$(uname -m)")"
ARTIFACT_BASE="$RELEASE_DIR/git-ai-commit-${VERSION}-${OS_NAME}-${ARCH_NAME}"

python3 - "$DIST_DIR" "$ARTIFACT_BASE" <<'PY'
import os
import shutil
import sys

src = sys.argv[1]
base = sys.argv[2]
archive = shutil.make_archive(
    base,
    "zip",
    root_dir=os.path.dirname(src),
    base_dir=os.path.basename(src),
)
PY

echo "$ARTIFACT_BASE.zip"
popd >/dev/null
