#! /usr/bin/env bash

BIN_PATH="`dirname \"$0\"`";
BEDROCK_PATH="`( cd \"$BIN_PATH/.." && pwd )`";
echo $BEDROCK_PATH;

echo "common";
ln -nsf $BEDROCK_PATH/source/common common;

echo "bag";
ln -nsf $BEDROCK_PATH/source/bag bag;

echo "text";
ln -nsf $BEDROCK_PATH/source/text text;

echo "test";
ln -nsf $BEDROCK_PATH/source/test test;

echo "+test-common";
ln -nsf $BEDROCK_PATH/source/+test-common +test-common;

echo "+test-bag";
ln -nsf $BEDROCK_PATH/source/+test-bag +test-bag;

echo "+test-text";
ln -nsf $BEDROCK_PATH/source/+test-text +test-text;
