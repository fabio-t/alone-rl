#!/bin/bash

v="0.1.0"

mvn clean package -Dmaven.test.skip=true

cp target/alone-rl-$v-SNAPSHOT-jar-with-dependencies.jar alone-rl-$v.jar

7z a -tzip alone-rl-$v.zip data/ alone-rl-$v.jar

rm alone-rl-$v.jar
