rem attribute entfernen
attrib -r -s -h /s

rem ordner/dateien löschen
rd /s /q c:\MADDE\0.6.14\home\JBergmann\tv-browserm\src\debug
rd /s /q c:\MADDE\0.6.14\home\JBergmann\tv-browserm\src\release
rd /s /q c:\MADDE\0.6.14\home\JBergmann\tv-browserm\debian\tv-browserm
rd /s /q c:\MADDE\0.6.14\home\berjan\tv-browserm\src\debug
rd /s /q c:\MADDE\0.6.14\home\berjan\tv-browserm\src\release
rd /s /q c:\MADDE\0.6.14\home\berjan\tv-browserm\debian\tv-browserm

del /s /q *.o
del /s /q moc*.*
del /s /q makefile.*
del /s /q object_script*.*

del /s /q c:\MADDE\0.6.14\home\berja\tv-browserm\src\tv-browserm
del /s /q c:\MADDE\0.6.14\home\berja\tv-browserm\src\tv-browserm.exe