@echo off

call setClasspath.cmd

java -cp java\PrimaryDataManager.jar;%PDS_CLASSPATH% primarydatamanager.PrimaryDataManager %*
