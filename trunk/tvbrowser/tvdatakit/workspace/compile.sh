#!/bin/bash
PDS_CLASSPATH=classes:lib/poi-2.5-final-20040302.jar:lib/xercesImpl.jar

javac -classpath lib/PDSRunner.jar:$PDS_CLASSPATH -sourcepath java -d classes java/*.java
