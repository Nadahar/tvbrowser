@echo off
C:
CD "%ProgramFiles(x86)%\TV-Browser"
set OPT=
set OPT=%OPT% -Dcaptureplugin.ProgramOptionPanel.beforeEvent=true
set OPT=%OPT% -Dcaptureplugin.E2MovieHelper.genres="Action,Drama,Kom�die,Krimi,Kinder,Lovestory,Serie,Science Fiction,Thriller"
REM set OPT=%OPT% -Dcaptureplugin.ProgramOptionPanel.switchToSd=true
set JAVA=%windir%\system32\java.exe
echo ------------------------------------------------------------------------------
echo Properties=%OPT%
echo ------------------------------------------------------------------------------
%JAVA% -version
echo ------------------------------------------------------------------------------
"%JAVA%" -Xms64m -Xmx768m %OPT% -Dpropertiesfile=windows.properties -jar tvbrowser.jar
if ERRORLEVEL 1  pause
