@echo off

call setClasspath.cmd

javac -classpath java/PDSRunner.jar;%PDS_CLASSPATH% -sourcepath java -d java SimplePDS.java ExcelPDS.java XmlTvPDS.java
