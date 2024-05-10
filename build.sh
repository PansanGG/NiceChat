#!/bin/bash

rm -r "target"
/usr/lib/intellij-idea/plugins/maven/lib/maven3/bin/mvn package

if [ -z $1 ]; then
  exit 0
elif [ "$1" == "test" ]; then
  cp "target/NiceChat-1.1.jar" "/home/user/Desktop/servertest/plugins/nicechat.jar"
  exit 0
fi
