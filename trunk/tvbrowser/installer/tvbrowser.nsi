# NSIS script for creating the Windows installer.
#
#
# TV-Browser
# Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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


#--------------------------------
# Include Modern UI
!include "MUI.nsh"


#--------------------------------
# Configuration

# program name
Name "${PROG_NAME} ${VERSION}"

# The file to write
OutFile "${PUBLIC_DIR}\${PROG_NAME_FILE}_${VERSION_FILE}.exe"

# Use LZMA compression
SetCompressor lzma

# The icons of the installer and uninstaller
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\murfman-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\murfman-uninstall.ico"

!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_FINISHPAGE_TITLE_3LINES
!define MUI_UNWELCOMEPAGE_TITLE_3LINES
!define MUI_UNFINISHPAGE_TITLE_3LINES

# Set the default start menu folder
!define MUI_STARTMENUPAGE_DEFAULTFOLDER $7

# Use no descriptions in the components page
!define MUI_COMPONENTSPAGE_NODESC


#--------------------------------
#Variables

Var STARTMENU_FOLDER
Var INI_VALUE


#--------------------------------
#Interface Settings

!define MUI_ABORTWARNING


#--------------------------------
#Pages

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "${RUNTIME_DIR}\LICENSE.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
# UninstPage custom un.UninstallTvDataPage
UninstPage custom un.UninstallSettingsPage
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH


#--------------------------------
# Custom pages (InstallOptions)
#ReserveFile "${NSISDIR}\UninstallTvData.ini"
ReserveFile "${NSISDIR}\UninstallSettings.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS


#--------------------------------
# Installer Functions

Function .onInit
  push $0
  # Get Account Type of the current user
  UserInfo::GetAccountType
  pop $1
  StrCmp $1 "Admin" isadmin isnotadmin
  isnotadmin:
  StrCmp $1 "Power" isadmin isnotpower
  isadmin:
  StrCpy $8 "HKLM"
  # Get installation folder from registry if available
  ReadRegStr $0 HKLM "Software\${PROG_NAME}" "Install directory"
  # Get the default start menu folder from registry if available
  ReadRegStr $7 HKLM "Software\${PROG_NAME}" "Start Menu Folder"
  goto goon
  isnotpower:
  StrCpy $8 "HKCU"
  # Get installation folder from registry if available
  ReadRegStr $0 HKCU "Software\${PROG_NAME}" "Install directory"
  # Get the default start menu folder from registry if available
  ReadRegStr $7 HKCU "Software\${PROG_NAME}" "Start Menu Folder"
  goon:
  IfErrors errors
  StrCpy $INSTDIR "$0"
  goto end
  errors:
  # The default installation directory
  StrCpy $INSTDIR "$PROGRAMFILES\${PROG_NAME}"
  # The default start menu folder
  StrCpy $7 "${PROG_NAME}"
  end:
  pop $0
FunctionEnd

Function un.onInit
  # Extract InstallOptions INI files
  # !insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "${NSISDIR}\UninstallTvData.ini"   "UninstallTvData.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "${NSISDIR}\UninstallSettings.ini" "UninstallSettings.ini"
FunctionEnd

# Function un.UninstallTvDataPage
#  !insertmacro MUI_HEADER_TEXT "TV-Daten l�schen" \
#    "Bestimmen Sie, ob bereits heruntergeladene TV-Daten gel�scht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallTvData.ini"
# FunctionEnd

Function un.UninstallSettingsPage
  !insertmacro MUI_HEADER_TEXT "Einstellungen l�schen" \
    "Bestimmen Sie, ob Ihre Einstellungen gel�scht werden sollen"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallSettings.ini"
FunctionEnd


#--------------------------------
#Languages

!insertmacro MUI_LANGUAGE "German"


#--------------------------------
# The installation types

InstType "Normal (mit allen Plugins)"
InstType "Minimal (ohne Plugins)"


#--------------------------------
#Installer Sections

