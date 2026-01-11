#!/bin/bash

echo "> DataGSM Multi-Module Staging Deployment Start"

cd /home/ec2-user/builds/

echo "> Stopping and removing existing containers..."
docker compose -f compose.stage.yaml down

echo "> Building and starting containers..."
docker compose -f compose.stage.yaml up -d --build

echo "> Cleaning up unused Docker resources..."
docker system prune -a --volumes -f

echo "> Deployment completed successfully"
