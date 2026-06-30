#!/usr/bin/env sh
set -e

GRADLE_VERSION="8.13"
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-$GRADLE_VERSION-bin"
ZIP_FILE="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
GRADLE_HOME="$DIST_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

mkdir -p "$DIST_DIR"

if [ ! -x "$GRADLE_BIN" ]; then
  echo "Downloading Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -L --fail -o "$ZIP_FILE" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_FILE" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  else
    echo "curl or wget is required to download Gradle." >&2
    exit 1
  fi
  unzip -q -o "$ZIP_FILE" -d "$DIST_DIR"
fi

cd "$APP_HOME"
exec "$GRADLE_BIN" "$@"
