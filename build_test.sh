#!/usr/bin/env bash

# abort script on error
set -euo pipefail

JUNIT_VERSION="1.11.3"
JUNIT_JAR="junit-platform-console-standalone-${JUNIT_VERSION}.jar"
JUNIT_URL="https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/${JUNIT_JAR}"
BUILD_DIR="build"
TEST_JAR="${BUILD_DIR}/tests.jar"

mkdir -p "${BUILD_DIR}"

# download JUnit-5 runner if necessary
if [[ ! -f "${JUNIT_JAR}" ]]; then
  echo "Lade ${JUNIT_JAR} herunter..."
  curl -fsSL -o "${JUNIT_JAR}" "${JUNIT_URL}"
fi

# compile kotlin code and tests
kotlinc AssistanceLUT.kt AssistanceLUTTest.kt \
  -cp "${JUNIT_JAR}" \
  -include-runtime \
  -d "${TEST_JAR}"

# run JUnit-5 tests
java --enable-final-field-mutation=ALL-UNNAMED \
  -jar "${JUNIT_JAR}" execute \
  --class-path "${TEST_JAR}" \
  --scan-class-path
