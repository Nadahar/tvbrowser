@echo off

call setEnv.cmd

set PATH=%PATH%;%JAVA_HOME%\bin

cd java
javac -classpath poi-2.5-final-20040302.jar;PDSRunner.jar ExcelPDS.java
cd ..

pause