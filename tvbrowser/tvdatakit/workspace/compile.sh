#!/bin/bash

. ./setClasspath.sh

javac -classpath java/PDSRunner.jar:"$PDS_CLASSPATH" -sourcepath java -d java *.java
