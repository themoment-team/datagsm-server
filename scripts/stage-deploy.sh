#!/bin/bash

echo "> DataGSM Multi-Module Staging Deployment Start"

WEB_CONTAINER=datagsm-web-stage
AUTH_CONTAINER=datagsm-auth-stage
RESOURCE_CONTAINER=datagsm-resource-stage

WEB_IMAGE=datagsm-web-stage-img
AUTH_IMAGE=datagsm-auth-stage-img
RESOURCE_IMAGE=datagsm-resource-stage-img

stop_and_remove_container() {
  local CONTAINER_NAME=$1
  echo "> Checking container: $CONTAINER_NAME"

  EXISTING_ID=$(docker ps -a -q -f name=$CONTAINER_NAME)

  if [ ! -z "$EXISTING_ID" ]; then
    echo "> Stopping and removing container: $CONTAINER_NAME"
    docker stop $CONTAINER_NAME || true
    docker rm $CONTAINER_NAME
  fi
}

stop_and_remove_container $WEB_CONTAINER
stop_and_remove_container $AUTH_CONTAINER
stop_and_remove_container $RESOURCE_CONTAINER

cd /home/ec2-user/builds/

echo "> Building Docker images..."
docker build -t $WEB_IMAGE -f datagsm-web/stage.dockerfile ./datagsm-web
docker build -t $AUTH_IMAGE -f datagsm-authorization/stage.dockerfile ./datagsm-authorization
docker build -t $RESOURCE_IMAGE -f datagsm-resource/stage.dockerfile ./datagsm-resource

echo "> Starting Docker containers..."
docker run -d --name $WEB_CONTAINER -p 8080:8080 $WEB_IMAGE
docker run -d --name $AUTH_CONTAINER -p 8081:8081 $AUTH_IMAGE
docker run -d --name $RESOURCE_CONTAINER -p 8082:8082 $RESOURCE_IMAGE

echo "> Cleaning up unused Docker resources..."
docker system prune -a --volumes -f

echo "> Deployment completed successfully"
