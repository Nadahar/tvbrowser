#!/bin/bash

PDS_CLASSPATH=classes:lib/poi-3.0-alpha2-20060616.jar:lib/xercesImpl.jar

java -Xmx256m -Djava.awt.headless=true -cp lib/PDSRunner.jar:"$PDS_CLASSPATH" primarydatamanager.PDSRunner $*
