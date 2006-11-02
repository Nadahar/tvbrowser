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
OutFile "${PUBLIC_DIR}\${PROG_NAME_FILE}-${VERSION_FILE}.exe"

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
#Var INI_VALUE


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
#UninstPage custom un.UninstallSettingsPage
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH


#--------------------------------
# Custom pages (InstallOptions)
#ReserveFile "${NSISDIR}\UninstallTvData.ini"
#ReserveFile "${NSISDIR}\UninstallSettings.ini"
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
 # !insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "${NSISDIR}\UninstallSettings.ini" "UninstallSettings.ini"
FunctionEnd

# Function un.UninstallTvDataPage
#  !insertmacro MUI_HEADER_TEXT "TV-Daten löschen" \
#    "Bestimmen Sie, ob bereits heruntergeladene TV-Daten gelöscht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallTvData.ini"
# FunctionEnd

#Function un.UninstallSettingsPage
#  !insertmacro MUI_HEADER_TEXT "Einstellungen löschen" \
#    "Bestimmen Sie, ob Ihre Einstellungen gelöscht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallSettings.ini"
#FunctionEnd


#--------------------------------
#Languages

!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"

#--------------------------------
#Language Strings

  ;Description
 LangString INST_TYPE_1 ${LANG_German} "Normal (mit allen Plugins)"
 LangString INST_TYPE_1 ${LANG_ENGLISH} "Normal (with all Plugins)"

 LangString INST_TYPE_2 ${LANG_German} "Minimal (ohne Plugins)"
 LangString INST_TYPE_2 ${LANG_ENGLISH} "Minimal (without Plugins)"

 LangString STD_SECTION_NAME ${LANG_German} "${PROG_NAME} (erforderlich)"
 LangString STD_SECTION_NAME ${LANG_ENGLISH} "${PROG_NAME} (necessary)"

 LangString LINK_SECTION_NAME ${LANG_German} "Verknüpfung auf dem Desktop"
 LangString LINK_SECTION_NAME ${LANG_ENGLISH} "Link on the desktop"

 LangString START_WITH_WINDOWS ${LANG_GERMAN} "TV-Browser mit Windows starten"
 LangString START_WITH_WINDOWS ${LANG_ENGLISH} "Start TV-Browser at Windows startup"

 LangString DATA_SECTION_NAME ${LANG_German} "Daten-Services"
   LangString DATA_TVB_SUBSECTION_NAME ${LANG_German} "TV-Browser-Datenservice"
   LangString DATA_RADIOTIMES_SUBSECTION_NAME ${LANG_German} "Radio-Times-Datenservice"
   LangString DATA_SWEDB_SUBSECTION_NAME ${LANG_German} "SweDB TV-Datenservice"
 LangString DATA_SECTION_NAME ${LANG_ENGLISH} "Data service"
   LangString DATA_TVB_SUBSECTION_NAME ${LANG_ENGLISH} "TV-Browser data service"
   LangString DATA_RADIOTIMES_SUBSECTION_NAME ${LANG_ENGLISH} "Radio Times data service"
   LangString DATA_SWEDB_SUBSECTION_NAME ${LANG_ENGLISH} "SweDB TV data dervice"
 
 LangString MISC_DIR ${LANG_GERMAN} "Sonstiges"
 LangString MISC_DIR ${LANG_ENGLISH} "Misc"

 LangString LICENSE_TXT ${LANG_GERMAN} "Lizenz"
 LangString LICENSE_TXT ${LANG_ENGLISH} "License"
 
 LangString WITHOUT_DIRECTX ${LANG_GERMAN} "${PROG_NAME} (ohne DirectX)"
 LangString WITHOUT_DIRECTX ${LANG_ENGLISH} "${PROG_NAME} (without DirectX)"

 LangString UNINSTALL_TXT ${LANG_GERMAN} "${PROG_NAME} deinstallieren"
 LangString UNINSTALL_TXT ${LANG_ENGLISH} "Uninstall ${PROG_NAME}"
 
 LangString PROGRAM_INFO ${LANG_GERMAN} "Sendungsinfo-Betrachter"
 LangString PROGRAM_INFO ${LANG_ENGLISH} "Programinfo viewer"
 
 LangString REMINDER ${LANG_GERMAN} "Erinnerer"
 LangString REMINDER ${LANG_ENGLISH} "Reminder"

 LangString I18N ${LANG_GERMAN} "Übersetzungstool"
 LangString I18N ${LANG_ENGLISH} "Translation tool"
 
 LangString PRINT ${LANG_GERMAN} "Drucken"
 LangString PRINT ${LANG_ENGLISH} "Print"
 
 LangString FAVORITES ${LANG_GERMAN} "Lieblingssendungen verwalten"
 LangString FAVORITES ${LANG_ENGLISH} "Manage favorite programs"
 
 LangString SHOWVIEW ${LANG_GERMAN} "Showviewnummern berechnen"
 LangString SHOWVIEW ${LANG_ENGLISH} "Calculate Showview numbers"
 
 LangString TVRATER ${LANG_GERMAN} "TV-Bewertungen"
 LangString TVRATER ${LANG_ENGLISH} "TV rates"
 
 LangString LISTVIEW ${LANG_GERMAN} "Was läuft gerade"
 LangString LISTVIEW ${LANG_ENGLISH} "What runs now"
 
 LangString NEWS ${LANG_GERMAN} "Nachrichten"
 LangString NEWS ${LANG_ENGLISH} "News"
 
 LangString CLIPBOARD ${LANG_GERMAN} "Zwischenablage"
 LangString CLIPBOARD ${LANG_ENGLISH} "Clipboard-Plugin"

 LangString BLOGTHIS ${LANG_GERMAN} "Blog dies!"
 LangString BLOGTHIS ${LANG_ENGLISH} "Blog this!"
 
 LangString CALENDAR ${LANG_GERMAN} "Kalender Export"
 LangString CALENDAR ${LANG_ENGLISH} "Calendar export"

 LangString SIMPLEMARKER ${LANG_GERMAN} "Markierungs-Plugin"
 LangString SIMPLEMARKER ${LANG_ENGLISH} "Marker Plugin"
 
 LangString un.QUESTION ${LANG_GERMAN} "Sollen die Konfigurationsdateien und TV-Daten gelöscht werden?"
 LangString un.QUESTION ${LANG_ENGLISH} "Do you want to delete the setting and TV data files?"

 LangString un.CONFIRM ${LANG_GERMAN} "Sind Sie sicher?"
 LangString un.CONFIRM ${LANG_ENGLISH} "Are you sure?"

