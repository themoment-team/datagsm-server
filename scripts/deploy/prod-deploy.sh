#!/bin/bash

exec >> /home/ec2-user/deploy.log 2>&1

echo "> DataGSM Multi-Module Deployment Start"

cd /home/ec2-user/builds/ || exit 1

echo "> Stopping and removing existing containers..."
docker compose -f compose.prod.yaml down

echo "> Cleaning up unused Docker resources..."
docker system prune -a --volumes -f

echo "> Building and starting containers..."
docker compose -f compose.prod.yaml up -d --build

echo "> Deployment completed successfully"
