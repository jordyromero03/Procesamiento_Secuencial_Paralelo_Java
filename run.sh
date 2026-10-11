#!/usr/bin/env bash
# Compila y ejecuta la práctica.
# Uso: ./run.sh [archivo] [palabra] [secuencia] [repeticiones]
# Ej.: ./run.sh                                   (valores por defecto)
#      ./run.sh data/quijote.txt Sancho mente 10
set -e
cd "$(dirname "$0")"

javac -d out src/*.java
java -cp out Main "$@"
