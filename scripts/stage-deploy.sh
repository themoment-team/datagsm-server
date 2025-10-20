#!/bin/bash

IMAGE_NAME=datagsm-stage-server-img
CONTAINER_NAME=datagsm-stage-server
DOCKERFILE_NAME=stage.dockerfile

EXISTING_CONTAINER_ID=$(docker ps -a -q -f name=$CONTAINER_NAME)

if [ ! -z "$EXISTING_CONTAINER_ID" ]
then
  echo "현재 이전버전의 컨테이너가 있습니다. 중지하고 삭제합니다."
  docker stop $CONTAINER_NAME || true
  docker rm $CONTAINER_NAME
fi

cd /home/ec2-user/builds/
docker build -t $IMAGE_NAME -f $DOCKERFILE_NAME .

docker run -d --name $CONTAINER_NAME --network datagsm-network -p 8080:8080 $IMAGE_NAME

docker system prune -a --volumes -f
