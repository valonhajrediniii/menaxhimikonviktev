#!/usr/bin/env sh

# Lightweight Maven wrapper using the bundled distribution in this repository.
set -e

BASEDIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MVN="$BASEDIR/.maven/apache-maven-3.9.9/bin/mvn"
JDK_HOME="$BASEDIR/.jdk/jdk-21.0.11+10"

if [ -d "$JDK_HOME" ]; then
  export JAVA_HOME="$JDK_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if [ ! -x "$MVN" ]; then
  echo "Bundled Maven not found at: $MVN" >&2
  echo "Extract .maven/maven.zip or install Maven globally." >&2
  exit 1
fi

exec "$MVN" "$@"