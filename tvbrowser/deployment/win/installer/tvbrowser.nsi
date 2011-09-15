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


# The following Variables are set from the ANT build script:
#   VERSION,
#   VERSION_FILE
#   PROG_NAME
#   PROG_NAME_FILE
#   RUNTIME_DIR
#   INSTALLER_DIR
#   PUBLIC_DIR


#--------------------------------
# Includes
#--------------------------------
!AddIncludeDir "${INSTALLER_DIR}"
!include MUI.nsh
!include LogicLib.nsh

#--------------------------------
# Configuration
#--------------------------------

# program name
Name "${PROG_NAME} ${VERSION}"

# The file to write
OutFile "${PUBLIC_DIR}\${PROG_NAME_FILE}_${VERSION}_win32.exe"

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

#Function un.UninstallSettingsPage
#  !insertmacro MUI_HEADER_TEXT "Einstellungen l�schen" "Bestimmen Sie, ob Ihre Einstellungen gel�scht werden sollen"
#  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "UninstallSettings.ini"
#FunctionEnd

!define MIN_JAVA_VERSION_STRING "1.6"
!define MIN_JAVA_VERSION 16

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
        StrCpy $JAVA_INSTALLATION_MSG "Java Runtime Environment is not installed on your computer. You need version ${MIN_JAVA_VERSION_STRING} or newer to run this program."
        Goto Done

    CheckJavaVer:
        ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$JAVA_VER" JavaHome
        GetFullPathName /SHORT $JAVA_HOME "$0"
        StrCpy $0 $JAVA_VER 1 0
        StrCpy $1 $JAVA_VER 1 2
        StrCpy $JAVA_VER "$0$1"
        IntCmp ${MIN_JAVA_VERSION} $JAVA_VER FoundCorrectJavaVer FoundCorrectJavaVer JavaVerNotCorrect

    FoundCorrectJavaVer:
        IfFileExists "$JAVA_HOME\bin\javaw.exe" 0 JavaNotPresent
        ;MessageBox MB_OK "Found Java: $JAVA_VER at $JAVA_HOME"
        Goto Done

    JavaVerNotCorrect:
        StrCpy $JAVA_INSTALLATION_MSG "The version of Java Runtime Environment installed on your computer is $JAVA_VER. Version ${MIN_JAVA_VERSION_STRING} or newer is required to run this program."

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
  File "${RUNTIME_DIR}\tvbrowser.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.exe"
  File "${RUNTIME_DIR}\tvbrowser_noDD.txt"
  File "${RUNTIME_DIR}\tvbrowser.jar"
  File "${RUNTIME_DIR}\windows.properties"
  File "${RUNTIME_DIR}\jRegistryKey.dll"
  File "${RUNTIME_DIR}\jcom.dll"

  WriteUninstaller "Uninstall.exe"

  SetOutPath "$INSTDIR\hyphen"
  File "${RUNTIME_DIR}\hyphen\*.*"

  SetOutPath "$INSTDIR\imgs"
  File "${RUNTIME_DIR}\imgs\*.*"

  SetOutPath "$INSTDIR\personas"
  File /r "${RUNTIME_DIR}\personas\*.*"

  SetOutPath "$INSTDIR\icons"
  File /r "${RUNTIME_DIR}\icons\*.*"

  SetOutPath "$INSTDIR\themepacks"
  File "${RUNTIME_DIR}\themepacks\*.*"

  # Register uninstaller at Windows (Add/Remove programs)
  !define UPDATE_INFO_URL "http://tvbrowser.sourceforge.net"
  !define SUPPORT_URL "http://tvbrowser.org/forum.html"
  !define ABOUT_URL "http://tvbrowser.org"
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
      "Publisher" \
      "TV-Browser Team"
    ; get update infos directly on sourceforge
    WriteRegStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "URLUpdateInfo" \
      ${UPDATE_INFO_URL}
    ; link about to homepage
    WriteRegStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "URLInfoAbout" \
      ${ABOUT_URL}
    ; support via forum
    WriteRegStr \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "HelpLink" \
      ${SUPPORT_URL}
    ; no modify option
    WriteRegDWORD \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "NoModify" \
      1
    ; no repair option
    WriteRegDWORD \
      HKCU \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "NoRepair" \
      1
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
      "Publisher" \
      "TV-Browser Team"
    WriteRegStr \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "URLUpdateInfo" \
      ${UPDATE_INFO_URL}
    ; link about to homepage
    WriteRegStr \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "URLInfoAbout" \
      ${ABOUT_URL}
    ; support via forum
    WriteRegStr \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "HelpLink" \
      ${SUPPORT_URL}
    ; no modify option
    WriteRegDWORD \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "NoModify" \
      1
    ; no repair option
    WriteRegDWORD \
      HKLM \
      "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
      "NoRepair" \
      1
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
  !insertmacro removeFirewall "$INSTDIR\tvbrowser.exe" "${PROG_NAME}"
  !insertmacro removeFirewall "$INSTDIR\tvbrowser_noDD.exe" "$(WITHOUT_DIRECTX)"
  # no removeFirewall for Java because other applications may need that

  ; remove known files in installation directory
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
  Delete "$INSTDIR\uninstall.exe"
  Delete "$INSTDIR\website"
  Delete "$INSTDIR\wiki"
  Delete "$INSTDIR\windows.properties"

  ; remove sub directories recursively (with ALL files in those directories, so also downloaded skins and plugins are deleted)
  IfFileExists "$INSTDIR\hyphen\dehyphx.tex" deleteHyphenDir noDeleteHyphenDir
  deleteHyphenDir:
  RMDir /r "$INSTDIR\hyphen"
  noDeleteHyphenDir:

  IfFileExists "$INSTDIR\imgs\tvbrowser128.png" deleteImageDir noDeleteImageDir
  deleteImageDir:
  RMDir /r "$INSTDIR\imgs"
  noDeleteImageDir:

  IfFileExists "$INSTDIR\icons\Tango.zip" deleteIconsDir noDeleteIconsDir
  deleteIconsDir:
  RMDir /r "$INSTDIR\icons"
  noDeleteIconsDir:

  IfFileExists "$INSTDIR\plugins\TvBrowserDataService.jar" deletePluginsDir noDeletePluginsDir
  deletePluginsDir:
  RMDir /r "$INSTDIR\plugins"
  noDeletePluginsDir:
  
  RMDir /r "$INSTDIR\personas"

  IfFileExists "$INSTDIR\themepacks\themepack.zip" deleteThemesDir noDeleteThemesDir
  deleteThemesDir:
  RMDir /r "$INSTDIR\themepacks"
  noDeleteThemesDir:

  ; finally remove the installation dir (without recursion, so it is not deleted, if any files are still inside)
  RMDir "$INSTDIR"

  ClearErrors
  ReadEnvStr $1 "APPDATA"
  IfErrors noDelete
  IfFileExists "$1\TV-Browser" askDelete

  ClearErrors
  ReadEnvStr $1 "WINDIR"
  IfErrors noDelete
  IfFileExists "$1\TV-Browser" askDelete

  ClearErrors
  ReadEnvStr $1 "USERPROFILE"
  IfErrors noDelete
  IfFileExists "$1\TV-Browser" askDelete

  askDelete:
  MessageBox MB_YESNO "$(un.QUESTION_DELETE_CONFIG)$\r$\n($1\TV-Browser)" IDNO noDelete
  MessageBox MB_YESNO "$(un.CONFIRM)" IDNO noDelete
  RMDir /r "$1\TV-Browser"
  noDelete:

  # Unregister uninstaller at Windows (Add/Remove programs)
  push $8
  UserInfo::GetAccountType
  pop $1
  StrCmp $1 "Admin" isadmin isnotadmin
  isnotadmin:
  StrCmp $1 "Power" isadmin isnotpower
  isadmin:
    ReadRegStr $8 HKLM "Software\${PROG_NAME}${VERSION}" "Start Menu Folder"
    IfErrors noDeleteStartMenu
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
    IfErrors noDeleteStartMenu
    DeleteRegKey \
    HKCU \
    "Software\${PROG_NAME}${VERSION}"
    DeleteRegKey \
    HKCU \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}"
    SetShellVarContext current
  end:

  # Remove start menu shortcuts, but only if we successfully read the registry
  ${If} "$8" != ""
    RMDir /r "$SMPROGRAMS\$8"
  ${EndIf}

  noDeleteStartMenu:
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
