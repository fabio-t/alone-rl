#!/bin/bash

mvn clean package -Dmaven.test.skip=true

cp target/alone-0.0.1-SNAPSHOT-jar-with-dependencies.jar alone-0.0.1.jar

7z a -tzip alone.zip data/ alone-0.0.1.jar

rm alone-0.0.1.jar
