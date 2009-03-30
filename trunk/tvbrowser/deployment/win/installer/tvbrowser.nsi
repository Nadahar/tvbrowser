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
# Includes
#--------------------------------

!include "MUI.nsh"


#--------------------------------
# Configuration
#--------------------------------

# program name
Name "${PROG_NAME} ${VERSION}"

# The file to write
OutFile "${PUBLIC_DIR}\${PROG_NAME_FILE}-${VERSION}.exe"

# Use LZMA compression
SetCompressor /SOLID lzma

# The icons of the installer and uninstaller
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"

# use titles with linebreaks
!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_FINISHPAGE_TITLE_3LINES
!define MUI_UNWELCOMEPAGE_TITLE_3LINES
!define MUI_UNFINISHPAGE_TITLE_3LINES

# run TV-Browser from last page
!define MUI_FINISHPAGE_RUN "$INSTDIR\tvbrowser.exe"
!define MUI_FINISHPAGE_RUN_NOTCHECKED

# Set the default start menu folder
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "$7"


#--------------------------------
# Variables
#--------------------------------

Var STARTMENU_FOLDER
#Var INI_VALUE


#--------------------------------
#Interface Settings

# message box when cancelling the installation
!define MUI_ABORTWARNING
# use small description box on bottom of component selection page
!define MUI_COMPONENTSPAGE_SMALLDESC
# always show language selection
!define MUI_LANGDLL_ALWAYSSHOW


#--------------------------------
# Installer pages
#--------------------------------

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
# Supported installation languages
#--------------------------------

!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"


#--------------------------------
# reserve files for faster extraction
#ReserveFile "${NSISDIR}\UninstallTvData.ini"
#ReserveFile "${NSISDIR}\UninstallSettings.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS
!insertmacro MUI_RESERVEFILE_LANGDLL


#--------------------------------
# Installer Functions
#--------------------------------

Function .onInit
    # have language selection for the user
    !insertmacro MUI_LANGDLL_DISPLAY
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
  ClearErrors
  ReadRegStr $0 HKLM "Software\${PROG_NAME}${VERSION}" "Install directory"
  IfErrors +1 +2
  ReadRegStr $0 HKLM "Software\TV-Browser" "Install directory"
  # Get the default start menu folder from registry if available
  ClearErrors
  ReadRegStr $7 HKLM "Software\${PROG_NAME}${VERSION}" "Start Menu Folder"
  IfErrors +1 +2
  ReadRegStr $7 HKLM "Software\TV-Browser" "Start Menu Folder"
  goto goon
  isnotpower:
  StrCpy $8 "HKCU"
  # Get installation folder from registry if available
  ClearErrors
  ReadRegStr $0 HKCU "Software\${PROG_NAME}${VERSION}" "Install directory"
  IfErrors +1 +2
  ReadRegStr $0 HKCU "Software\TV-Browser" "Install directory"
  # Get the default start menu folder from registry if available
  ClearErrors
  ReadRegStr $7 HKCU "Software\${PROG_NAME}${VERSION}" "Start Menu Folder"
  IfErrors +1 +2
  ReadRegStr $7 HKCU "Software\TV-Browser" "Start Menu Folder"

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
    !insertmacro MUI_UNGETLANGUAGE 
FunctionEnd

# Function un.UninstallTvDataPage
#  !insertmacro MUI_HEADER_TEXT "TV-Daten löschen" "Bestimmen Sie, ob bereits heruntergeladene TV-Daten gelöscht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallTvData.ini"
# FunctionEnd

