#!/bin/bash
# Build script for Java 21 Todo App

export JAVA_HOME=/Users/Farid/Library/Java/JavaVirtualMachines/graalvm-ce-21.0.2/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java version:"
java -version
echo ""

mvn "$@"
