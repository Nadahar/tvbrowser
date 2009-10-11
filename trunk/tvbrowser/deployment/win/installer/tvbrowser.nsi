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
!AddIncludeDir "${INSTALLER_DIR}"
!include MUI.nsh

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
Page Custom LockedListShow
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
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "English"
!include tvbrowser_english.nsh
!include tvbrowser_german.nsh


#--------------------------------
# reserve files for faster extraction
#ReserveFile "${NSISDIR}\UninstallTvData.ini"
#ReserveFile "${NSISDIR}\UninstallSettings.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS
!insertmacro MUI_RESERVEFILE_LANGDLL


#--------------------------------
# Installer Functions
#--------------------------------

Function CheckMultipleInstance
    System::Call 'kernel32::CreateMutexA(i 0, i 0, t "TV-Browser installation") i .r1 ?e'
    Pop $R0
    StrCmp $R0 0 +3
    MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is already running."
    Abort
FunctionEnd

Function .onInit
    call CheckMultipleInstance 
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

Function LockedListShow
  !insertmacro MUI_HEADER_TEXT "$(LOCKED_LIST_HEADING)" "$(LOCKED_LIST_CAPTION)"
  LockedList::AddModule /NOUNLOAD "\tvbrowser.exe"
  LockedList::AddModule /NOUNLOAD "\tvbrowser_noDD.exe"
  LockedList::AddCaption /NOUNLOAD "TV-Browser*"
  LockedList::Dialog /heading "$(LOCKED_LIST_HEADING)" /caption "$(LOCKED_LIST_CAPTION)" /searching "$(LOCKED_LIST_SEARCHING)" /noprograms "$(LOCKED_LIST_NOPROGRAMS)" /colheadings "$(LOCKED_LIST_APPLICATION)" "$(LOCKED_LIST_PROCESS)" /ignore "$(LOCKED_LIST_IGNORE)"
FunctionEnd

# Function un.UninstallTvDataPage
#  !insertmacro MUI_HEADER_TEXT "TV-Daten löschen" "Bestimmen Sie, ob bereits heruntergeladene TV-Daten gelöscht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallTvData.ini"
# FunctionEnd

#Function un.UninstallSettingsPage
#  !insertmacro MUI_HEADER_TEXT "Einstellungen löschen" "Bestimmen Sie, ob Ihre Einstellungen gelöscht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallSettings.ini"
#FunctionEnd

Var JAVA_HOME
Var JAVA_VER
Var JAVA_INSTALLATION_MSG

Function LocateJVM
    ;Check for Java version and location
    Push $0
    Push $1
    StrCpy $JAVA_VER "0"
 
    ReadRegStr $JAVA_VER HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" CurrentVersion
    StrCmp "" "$JAVA_VER" JavaNotPresent CheckJavaVer
 
    JavaNotPresent:
        StrCpy $JAVA_INSTALLATION_MSG "Java Runtime Environment is not installed on your computer. You need version 1.4 or newer to run this program."
        Goto Done
 
    CheckJavaVer:
        ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$JAVA_VER" JavaHome
        GetFullPathName /SHORT $JAVA_HOME "$0"
        StrCpy $0 $JAVA_VER 1 0
        StrCpy $1 $JAVA_VER 1 2
        StrCpy $JAVA_VER "$0$1"
        IntCmp 16 $JAVA_VER FoundCorrectJavaVer FoundCorrectJavaVer JavaVerNotCorrect
 
    FoundCorrectJavaVer:
        IfFileExists "$JAVA_HOME\bin\javaw.exe" 0 JavaNotPresent
        ;MessageBox MB_OK "Found Java: $JAVA_VER at $JAVA_HOME"
        Goto Done
 
    JavaVerNotCorrect:
        StrCpy $JAVA_INSTALLATION_MSG "The version of Java Runtime Environment installed on your computer is $JAVA_VER. Version 1.6 or newer is required to run this program."
 
    Done:
        Pop $1
        Pop $0
FunctionEnd


!macro registerFirewall fileName displayText
    nsisFirewall::AddAuthorizedApplication "${fileName}" "${displayText}"
    Pop $0
    ${If} $0 == 0
        DetailPrint "${displayText} added to Firewall exception list"
    ${Else}
        DetailPrint "An error happened while adding ${displayText} to Firewall exception list (result=$0)"
    ${EndIf}
!macroend

