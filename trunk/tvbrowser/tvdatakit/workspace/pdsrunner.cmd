@echo off

call setClasspath.cmd

java -cp java\PDSRunner.jar;%PDS_CLASSPATH% primarydatamanager.PDSRunner %*
