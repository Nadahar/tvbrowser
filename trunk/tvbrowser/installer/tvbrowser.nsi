#
# NSIS script for creating the Windows installer.
#
#
# TV-Browser
# Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
#
# Author: Til Schneider, www.murfman.de



# The following Variables are set from the build script:
#   VERSION, VERSION_FILE, PROG_NAME, PROG_NAME_FILE,
#   RUNTIME_DIR, INSTALLER_DIR and PUBLIC_DIR

; !define EXAMPLE "Example"



# Deutsche Sprache einstellen
LoadLanguageFile "${INSTALLER_DIR}\German.nlf"

# Der Programmname
Name "${PROG_NAME} ${VERSION}"

# XP-Style UI
XPStyle on

# Die Lizenzbedingungen
LicenseText "Lizenzbedingungen für ${PROG_NAME} ${VERSION}."
LicenseData "${RUNTIME_DIR}\LICENSE.txt"

# The file to write
OutFile "${PUBLIC_DIR}\${PROG_NAME_FILE}_${VERSION_FILE}.exe"

# The installation type
InstType "Normal (mit allen Plugins)"
InstType "Minimal (ohne Plugins)"

# Show all details
ShowInstDetails show
ShowUninstDetails show

# The default installation directory
InstallDir "$PROGRAMFILES\${PROG_NAME}"

# The text to prompt the user to choose the components
ComponentText "${PROG_NAME} ${VERSION} wird nun auf Ihrem Computer installiert. Bitte wählen Sie, welche Komponenten installiert werden sollen."
# The text to prompt the user to enter a directory
DirText "Wählen Sie ein Verzeichnis."



# Checks whether Java is installed with the right version.
# If Java is not installed or if the version is too low,
# the user is asked whether download and install Java.
#
Function CheckForJava
  # TODO

FunctionEnd



# The stuff to install
Section "${PROG_NAME} (erforderlich)"
  # make the section requiered
  SectionIn 1 2 RO

  Call CheckForJava

  # Set output path to the installation directory.
  SetOutPath "$INSTDIR"
  File "${RUNTIME_DIR}\LICENSE.txt"
  File "${RUNTIME_DIR}\tvbrowser.jar"
  File "${RUNTIME_DIR}\website.url"
  File "${RUNTIME_DIR}\where_is_the_exe.txt"

  WriteUninstaller "Uninstall.exe"

  SetOutPath "$INSTDIR\imgs"
  File "${RUNTIME_DIR}\imgs\*.*"
	

  SetOutPath "$INSTDIR\help\de"
  File "${RUNTIME_DIR}\help\de\*.*"

  SetOutPath "$INSTDIR\help\default"
  File "${RUNTIME_DIR}\help\default\*.*"

  SetOutPath "$INSTDIR\themepacks"
  File "${RUNTIME_DIR}\themepacks\*.*"

  
  CreateDirectory "$INSTDIR\tvdata"
  CreateDirectory "$INSTDIR\plugins"
  


  # Register uninstaller at Windows (Add/Remove programs)
  WriteRegExpandStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "UninstallString" \
    "$INSTDIR\Uninstall.exe"
  WriteRegExpandStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "InstallLocation" \
    "$INSTDIR"
  WriteRegStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "DisplayName" \
    "${PROG_NAME} ${VERSION}"
  ; WriteRegStr \
  ;   HKLM \
  ;   "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
  ;   "DisplayIcon" \
  ;   "$INSTDIR\SomeIcon.exe,0"
  WriteRegStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "DisplayVersion" \
    "${VERSION}"
  WriteRegStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "URLUpdateInfo" \
    "http://tvbrowser.sourceforge.net"

SectionEnd # end the section


SubSection "Verknüpfungen"

Section "Verknüpfungen im Start-Menü"
  SectionIn 1 2

  # Set the directory where the shortcuts should be executed in
  SetOutPath "$INSTDIR"

  CreateDirectory "$SMPROGRAMS\${PROG_NAME}"

  CreateShortCut \
    "$SMPROGRAMS\${PROG_NAME}\${PROG_NAME}.lnk" \
    "$INSTDIR\tvbrowser.jar"

  CreateShortCut \
    "$SMPROGRAMS\${PROG_NAME}\Lizenz.lnk" \
    "$INSTDIR\LICENSE.txt"

	CreateShortCut \
    "$SMPROGRAMS\${PROG_NAME}\Website.lnk" \
    "http://tvbrowser.sourceforge.net"

  CreateShortCut \
    "$SMPROGRAMS\${PROG_NAME}\${PROG_NAME} deinstallieren.lnk" \
    "$INSTDIR\Uninstall.exe" \
    "" \
    "$INSTDIR\Uninstall.exe" \
    0
SectionEnd

Section "Verknüpfung auf dem Desktop"
  SectionIn 1 2

  # Set the directory where the shortcuts should be executed in
  SetOutPath "$INSTDIR"

  CreateShortCut \
    "$DESKTOP\${PROG_NAME}.lnk" \
    "$INSTDIR\tvbrowser.jar"
SectionEnd

SubSectionEnd


SubSection "Daten-Services"

Section "TV-Browser-Datenservice"
  SectionIn 1 2

  SetOutPath "$INSTDIR\tvdataservice"
  File "${RUNTIME_DIR}\tvdataservice\TvBrowserDataService.jar"
SectionEnd

Section "Premiere-Datenservice"
  SectionIn 1 2

  SetOutPath "$INSTDIR\tvdataservice"
  File "${RUNTIME_DIR}\tvdataservice\PremiereDataService.jar"
SectionEnd

Section "WDR-Datenservice"
  SectionIn 1 2

  SetOutPath "$INSTDIR\tvdataservice"
  File "${RUNTIME_DIR}\tvdataservice\WdrDataService.jar"
SectionEnd

SubSectionEnd


SubSection "Plugins"

Section "Sendungsinfo-Betrachter"
  SectionIn 1

  SetOutPath "$INSTDIR\plugins"
  File "${RUNTIME_DIR}\plugins\ProgramInfo.jar"
SectionEnd

Section "Erinnerer"
  SectionIn 1

  SetOutPath "$INSTDIR\plugins"
  File "${RUNTIME_DIR}\plugins\ReminderPlugin.jar"
SectionEnd

Section "Sendungen suchen"
  SectionIn 1

  SetOutPath "$INSTDIR\plugins"
  File "${RUNTIME_DIR}\plugins\SearchPlugin.jar"
SectionEnd

Section "Lieblingssendungen verwalten"
  SectionIn 1

  SetOutPath "$INSTDIR\plugins"
  File "${RUNTIME_DIR}\plugins\FavoritesPlugin.jar"
SectionEnd

Section "Drucken"
  SectionIn 1

  SetOutPath "$INSTDIR\plugins"
  File "${RUNTIME_DIR}\plugins\PrintPlugin.jar"

SectionEnd

SubSectionEnd


# uninstall stuff

UninstallText "${PROG_NAME} ${VERSION} wird nun von Ihrem Computer entfernt."

# special uninstall section.
Section "Uninstall"
  # remove directories used.
  RMDir /r "$SMPROGRAMS\${PROG_NAME}"
  RMDir /r "$INSTDIR"

  # remove desktop shortcut
  Delete "$DESKTOP\${PROG_NAME}.lnk"

  # TODO: Ask whether to uninstall the settings
  # No way to do this, because there is no variable for the
  # directory "c:\Documents and Settings\<user>"

  # Unregister uninstaller at Windows (Add/Remove programs)
  DeleteRegKey \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}"

SectionEnd

#eof
