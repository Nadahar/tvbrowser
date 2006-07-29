@echo off

call setClasspath.cmd

java -cp lib\PDSRunner.jar;%PDS_CLASSPATH% primarydatamanager.PDSRunner %*
