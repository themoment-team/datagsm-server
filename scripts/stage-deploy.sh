#!/bin/bash

echo "> DataGSM Multi-Module Staging Deployment Start"

cd /home/ec2-user/builds/

echo "> Stopping and removing existing containers..."
docker compose -f compose.stage.yaml down

echo "> Cleaning up unused Docker resources..."
docker system prune -a --volumes -f

echo "> Building and starting containers..."
docker compose -f compose.stage.yaml up -d --build

echo "> Deployment completed successfully"
