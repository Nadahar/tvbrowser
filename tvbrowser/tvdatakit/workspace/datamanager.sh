#!/bin/bash

PDS_CLASSPATH=classes:lib/poi-3.0-alpha2-20060616.jar:lib/xercesImpl.jar

java -cp lib/PrimaryDataManager.jar:"$PDS_CLASSPATH" primarydatamanager.PrimaryDataManager $*