!macro removeFirewall fileName displayText
    nsisFirewall::RemoveAuthorizedApplication "${fileName}"
    Pop $0
    ${If} $0 == 0
        DetailPrint "${displayText} removed from Firewall exception list"
    ${Else}
        DetailPrint "An error happened while removing ${displayText} from Firewall exception list (result=$0)"
    ${EndIf}
!macroend

!macro createInternetShortcut FILENAME URL #ICONFILE ICONINDEX
WriteINIStr "${FILENAME}.url" "InternetShortcut" "URL" "${URL}"
# WriteINIStr "${FILENAME}.url" "InternetShortcut" "IconFile" "${ICONFILE}"
# WriteINIStr "${FILENAME}.url" "InternetShortcut" "IconIndex" "${ICONINDEX}"
!macroend

#--------------------------------
# The installation types

InstType "$(INSTALLATION_TYPE_NORMAL)" #"Normal"

#--------------------------------
#Installer Sections

Section "$(STD_SECTION_NAME)" SEC_STANDARD
  # make the section required
  SectionIn 1 2 RO

  # Set output path to the installation directory.
  SetOutPath "$INSTDIR"
  File "${RUNTIME_DIR}\COPYRIGHT.txt"
  File "${RUNTIME_DIR}\LICENSE.txt"
  # the license is already copied by the license page: No it's not
  File "${RUNTIME_DIR}\tvbrowser.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.txt"
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

  SetOutPath "$INSTDIR\plugins"
  File "${RUNTIME_DIR}\plugins\*.*"


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
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(LICENSE_TXT).lnk" \
      "$INSTDIR\LICENSE.txt"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(WITHOUT_DIRECTX).lnk" \
      "$INSTDIR\tvbrowser_noDD.exe" "" "$INSTDIR\imgs\desktop.ico"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(WITHOUT_DIRECTX) - Info.lnk" \
      "$INSTDIR\tvbrowser_noDD.txt"

    !insertmacro CreateInternetShortcut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\Website" \
        "http://tvbrowser.sourceforge.net/"
  
    !insertmacro CreateInternetShortcut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\Forum" \
        "http://hilfe.tvbrowser.org/index.php"

    !insertmacro CreateInternetShortcut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\Deutsches Handbuch" \
        "http://wiki.tvbrowser.org/"

    !insertmacro CreateInternetShortcut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\English Manual" \
        "http://enwiki.tvbrowser.org"

    CreateShortCut \
      "$SMPROGRAMS\$STARTMENU_FOLDER\$(MISC_DIR)\$(UNINSTALL_TXT).lnk" \
      "$INSTDIR\Uninstall.exe" \
      "" \
      "$INSTDIR\Uninstall.exe" \
      0

  !insertmacro MUI_STARTMENU_WRITE_END
  
  !insertmacro registerFirewall "$INSTDIR\tvbrowser.exe" "${PROG_NAME}"
  !insertmacro registerFirewall "$INSTDIR\tvbrowser_noDD.exe" "$(WITHOUT_DIRECTX)"
  Call LocateJVM
  ${If} $JAVA_VER > 0
    !insertmacro registerFirewall "$JAVA_HOME\bin\java.exe" "Java"
    !insertmacro registerFirewall "$JAVA_HOME\bin\javaw.exe" "Java"
  ${EndIf}
SectionEnd # main section


Section "$(DESKTOP_SECTION_NAME)" SEC_DESKTOP
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

Section "$(QUICKLAUNCH_SECTION_NAME)" SEC_QUICKLAUNCH
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
    "$QUICKLAUNCH\${PROG_NAME}.lnk" \
    "$INSTDIR\tvbrowser.exe" "" "$INSTDIR\imgs\desktop.ico"
SectionEnd # link section

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

  !insertmacro removeFirewall "$INSTDIR\tvbrowser.exe" "${PROG_NAME}"
  !insertmacro removeFirewall "$INSTDIR\tvbrowser_noDD.exe" "$(WITHOUT_DIRECTX)"
  # no removeFirewall for Java because other applications may need that

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

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_STANDARD} $(DESC_SEC_STANDARD)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_DESKTOP} $(DESC_SEC_DESKTOP)
    !insertmacro MUI_DESCRIPTION_TEXT ${SEC_QUICKLAUNCH} $(DESC_SEC_QUICKLAUNCH)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

#eof