#Function un.UninstallSettingsPage
#  !insertmacro MUI_HEADER_TEXT "Einstellungen löschen" "Bestimmen Sie, ob Ihre Einstellungen gelöscht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallSettings.ini"
#FunctionEnd


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
   LangString DATA_SCHEDULESDIRECT_SUBSECTION_NAME ${LANG_German} "SchedulesDirect-Datenservice"
 LangString DATA_SECTION_NAME ${LANG_ENGLISH} "Data service"
   LangString DATA_TVB_SUBSECTION_NAME ${LANG_ENGLISH} "TV-Browser data service"
   LangString DATA_RADIOTIMES_SUBSECTION_NAME ${LANG_ENGLISH} "Radio Times data service"
   LangString DATA_SWEDB_SUBSECTION_NAME ${LANG_ENGLISH} "SweDB TV data service"
   LangString DATA_SCHEDULESDIRECT_SUBSECTION_NAME ${LANG_ENGLISH} "SchedulesDirect data service"
 
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

 LangString BLOGTHIS ${LANG_GERMAN} "Blog dies!"
 LangString BLOGTHIS ${LANG_ENGLISH} "Blog this!"
 
 LangString CAPTURE ${LANG_GERMAN} "Aufnahmesteuerung"
 LangString CAPTURE ${LANG_ENGLISH} "Recording control"

 LangString CALENDAR ${LANG_GERMAN} "Kalender-Export"
 LangString CALENDAR ${LANG_ENGLISH} "Calendar export"

 LangString CLIPBOARD ${LANG_GERMAN} "Zwischenablage"
 LangString CLIPBOARD ${LANG_ENGLISH} "Clipboard Plugin"

 LangString EMAIL ${LANG_GERMAN} "E-Mail"
 LangString EMAIL ${LANG_ENGLISH} "E-Mail Plugin"

 LangString FAVORITES ${LANG_GERMAN} "Lieblingssendungen verwalten"
 LangString FAVORITES ${LANG_ENGLISH} "Manage favorite programs"
 
 LangString GENRES ${LANG_GERMAN} "Genres-Plugin"
 LangString GENRES ${LANG_ENGLISH} "Genres Plugin"
 
 LangString I18N ${LANG_GERMAN} "Übersetzungstool"
 LangString I18N ${LANG_ENGLISH} "Translation tool"
 
 LangString LISTVIEW ${LANG_GERMAN} "Was läuft gerade"
 LangString LISTVIEW ${LANG_ENGLISH} "What runs now"
 
 LangString NEWS ${LANG_GERMAN} "Nachrichten"
 LangString NEWS ${LANG_ENGLISH} "News"
 
 LangString PRINT ${LANG_GERMAN} "Drucken"
 LangString PRINT ${LANG_ENGLISH} "Print"
 
 LangString PROGRAMLIST ${LANG_GERMAN} "Sendungsliste"
 LangString PROGRAMLIST ${LANG_ENGLISH} "Program list"
 
 LangString SHOWVIEW ${LANG_GERMAN} "Showviewnummern berechnen"
 LangString SHOWVIEW ${LANG_ENGLISH} "Calculate Showview numbers"
 
 LangString SIMPLEMARKER ${LANG_GERMAN} "Markierungs-Plugin"
 LangString SIMPLEMARKER ${LANG_ENGLISH} "Marker Plugin"
 
 LangString TVRATER ${LANG_GERMAN} "TV-Bewertungen"
 LangString TVRATER ${LANG_ENGLISH} "TV rating"

 LangString WEB ${LANG_GERMAN} "Internet-Suche"
 LangString WEB ${LANG_ENGLISH} "Web Search"

 LangString un.QUESTION ${LANG_GERMAN} "Sollen die Konfigurationsdateien und TV-Daten gelöscht werden?"
 LangString un.QUESTION ${LANG_ENGLISH} "Do you want to delete the settings and TV data files?"

 LangString un.CONFIRM ${LANG_GERMAN} "Sind Sie sicher?"
 LangString un.CONFIRM ${LANG_ENGLISH} "Are you sure?"

#--------------------------------
# The installation types

InstType "$(INST_TYPE_1)" #"Normal (mit allen Plugins)"
InstType "$(INST_TYPE_2)" #"Minimal (ohne Plugins)"

#--------------------------------
#Installer Sections

