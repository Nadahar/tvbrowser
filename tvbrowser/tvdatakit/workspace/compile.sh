#!/bin/bash
PDS_CLASSPATH=classes:lib/poi-3.0-alpha2-20060616.jar:lib/xercesImpl.jar

javac -classpath lib/PDSRunner.jar:$PDS_CLASSPATH -sourcepath java -d classes java/*.java
