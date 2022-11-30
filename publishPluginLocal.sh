#!/usr/bin/env bash
# Author: wswenyue.
#Date & Time: 2022-09-26 14:35:57
#Description: publish plugin to local

./gradlew -Dorg.gradle.daemon=false \
          clean \
          EasyTools:publishToMavenLocal \
          EasyAopCfg:publishToMavenLocal \
          EasyAopASM:publishToMavenLocal \
          EasyAOPPlugin:publishToMavenLocal