#! /usr/bin/env bash

. bin/common.sh $1

echo "Deploy: pushing docker tag $PROJECT_NAME:${PROJECT_VERSION,,} to AWS";

# get a few user params (AWS_ACCOUNT_ID, AWS_REGION, AWS_PROFILE)
. ~/.aws/$PROJECT_NAME.sh;
AWS_EXEC="aws --profile $AWS_PROFILE";

DOCKER_REPOSITORY="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$PROJECT_NAME";
echo "Deploy: Repository ($DOCKER_REPOSITORY)";
$AWS_EXEC ecr create-repository --repository-name $PROJECT_NAME || echo "Deploy: Repository for $PROJECT_NAME exists";

echo "Deploy: docker tag";
docker tag $PROJECT_NAME:${PROJECT_VERSION,,} $DOCKER_REPOSITORY;

echo "Deploy: get login password";
LOGIN_PASSWORD=$($AWS_EXEC ecr get-login-password);
#echo "Login password: $LOGIN_PASSWORD";

echo "Deploy: set login";
echo $LOGIN_PASSWORD | docker login --username AWS --password-stdin $DOCKER_REPOSITORY;

echo "Deploy: push repository";
docker push $DOCKER_REPOSITORY;

# delete old images
echo "Deploy: check for old images";
OLD_IMAGES=$($AWS_EXEC ecr list-images --repository-name $PROJECT_NAME --query 'imageIds[?imageTag!=`latest`]' --output json);

if [[ $OLD_IMAGES != "[]" ]]; then
    echo "Deploy: delete old images";
    echo $OLD_IMAGES > old_images.json;
    $AWS_EXEC ecr batch-delete-image --repository-name $PROJECT_NAME --image-ids file://old_images.json;
    rm old_images.json;
fi

# force ECS service update
echo "Deploy: force service update";
$AWS_EXEC ecs update-service --cluster $PROJECT_NAME-cluster --service $PROJECT_NAME-service --force-new-deployment;

echo "Deploy: Finished";
