#!/bin/bash

echo "> DataGSM Multi-Module Deployment Start" >> /home/ec2-user/deploy.log

cd /home/ec2-user/builds/

echo "> Stopping and removing existing containers..." >> /home/ec2-user/deploy.log
docker compose -f compose.prod.yaml down >> /home/ec2-user/deploy.log 2>&1

echo "> Cleaning up unused Docker resources..."
docker system prune -a --volumes -f

echo "> Building and starting containers..." >> /home/ec2-user/deploy.log
docker compose -f compose.prod.yaml up -d --build >> /home/ec2-user/deploy.log 2>&1


echo "> Deployment completed successfully" >> /home/ec2-user/deploy.log