Section "$(STD_SECTION_NAME)" SEC_STANDARD
  # make the section requiered
  SectionIn 1 2 RO

  # Set output path to the installation directory.
  SetOutPath "$INSTDIR"
  File "${RUNTIME_DIR}\COPYRIGHT.txt"
  File "${RUNTIME_DIR}\LICENSE.txt"
  # the license is already copied by the license page: No it's not
  File "${RUNTIME_DIR}\tvbrowser.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.txt"
  File "${RUNTIME_DIR}\website.url"
  File "${RUNTIME_DIR}\forum.url"
  File "${RUNTIME_DIR}\wiki.url"
  File "${RUNTIME_DIR}\enwiki.url"
  File "${RUNTIME_DIR}\tvbrowser.jar"
  File "${RUNTIME_DIR}\windows.properties"
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

  SetOutPath "$INSTDIR\icons"
  File /r "${RUNTIME_DIR}\icons\*.*"

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
    WriteRegStr HKCU "Software\TV-Browser" "Install directory" $INSTDIR
    WriteRegStr HKCU "Software\${PROG_NAME}${VERSION}" "Install directory" $INSTDIR
    # Remember the selected start menu folder in registry
    WriteRegStr HKCU "Software\TV-Browser" "Start Menu Folder" $STARTMENU_FOLDER
    WriteRegStr HKCU "Software\${PROG_NAME}${VERSION}" "Start Menu Folder" $STARTMENU_FOLDER
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
    WriteRegStr HKLM "Software\TV-Browser" "Install directory" $INSTDIR
    WriteRegStr HKLM "Software\${PROG_NAME}${VERSION}" "Install directory" $INSTDIR
    # Remember the selected start menu folder in registry
    WriteRegStr HKLM "Software\TV-Browser" "Start Menu Folder" $STARTMENU_FOLDER
    WriteRegStr HKLM "Software\${PROG_NAME}${VERSION}" "Start Menu Folder" $STARTMENU_FOLDER

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
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\Deutsches Handbuch.lnk" \
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
SectionEnd # main section


Section "$(LINK_SECTION_NAME)" SEC_LINK
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
SectionEnd # link section


SubSection "$(DATA_SECTION_NAME)" SEC_DATASERVICES

  Section "$(DATA_TVB_SUBSECTION_NAME)" SEC_SERVICE_TVB
    SectionIn 1 2 RO

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\TvBrowserDataService.jar"
  SectionEnd
  
  Section "$(DATA_RADIOTIMES_SUBSECTION_NAME)" SEC_SERVICE_RADIOTIMES
    SectionIn 1 2

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\RadioTimesDataService.jar"
  SectionEnd

  Section "$(DATA_SWEDB_SUBSECTION_NAME)" SEC_SERVICE_SWEDB
    SectionIn 1 2

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\SweDBTvDataService.jar"
  SectionEnd
  
  Section "$(DATA_SCHEDULESDIRECT_SUBSECTION_NAME)" SEC_SERVICE_SCHEDULESDIRECT
    SectionIn 1 2

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\SchedulesDirectDataService.jar"
  SectionEnd  
SubSectionEnd # data services section


SubSection "Plugins" SEC_PLUGINS
 
  Section "$(BLOGTHIS)" SEC_PLUGIN_BLOGTHIS
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\BlogThisPlugin.jar"
  SectionEnd

  Section "$(PRINT)" SEC_PLUGIN_PRINT
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\PrintPlugin.jar"
  SectionEnd

  Section "$(SHOWVIEW)" SEC_PLUGIN_SHOWVIEW
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ShowviewPlugin.jar"
  SectionEnd

  Section "$(WEB)" SEC_PLUGIN_WEB
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\WebPlugin.jar"
  SectionEnd

  Section "$(EMAIL)" SEC_PLUGIN_EMAIL
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\EMailPlugin.jar"
  SectionEnd

  Section "$(TVRATER)" SEC_PLUGIN_TVRATER
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\TVRaterPlugin.jar"
  SectionEnd

  Section "$(LISTVIEW)" SEC_PLUGIN_LISTVIEW
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ListViewPlugin.jar"
  SectionEnd

  Section "$(NEWS)" SEC_PLUGIN_NEWS
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\NewsPlugin.jar"
  SectionEnd

  Section "$(CAPTURE)" SEC_PLUGIN_CAPTURE
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\CapturePlugin.jar"
  SectionEnd

  Section "$(CLIPBOARD)" SEC_PLUGIN_CLIPBOARD
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ClipboardPlugin.jar"
  SectionEnd

  Section "$(CALENDAR)" SEC_PLUGIN_CALENDAR
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\CalendarExportPlugin.jar"
  SectionEnd
  
  Section "$(SIMPLEMARKER)" SEC_PLUGIN_SIMPLEMARKER
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\SimpleMarkerPlugin.jar"
  SectionEnd

  Section "$(GENRES)" SEC_PLUGIN_GENRES
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\GenrePlugin.jar"
  SectionEnd

  Section "$(PROGRAMLIST)" SEC_PLUGIN_PROGRAM_LIST
    SectionIn 1
    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\ProgramListPlugin.jar"
  SectionEND

  Section "$(I18N)" SEC_PLUGIN_I18N
    SectionIn 1

    SetOutPath "$INSTDIR\plugins"
    File "${RUNTIME_DIR}\plugins\I18NPlugin.jar"
  SectionEnd
