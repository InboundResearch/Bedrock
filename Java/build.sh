#! /usr/bin/env bash

mvn clean install && pushd site/ && build.sh && popd;
