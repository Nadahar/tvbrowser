@echo off

call setClasspath.cmd

javac -classpath lib\PDSRunner.jar;%PDS_CLASSPATH% -sourcepath java -d java java/*.java
