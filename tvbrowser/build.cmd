@echo off

rem Checking JAVA_HOME
if "%JAVA_HOME%" == "" goto noJavaHome
goto setClasspath

:noJavaHome
echo The environmental variable JAVA_HOME is not set.

goto end

:setClasspath
rem Set the classpath
set CLASSPATH=.\lib\ant.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar

echo Using CLASSPATH: %CLASSPATH%

rem Run application
%JAVA_HOME%\bin\java.exe -cp %CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 -buildfile build.xml

:end