#--------------------------------
# The installation types

InstType "$(INST_TYPE_1)" #"Normal (mit allen Plugins)"
InstType "$(INST_TYPE_2)" #"Minimal (ohne Plugins)"

#--------------------------------
#Installer Sections

Section "$(STD_SECTION_NAME)"
  # make the section requiered
  SectionIn 1 2 RO

  # Set output path to the installation directory.
  SetOutPath "$INSTDIR"
  File "${RUNTIME_DIR}\LICENSE.txt"
  File "${RUNTIME_DIR}\tvbrowser.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.txt"
  File "${RUNTIME_DIR}\website.url"
  File "${RUNTIME_DIR}\forum.url"
  File "${RUNTIME_DIR}\wiki.url"
  File "${RUNTIME_DIR}\enwiki.url"
  File "${RUNTIME_DIR}\tvbrowser.jar"
  File "${RUNTIME_DIR}\windows.properties"
  File "${RUNTIME_DIR}\DesktopIndicator.dll"
  File "${RUNTIME_DIR}\jRegistryKey.dll"
  File "${RUNTIME_DIR}\jcom.dll"


#  #set up the path to the user data in the windows.properties
#  ReadEnvStr $1 "APPDATA"
#  IfErrors error
#  ReadEnvStr $2 "USERPROFILE"
#  IfErrors error
#
#  #length of the USERPROFILE String to $3
#  StrLen $3 "$2"
#
#  #check if APPDATE has the USERPROFILE as a parent directory
#  StrCpy $6 $1 $3
#  StrCmp $2 $6 weiter error
#  weiter:
#  #if APPDATA is in the USERPROFILE copy the sub-directory string to $4
#  IntOp $3 $3 + 1
#  StrCpy $4 $1 "" "$3"
#  StrCpy $4 "/$4"
#  IfFileExists "$2\TV-Browser" move goon
#  move:
#  Rename "$2\TV-Browser" "$1\TV-Browser"
#  goto goon
#  error:
#  #if some error happened copy an empty string in $4 (fallback mode)
#  StrCpy $4 ""
#  goon:
#  #complete the windows.properties
#  FileOpen $5 "$INSTDIR\windows.properties" "a"
#  FileSeek $5 0 END
#
#  FileWrite $5 "# In this folder TV-Browser stores the settings$\r$\n"
#  FileWrite $5 "userdir=${user.home}$4/TV-Browser$\r$\n$\r$\n"
#
#  FileWrite $5 "# In this folder TV-Browser stores the TV listings$\r$\n"
#  FileWrite $5 "tvdatadir=${user.home}$4/TV-Browser/tvdata$\r$\n$\r$\n"
#
#  FileWrite $5 "# The folder for logging$\r$\n"
#  FileWrite $5 "#logdirectory=${user.home}$4/TV-Browser$\r$\n"
#
#  FileClose $5

  WriteUninstaller "Uninstall.exe"

  SetOutPath "$INSTDIR\imgs"
  File "${RUNTIME_DIR}\imgs\*.*"

  SetOutPath "$INSTDIR\icons\tango"
  File /r "${RUNTIME_DIR}\icons\tango\*.*"

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

    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\${PROG_NAME}.lnk" \
      "$INSTDIR\tvbrowser.exe" "" "$INSTDIR\imgs\desktop.ico"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(WITHOUT_DIRECTX).lnk" \
      "$INSTDIR\tvbrowser_noDD.exe" "" "$INSTDIR\imgs\desktop.ico"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(LICENSE_TXT).lnk" \
      "$INSTDIR\LICENSE.txt"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(WITHOUT_DIRECTX) - Info.lnk" \
      "$INSTDIR\tvbrowser_noDD.txt"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\Website.lnk" \
      "$INSTDIR\website.url"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\Forum.lnk" \
      "$INSTDIR\forum.url"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\Deutsches Handuch.lnk" \
      "$INSTDIR\wiki.url"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\English Manual.lnk" \
      "$INSTDIR\enwiki.url"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(UNINSTALL_TXT).lnk" \
      "$INSTDIR\Uninstall.exe" \
      "" \
      "$INSTDIR\Uninstall.exe" \
      0

  !insertmacro MUI_STARTMENU_WRITE_END

