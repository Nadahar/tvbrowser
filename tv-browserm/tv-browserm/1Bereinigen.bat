rem attribute entfernen
attrib -r -s -h /s

rem ordner/dateien löschen

del /s /q c:\MADDE\0.6.14\home\berja\tv-browserm\src\debug
del /s /q c:\MADDE\0.6.14\home\berja\tv-browserm\src\release
rmdir c:\MADDE\0.6.14\home\berja\tv-browserm\src\debug
rmdir c:\MADDE\0.6.14\home\berja\tv-browserm\src\release

del /s /q *.o
del /s /q moc*.*
del /s /q makefile.*
del /s /q object_script*.*

del /s /q c:\MADDE\0.6.14\home\berja\tv-browserm\src\tv-browserm
del /s /q c:\MADDE\0.6.14\home\berja\tv-browserm\src\tv-browserm.exe