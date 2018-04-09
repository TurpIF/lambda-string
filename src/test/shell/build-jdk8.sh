#!/usr/bin/env sh

# Run local gitlab runner to build the lambda-string agent with the JDK8
# and persist build output (with artifact) in /tmp/output
# This script use the docker socket, so the caller should be root or in the docker group.

rm -rf /tmp/output/*
gitlab-runner exec docker \
    --post-build-script "cp -r target/* /cache/" \
    --docker-volumes /tmp/output:/cache \
    build:jdk8
