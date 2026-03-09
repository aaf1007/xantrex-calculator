#!/bin/bash
# Load .env and run a Maven command. Usage: ./dev.sh [maven-args]
# Examples:
#   ./dev.sh spring-boot:run
#   ./dev.sh test
#   ./dev.sh test -Dtest=UserRepositoryTest

if [ ! -f .env ]; then
  echo "Error: .env file not found. Copy .env.example to .env and fill in your credentials."
  exit 1
fi

set -a
# shellcheck source=.env
source .env
set +a

./mvnw "$@"