SubSectionEnd # plugin section


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

  Delete "$INSTDIR\COPYRIGHT.txt"
  Delete "$INSTDIR\enwiki"
  Delete "$INSTDIR\forum"
  Delete "$INSTDIR\jcom.dll"
  Delete "$INSTDIR\jRegistryKey.dll"
  Delete "$INSTDIR\LICENSE.txt"
  Delete "$INSTDIR\tvbrowser.exe"
  Delete "$INSTDIR\tvbrowser.jar"
  Delete "$INSTDIR\tvbrowser_noDD.exe"
  Delete "$INSTDIR\tvbrowser_noDD.txt"
  Delete "$INSTDIR\website"
  Delete "$INSTDIR\wiki"
  Delete "$INSTDIR\windows.properties"
  
  RMDir /r "$INSTDIR\icons\CrystalClear"
  RMDir /r "$INSTDIR\icons\tango"
  RMDir "$INSTDIR\icons"
  
  IfFileExists "$INSTDIR\imgs\tvbrowser128.png" +1 nodelimgs
  RMDir /r "$INSTDIR\imgs"
  nodelimgs:  

  Delete "$INSTDIR\plugins\BbcBackstageDataService.jar"
  Delete "$INSTDIR\plugins\BlogThisPlugin.jar"
  Delete "$INSTDIR\plugins\CalendarExportPlugin.jar"
  Delete "$INSTDIR\plugins\CapturePlugin.jar"
  Delete "$INSTDIR\plugins\ClipboardPlugin.jar"
  Delete "$INSTDIR\plugins\DreamboxDataService.jar"
  Delete "$INSTDIR\plugins\EMailPlugin.jar"
  Delete "$INSTDIR\plugins\GenrePlugin.jar"
  Delete "$INSTDIR\plugins\I18NPlugin.jar"
  Delete "$INSTDIR\plugins\ListViewPlugin.jar"
  Delete "$INSTDIR\plugins\NewsPlugin.jar"
  Delete "$INSTDIR\plugins\PrintPlugin.jar"
  Delete "$INSTDIR\plugins\ProgramListPlugin.jar"
  Delete "$INSTDIR\plugins\RadioTimesDataService.jar"
  Delete "$INSTDIR\plugins\SchedulesDirectDataService.jar"
  Delete "$INSTDIR\plugins\ShowviewPlugin.jar"
  Delete "$INSTDIR\plugins\SimpleMarkerPlugin.jar"
  Delete "$INSTDIR\plugins\SweDBTvDataService.jar"  
  Delete "$INSTDIR\plugins\TvBrowserDataService.jar"
  Delete "$INSTDIR\plugins\TVRaterPlugin.jar"
  Delete "$INSTDIR\plugins\WebPlugin.jar"
  RMDir "$INSTDIR\plugins"
  
  Delete "$INSTDIR\themepacks\themepack.zip"
  RMDir "$INSTDIR\themepacks"
   
  RMDir "$INSTDIR"

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
    ReadRegStr $8 HKLM "Software\${PROG_NAME}${VERSION}" "Start Menu Folder"
    IfErrors donothing
    DeleteRegKey \
    HKLM \
    "Software\${PROG_NAME}${VERSION}"
    DeleteRegKey \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}"
    SetShellVarContext all
    goto end
  isnotpower:
    ReadRegStr $8 HKCU "Software\${PROG_NAME}${VERSION}" "Start Menu Folder"
    IfErrors donothing
    DeleteRegKey \
    HKCU \
    "Software\${PROG_NAME}${VERSION}"
    DeleteRegKey \
    HKCU \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}"
    SetShellVarContext current
  end:
    # Remove start menu shortcuts
  RMDir /r "$SMPROGRAMS\$8"
  donothing:
  pop $8

  # remove desktop shortcut
  Delete "$DESKTOP\${PROG_NAME}.lnk"
SectionEnd


