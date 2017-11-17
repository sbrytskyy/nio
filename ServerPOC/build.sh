#!/usr/bin/env bash

mvn clean assembly:assembly -DskipTests

cp target/ServerPOC-0.0.1-SNAPSHOT-jar-with-dependencies.jar target/ServerPOC.jar
