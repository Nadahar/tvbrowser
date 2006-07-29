@echo off

call setClasspath.cmd

java -cp lib\PrimaryDataManager.jar;%PDS_CLASSPATH% primarydatamanager.PrimaryDataManager %*
