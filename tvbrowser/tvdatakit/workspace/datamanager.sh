#!/bin/bash

. ./setClasspath.sh

java -cp java/PrimaryDataManager.jar:"$PDS_CLASSPATH" primarydatamanager.PrimaryDataManager $*