LangString DESC_SEC_STANDARD ${LANG_ENGLISH} "TV-Browser application and files"
LangString DESC_SEC_STANDARD ${LANG_GERMAN} "TV-Browser-Hauptprogramm und zusätzliche Dateien"

LangString DESC_SEC_LINK ${LANG_ENGLISH} "Create a link on your desktop to run TV-Browser."
LangString DESC_SEC_LINK ${LANG_GERMAN} "Eine Verknüpfung zum TV-Browser auf dem Desktop anlegen."

LangString DESC_SEC_DATASERVICES ${LANG_ENGLISH} "Data services bring you additional channels"
LangString DESC_SEC_DATASERVICES ${LANG_GERMAN} "Datenquellen zum Download der Programm-Daten"

LangString DESC_SEC_SERVICE_TVB ${LANG_ENGLISH} "TV-Browser standard service for program listings"
LangString DESC_SEC_SERVICE_TVB ${LANG_GERMAN} "TV-Browser-Standarddienst für Sendungsdaten"

LangString DESC_SEC_SERVICE_RADIOTIMES ${LANG_ENGLISH} "RadioTimes data service for English channels"
LangString DESC_SEC_SERVICE_RADIOTIMES ${LANG_GERMAN} "RadioTimes-Datenquelle für englische Sender"

LangString DESC_SEC_SERVICE_SWEDB ${LANG_ENGLISH} "DataHydra data service for different XMLTV sources"
LangString DESC_SEC_SERVICE_SWEDB ${LANG_GERMAN} "DataHydra-Datenquelle für verschiedene XMLTV-Quellen"

LangString DESC_SEC_SERVICE_SCHEDULESDIRECT ${LANG_ENGLISH} "SchedulesDirect data service for channels from SchedulesDirect.org"
LangString DESC_SEC_SERVICE_SCHEDULESDIRECT ${LANG_GERMAN} "SchedulesDirect-Datenquelle für Sender von SchedulesDirect.org"

LangString DESC_SEC_PLUGINS ${LANG_ENGLISH} "Plugins can provide additional features."
LangString DESC_SEC_PLUGINS ${LANG_GERMAN} "Mit Plugins können zusätzliche Funktionen bereitgestellt werden."

LangString DESC_SEC_PLUGIN_BLOGTHIS ${LANG_ENGLISH} "Creates a new blog entry."
LangString DESC_SEC_PLUGIN_BLOGTHIS ${LANG_GERMAN} "Erzeugt einen neuen Eintrag in einem Blog."

LangString DESC_SEC_PLUGIN_CALENDAR ${LANG_ENGLISH} "Exports a program to a calendar application or a ical/vcal File."
LangString DESC_SEC_PLUGIN_CALENDAR ${LANG_GERMAN} "Exportiert eine Sendung in eine Kalender-Anwendung."

LangString DESC_SEC_PLUGIN_CAPTURE ${LANG_ENGLISH} "Starts an external program with configurable parameters."
LangString DESC_SEC_PLUGIN_CAPTURE ${LANG_GERMAN} "Startet ein externes Programm mit einstellbaren Parametern."

LangString DESC_SEC_PLUGIN_CLIPBOARD ${LANG_ENGLISH} "Copy programs to the clipboard."
LangString DESC_SEC_PLUGIN_CLIPBOARD ${LANG_GERMAN} "Sendungen in die Zwischenablage kopieren."

LangString DESC_SEC_PLUGIN_EMAIL ${LANG_ENGLISH} "Sends an e-mail with an external e-mail application."
LangString DESC_SEC_PLUGIN_EMAIL ${LANG_GERMAN} "Verschickt eine E-Mail mit Hilfe einer externen E-Mail-Anwendung."

LangString DESC_SEC_PLUGIN_GENRES ${LANG_ENGLISH} "Shows the available programs sorted by genre."
LangString DESC_SEC_PLUGIN_GENRES ${LANG_GERMAN} "Zeigt die Programme nach Genre sortiert an."

LangString DESC_SEC_PLUGIN_I18N ${LANG_ENGLISH} "A tool for translators of TV-Browser."
LangString DESC_SEC_PLUGIN_I18N ${LANG_GERMAN} "Ein Werkzeug für Übersetzer von TV-Browser."

