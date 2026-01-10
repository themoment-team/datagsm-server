#!/bin/bash

echo "> DataGSM Multi-Module Deployment Start" >> /home/ec2-user/deploy.log

WEB_CONTAINER=datagsm-web-prod
AUTH_CONTAINER=datagsm-auth-prod
RESOURCE_CONTAINER=datagsm-resource-prod

WEB_IMAGE=datagsm-web-prod-img
AUTH_IMAGE=datagsm-auth-prod-img
RESOURCE_IMAGE=datagsm-resource-prod-img

stop_and_remove_container() {
  local CONTAINER_NAME=$1
  echo "> Checking container: $CONTAINER_NAME" >> /home/ec2-user/deploy.log

  RUNNING_ID=$(docker ps -q -f name=$CONTAINER_NAME)
  EXISTING_ID=$(docker ps -a -q -f name=$CONTAINER_NAME)

  if [ ! -z "$RUNNING_ID" ]; then
    echo "> Stopping container: $CONTAINER_NAME" >> /home/ec2-user/deploy.log
    docker stop $CONTAINER_NAME
  fi

  if [ ! -z "$EXISTING_ID" ]; then
    echo "> Removing container: $CONTAINER_NAME" >> /home/ec2-user/deploy.log
    docker rm $CONTAINER_NAME
  fi
}

stop_and_remove_container $WEB_CONTAINER
stop_and_remove_container $AUTH_CONTAINER
stop_and_remove_container $RESOURCE_CONTAINER

cd /home/ec2-user/builds/

echo "> Building Docker images..." >> /home/ec2-user/deploy.log
docker build -t $WEB_IMAGE -f datagsm-web/prod.dockerfile ./datagsm-web
docker build -t $AUTH_IMAGE -f datagsm-authorization/prod.dockerfile ./datagsm-authorization
docker build -t $RESOURCE_IMAGE -f datagsm-resource/prod.dockerfile ./datagsm-resource

echo "> Starting Docker containers..." >> /home/ec2-user/deploy.log
docker run -d --name $WEB_CONTAINER -p 8080:8080 $WEB_IMAGE
docker run -d --name $AUTH_CONTAINER -p 8081:8081 $AUTH_IMAGE
docker run -d --name $RESOURCE_CONTAINER -p 8082:8082 $RESOURCE_IMAGE

echo "> Deployment completed successfully" >> /home/ec2-user/deploy.log
