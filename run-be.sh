#!/bin/sh
mvn clean package
docker rm -f "ai-detector-be" || true
docker build -t ai-detector-be .
docker run -d --name "ai-detector-be" -p 8081:8081 --add-host host.docker.internal:host-gateway --env-file .env -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:15432/AIDB" -e AI_SERVICE_URL="http://host.docker.internal:8000" ai-detector-be