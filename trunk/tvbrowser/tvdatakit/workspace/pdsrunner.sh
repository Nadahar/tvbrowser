#!/bin/bash

. ./setClasspath.sh

java -cp java/PDSRunner.jar:"$PDS_CLASSPATH" primarydatamanager.PDSRunner $*