LangString DESC_SEC_PLUGIN_LISTVIEW ${LANG_ENGLISH} "Shows a list of currently running programs."
LangString DESC_SEC_PLUGIN_LISTVIEW ${LANG_GERMAN} "Zeigt eine Liste von momentan laufenden Programmen."

LangString DESC_SEC_PLUGIN_NEWS ${LANG_ENGLISH} "Checks for news about the TV-Browser project."
LangString DESC_SEC_PLUGIN_NEWS ${LANG_GERMAN} "Prüft automatisch nach Neuigkeiten rund um das TV-Browser-Projekt."

LangString DESC_SEC_PLUGIN_PROGRAM_LIST ${LANG_ENGLISH} "This plugin shows filtered programs in a list."
LangString DESC_SEC_PLUGIN_PROGRAM_LIST ${LANG_GERMAN} "Dieses Plugin zeigt gefilterte Sendungen in einer Liste an."

LangString DESC_SEC_PLUGIN_PRINT ${LANG_ENGLISH} "This plugin allows to print the program."
LangString DESC_SEC_PLUGIN_PRINT ${LANG_GERMAN} "Mit diesem Plugin kann die Programmvorschau ausgedruckt werden."

LangString DESC_SEC_PLUGIN_SHOWVIEW ${LANG_ENGLISH} "Tries to calculate the missing Showview numbers after a TV listings update."
LangString DESC_SEC_PLUGIN_SHOWVIEW ${LANG_GERMAN} "Versucht nach dem Aktualisieren der TV-Daten die fehlenden Showviewnummern zu berechnen."

LangString DESC_SEC_PLUGIN_SIMPLEMARKER ${LANG_ENGLISH} "A simple marker plugin."
LangString DESC_SEC_PLUGIN_SIMPLEMARKER ${LANG_GERMAN} "Ein einfaches Markierungs-Plugin."

LangString DESC_SEC_PLUGIN_TVRATER ${LANG_ENGLISH} "Allows to rate programs and view ratings entered by other users."
LangString DESC_SEC_PLUGIN_TVRATER ${LANG_GERMAN} "Ermöglicht die Bewertung von Sendungen und zeigt Bewertungen von anderen Benutzern."

LangString DESC_SEC_PLUGIN_WEB ${LANG_ENGLISH} "Searches the web for a program."
LangString DESC_SEC_PLUGIN_WEB ${LANG_GERMAN} "Sucht im Netz nach einer Sendung."

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_STANDARD} $(DESC_SEC_STANDARD)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGINS} $(DESC_SEC_PLUGINS)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_LINK} $(DESC_SEC_LINK)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_DATASERVICES} $(DESC_SEC_DATASERVICES)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_SERVICE_TVB} $(DESC_SEC_SERVICE_TVB)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_SERVICE_RADIOTIMES} $(DESC_SEC_SERVICE_RADIOTIMES)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_SERVICE_SWEDB} $(DESC_SEC_SERVICE_SWEDB)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_SERVICE_SCHEDULESDIRECT} $(DESC_SEC_SERVICE_SCHEDULESDIRECT)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_BLOGTHIS} $(DESC_SEC_PLUGIN_BLOGTHIS)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_CALENDAR} $(DESC_SEC_PLUGIN_CALENDAR)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_CAPTURE} $(DESC_SEC_PLUGIN_CAPTURE)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_CLIPBOARD} $(DESC_SEC_PLUGIN_CLIPBOARD)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_EMAIL} $(DESC_SEC_PLUGIN_EMAIL)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_GENRES} $(DESC_SEC_PLUGIN_GENRES)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_I18N} $(DESC_SEC_PLUGIN_I18N)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_LISTVIEW} $(DESC_SEC_PLUGIN_LISTVIEW)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_NEWS} $(DESC_SEC_PLUGIN_NEWS)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_PROGRAM_LIST} $(DESC_SEC_PLUGIN_PROGRAM_LIST)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_PRINT} $(DESC_SEC_PLUGIN_PRINT)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_SHOWVIEW} $(DESC_SEC_PLUGIN_SHOWVIEW)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_SIMPLEMARKER} $(DESC_SEC_PLUGIN_SIMPLEMARKER)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_TVRATER} $(DESC_SEC_PLUGIN_TVRATER)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PLUGIN_WEB} $(DESC_SEC_PLUGIN_WEB)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

#eof