Section "${PROG_NAME} (erforderlich)"
  # make the section requiered
  SectionIn 1 2 RO

  # Set output path to the installation directory.
  SetOutPath "$INSTDIR"
  File "${RUNTIME_DIR}\LICENSE.txt"
  File "${RUNTIME_DIR}\tvbrowser.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.txt"
  File "${RUNTIME_DIR}\website.url"
  File "${RUNTIME_DIR}\tvbrowser.jar"
  File "${RUNTIME_DIR}\windows.properties"
  File "${RUNTIME_DIR}\default.properties"
  File "${RUNTIME_DIR}\..\..\win\DesktopIndicator.dll"

  WriteUninstaller "Uninstall.exe"

  SetOutPath "$INSTDIR\imgs"
  File "${RUNTIME_DIR}\imgs\*.*"

  SetOutPath "$INSTDIR\themepacks"
  File "${RUNTIME_DIR}\themepacks\*.*"

  # CreateDirectory "$INSTDIR\tvdata"
  CreateDirectory "$INSTDIR\plugins"


  # Register uninstaller at Windows (Add/Remove programs)
  !define UPDATE_INFO_URL "http://tvbrowser.sourceforge.net"
  !define REGISTER_ICON "$INSTDIR\tvbrowser.exe,0"

  StrCmp $8 "HKCU" user admin
  user:
    SetShellVarContext current
    # Store installation folder in registry
    WriteRegStr HKCU "Software\${PROG_NAME}" "Install directory" $INSTDIR
    # Remember the selected start menu folder in registry
    WriteRegStr HKCU "Software\${PROG_NAME}" "Start Menu Folder" $STARTMENU_FOLDER
    WriteRegExpandStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "UninstallString" \
      "$INSTDIR\Uninstall.exe"
    WriteRegExpandStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "InstallLocation" \
      "$INSTDIR"
    WriteRegStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "DisplayName" \
      "${PROG_NAME} ${VERSION}"
    WriteRegStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "DisplayIcon" \
      ${REGISTER_ICON}
    WriteRegStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "DisplayVersion" \
      "${VERSION}"
    WriteRegStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "URLUpdateInfo" \
      ${UPDATE_INFO_URL}
    goto end
    
  admin:
    # Sets the context of $SMPROGRAMS and other shell folders. If set to 'all', the 'all users' shell folder is used.
    SetShellVarContext all
    # Store installation folder in registry
    WriteRegStr HKLM "Software\${PROG_NAME}" "Install directory" $INSTDIR
    # Remember the selected start menu folder in registry
    WriteRegStr HKLM "Software\${PROG_NAME}" "Start Menu Folder" $STARTMENU_FOLDER

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
    WriteRegStr \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "DisplayIcon" \
      ${REGISTER_ICON}
    WriteRegStr \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "DisplayVersion" \
      "${VERSION}"
    WriteRegStr \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "URLUpdateInfo" \
      ${UPDATE_INFO_URL}
  end:
  # Create start menu entry if wanted by the user
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application

    # Set the directory where the shortcuts should be executed in
    SetOutPath "$INSTDIR"

    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\${PROG_NAME}.lnk" \
      "$INSTDIR\tvbrowser.exe"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\Lizenz.lnk" \
      "$INSTDIR\LICENSE.txt"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\Website.lnk" \
      ${UPDATE_INFO_URL}

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\${PROG_NAME} deinstallieren.lnk" \
      "$INSTDIR\Uninstall.exe" \
      "" \
      "$INSTDIR\Uninstall.exe" \
      0

  !insertmacro MUI_STARTMENU_WRITE_END

SectionEnd # end the section


Section "Verkn�pfung auf dem Desktop"
  SectionIn 1 2
  StrCmp $8 "HKCU" user admin
  user:
    SetShellVarContext current
    goto goon
  admin:
    SetShellVarContext all
  goon:

  # Set the directory where the shortcuts should be executed in
  SetOutPath "$INSTDIR"

  CreateShortCut \
    "$DESKTOP\${PROG_NAME}.lnk" \
    "$INSTDIR\tvbrowser.exe"
SectionEnd


SubSection "Daten-Services"

  Section "TV-Browser-Datenservice"
    SectionIn 1 2 RO

    SetOutPath "$INSTDIR\tvdataservice"
    File "${RUNTIME_DIR}\tvdataservice\TvBrowserDataService.jar"
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

  Section "Drucken"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\PrintPlugin.jar"
  SectionEnd

  Section "Lieblingssendungen verwalten"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\FavoritesPlugin.jar"
  SectionEnd

  Section "Showviewnummern berechnen"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ShowviewPlugin.jar"
  SectionEnd


  Section "Web Plugin"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\WebPlugin.jar"
  SectionEnd

  Section "TV-Bewertungen"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\TVRaterPlugin.jar"
  SectionEnd

  Section "Was l�uft gerade"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ListViewPlugin.jar"
  SectionEnd

  Section "Nachrichten"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\NewsPlugin.jar"
  SectionEnd

  Section "Capture-Plugin"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\CapturePlugin.jar"
  SectionEnd

  Section "Zwischenablage"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ClipboardPlugin.jar"
  SectionEnd

SubSectionEnd


# special uninstall section.
Section "Uninstall"
  # Read whether "Remove TV data" was seleted in the "UninstallTvData.ini"
  #!insertmacro MUI_INSTALLOPTIONS_READ $INI_VALUE "UninstallTvData.ini" "Field 2" "State"

  # Remove TV data if "Remove TV data" was seleted in the "UninstallTvData.ini"
 # StrCmp $INI_VALUE "1" "" +2
 #   RMDir /r "$INSTDIR\tvdata"

  # Read whether "Remove settings" was seleted in the "UninstallSettings.ini"
  !insertmacro MUI_INSTALLOPTIONS_READ $INI_VALUE "UninstallSettings.ini" "Field 2" "State"

  # Remove settings if "Remove settings" was seleted in the "UninstallSettings.ini"
 # StrCmp $INI_VALUE "1" "" +2
 #   RMDir /r "$PROFILE\TV-Browser"


  RMDir /r "$INSTDIR\imgs"
  RMDir /r "$INSTDIR\plugins"
  RMDir /r "$INSTDIR\themepacks"
  RMDir /r "$INSTDIR\tvdataservice"
  Delete "$INSTDIR\*.*"
  RMDir "$INSTDIR"


  # Unregister uninstaller at Windows (Add/Remove programs)
  push $8
  UserInfo::GetAccountType
  pop $1
  StrCmp $1 "Admin" isadmin isnotadmin
  isnotadmin:
  StrCmp $1 "Power" isadmin isnotpower
  isadmin:
    ReadRegStr $8 HKLM "Software\${PROG_NAME}" "Start Menu Folder"
    DeleteRegKey \
    HKLM \
    "Software\${PROG_NAME}"
    DeleteRegKey \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}"
    SetShellVarContext all
    goto end
  isnotpower:
    ReadRegStr $8 HKCU "Software\${PROG_NAME}" "Start Menu Folder"
    DeleteRegKey \
    HKCU \
    "Software\${PROG_NAME}"
    DeleteRegKey \
    HKCU \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}"
    SetShellVarContext current
  end:
    # Remove start menu shortcuts
  RMDir /r "$SMPROGRAMS\$8"
  pop $8

  # remove desktop shortcut
  Delete "$DESKTOP\${PROG_NAME}.lnk"

SectionEnd

#eof