SectionEnd # end the section


Section "$(LINK_SECTION_NAME)"
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
    "$INSTDIR\tvbrowser.exe" "" "$INSTDIR\imgs\desktop.ico"
SectionEnd

SubSection "$(DATA_SECTION_NAME)"

  Section "$(DATA_TVB_SUBSECTION_NAME)"
    SectionIn 1 2 RO

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\TvBrowserDataService.jar"
  SectionEnd
  
  Section "$(DATA_RADIOTIMES_SUBSECTION_NAME)"
    SectionIn 1 2

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\RadioTimesDataService.jar"
  SectionEnd

  Section "$(DATA_SWEDB_SUBSECTION_NAME)"
    SectionIn 1 2

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\SweDBTvDataService.jar"
  SectionEnd

SubSectionEnd


SubSection "Plugins"
 
   Section "$(BLOGTHIS)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\BlogThisPlugin.jar"
  SectionEnd

  Section "$(PRINT)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\PrintPlugin.jar"
  SectionEnd

 
  Section "$(SHOWVIEW)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ShowviewPlugin.jar"
  SectionEnd

  Section "Web Plugin"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\WebPlugin.jar"
  SectionEnd

  Section "E-Mail Plugin"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\EMailPlugin.jar"
  SectionEnd

  Section "$(TVRATER)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\TVRaterPlugin.jar"
  SectionEnd

  Section "$(LISTVIEW)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ListViewPlugin.jar"
  SectionEnd

  Section "$(NEWS)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\NewsPlugin.jar"
  SectionEnd

  Section "Capture-Plugin"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\CapturePlugin.jar"
  SectionEnd

  Section "$(CLIPBOARD)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ClipboardPlugin.jar"
  SectionEnd

  Section "$(CALENDAR)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\CalendarExportPlugin.jar"
  SectionEnd
  
  Section "$(SIMPLEMARKER)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\SimpleMarkerPlugin.jar"
  SectionEnd

  Section "$(I18N)"
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\I18NPlugin.jar"
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
  #!insertmacro MUI_INSTALLOPTIONS_READ $INI_VALUE "UninstallSettings.ini" "Field 2" "State"

  # Remove settings if "Remove settings" was seleted in the "UninstallSettings.ini"
 # StrCmp $INI_VALUE "1" "" +2
 #   RMDir /r "$PROFILE\TV-Browser"

  RMDir /r "$INSTDIR"

  ClearErrors
  ReadEnvStr $1 "WINDIR"
  IfErrors no
  IfFileExists "$1\TV-Browser" noerror
  ClearErrors
  ReadEnvStr $1 "USERPROFILE"
  IfErrors no
  IfFileExists "$1\TV-Browser" noerror no
  noerror:
  MessageBox MB_YESNO $(un.QUESTION) IDNO no
  MessageBox MB_YESNO $(un.CONFIRM) IDNO no
    RMDir /r "$1\TV-Browser"
  no:

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
