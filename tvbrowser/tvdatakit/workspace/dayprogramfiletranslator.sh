#!/bin/bash

PDS_CLASSPATH=classes:lib/poi-2.5-final-20040302.jar:lib/xercesImpl.jar

java -cp lib/PrimaryDataManager.jar:"$PDS_CLASSPATH" primarydatamanager.DayProgramFileTranslator $*
