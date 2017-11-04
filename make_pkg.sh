#!/bin/bash

v=`ls target/alone-rl-*-jar-with-dependencies.jar | awk -F"-" '{print $3}'`

mvn clean package -Dmaven.test.skip=true

cp target/alone-rl-*-jar-with-dependencies.jar alone-rl-$v.jar

7z a -tzip alone-rl-$v.zip data/ alone-rl-$v.jar

rm alone-rl-$v.jar
