#!/bin/bash
./service-library/mvnw clean deploy
./service-pom/mvnw clean deploy -DskipDockerPush=true -DskipMavenDeploy=false
