#! /usr/bin/env bash

mvn clean install && pushd target/docker && docker compose up --build --remove-orphans && popd;
