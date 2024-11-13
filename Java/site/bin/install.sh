#! /usr/bin/env bash

. bin/common.sh $1

# exit on any error
set -e

# the src and target dirs
TARGET_DIR="$PROJECT_DIR/target";

# docker setup
echo "Install: building docker tag $PROJECT_NAME:${PROJECT_VERSION,,}";

cp -r $PROJECT_DIR/src/main/docker $TARGET_DIR/docker
cp $TARGET_DIR/bedrock.war $TARGET_DIR/docker/ROOT.war
pushd $TARGET_DIR/docker
docker build --tag "$PROJECT_NAME:${PROJECT_VERSION,,}" .
popd

echo "Install: Finished";
