#!/bin/sh
mkdir -p out
javac -d out $(find src -name "*.java") && echo "Compiled OK. Run ./run.sh"
