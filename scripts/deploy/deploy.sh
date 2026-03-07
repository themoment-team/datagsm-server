#!/bin/bash

DG_NAME="$DEPLOYMENT_GROUP_NAME"

if [ "$DG_NAME" == "datagsm-prod-server" ]; then
  chmod +x /home/ec2-user/builds/scripts/deploy/prod-deploy.sh
  /home/ec2-user/builds/scripts/deploy/prod-deploy.sh

elif [ "$DG_NAME" == "datagsm-stage-server" ]; then
  chmod +x /home/ec2-user/builds/scripts/deploy/stage-deploy.sh
  /home/ec2-user/builds/scripts/deploy/stage-deploy.sh

else
  echo "알 수 없는 배포 그룹입니다: $DG_NAME"
  exit 1
fi
