#!/bin/bash

v="0.1.0"

mvn clean package -Dmaven.test.skip=true

cp target/alone-$v-SNAPSHOT-jar-with-dependencies.jar alone-$v.jar

7z a -tzip alone-$v.zip data/ alone-$v.jar

rm alone-$v.jar
