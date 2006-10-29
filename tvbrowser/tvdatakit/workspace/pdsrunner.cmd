@echo off

call setClasspath.cmd

java -Xmx256m -Djava.awt.headless=true -cp lib\PDSRunner.jar;%PDS_CLASSPATH% primarydatamanager.PDSRunner %*
