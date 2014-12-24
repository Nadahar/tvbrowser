#!/bin/bash

# Based on Start-Script provided by Azureus
# changed for Mac OSX higher 10.7
# Rene Mach, Magdeburg | Erich Kuester, Krefeld | november 2014

######## CONFIGURE ########
# locations for Mac OSX
# first look for java program in java runtime envoronment
JAVA_PLUGIN_DIR="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin"
# then look for jre in current java development kit, maybe home directory exists (as symbolic link) which saves time
JAVA_JDK_HOME="/Library/Java/Home/jre/bin"
# last possibility is to look which jdk is in /Library/Java/JavaVirtualMachines/ and use the first found
JAVA_JDK_ROOT="/Library/Java/JavaVirtualMachines"
# use full path to TV-Browser bin dir
#PROGRAM_DIR="/home/username/apps/tvbrowser"
############################

# to show the program flow I prefer the messages in the real place
#MSG0="Loading TV-Browser:"
#MSG1="Starting TV-Browser..."
#MSG2="Java exec found in "
#MSG3="OOPS, your java version is too old"
#MSG4="You need to upgrade to jre 7 or newer from https://www.java.com"
#MSG5="Suitable java version found"
#MSG6="Configuring environment ..."
#MSG7="OOPS, you don't seem to have a valid jre"
#MSG8="OOPS, unable to locate java exec in"
#MSG9="hierarchy"
#MSG10="Java exec not found in path, starting auto-search ..."
#MSG11="Java exec found in path. Verifying..."

looking_for_java()
{
  # search given path for java
  echo "Looking for java in" $1
  if ! [ -f "$1/java" ]; then
     echo "No java version in" "$1"
     return 1
  fi
  echo "java version in" "$1"
  JAVA_HEADER=$("$1/java" -version 2>&1 | head -n 1)
  echo "java header is" $JAVA_HEADER
  JAVA_IMPL=`echo ${JAVA_HEADER} | cut -f1 -d' '`
  echo "java implementation is" ${JAVA_IMPL}
  if [ "$JAVA_IMPL" = "java" ] ; then
    VERSION=`echo ${JAVA_HEADER} | sed "s/java version \"\(.*\)\"/\1/"`
    if echo $VERSION | grep "^1.[0-5]" ; then
      echo "Oops, this java version is too old" "[${JAVA_PLUGIN_DIR}/java = ${VERSION}]"
      echo "You need to upgrade to JRE 7 or newer from https://www.java.com/"
      JAVA_DIR=
      return 1
    else
      echo "Suitable java version found" "[$1/java = ${VERSION}]"
      JAVA_DIR=$1
      echo "Configuring environment ..." $JAVA_DIR
      return 0
    fi
   fi
   # openjdk or similar is normally inlikely on macs
   echo "Wrong java implementation, exiting"
   return 1
}

echo "Starting TV-Browser..."

# locate and test the java executable
JAVA_DIR=
if ! looking_for_java $"$JAVA_PLUGIN_DIR"; then
   if ! looking_for_java $"$JAVA_JDK_HOME"; then
      IFS=$'\n'
      potential_java_dirs=$(find $"$JAVA_JDK_ROOT" -name "*.jdk" | sort)
      echo "potential java dirs" $potential_java_dirs
      for D in ${potential_java_dirs[@]}; do
         # select first valid jdk
         if looking_for_java $D/Contents/Home/bin; then
            break
         fi
      done
      if [ -z "$JAVA_DIR" ]; then
         echo "there is no suitable java environment, will terminate"
         exit 1
      fi
   fi
fi

echo "will use java from folder" "${JAVA_DIR}"
# get the app dir if not already defined
if [ -z "$PROGRAM_DIR" ]; then
   if [ -L $0 ]
      then
         SL= `file $0 | sed -e 's/.*to..\(.*\)./\1/'`
         PROGRAM_DIR=`dirname $SL`
      else
         PROGRAM_DIR=`dirname $0`
    fi
    PROGRAM_DIR=`cd "$PROGRAM_DIR"; pwd`
    echo "changed to program folder" $PROGRAM_DIR
else
    if [ "$(echo ${PROGRAM_DIR}/*.jar)" = "${PROGRAM_DIR}/*.jar" ]; then
	echo "You seem to have set an invalid PROGRAM_DIR, unable to continue!"
	exit 1
    elif ! (echo ${PROGRAM_DIR}/*.jar | grep tvbrowser.jar >/dev/null 2>&1 ); then
	echo "Unable to locate tvbrowser.jar in $PROGRAM_DIR, aborting!"
	exit 1
    fi
fi

cd ${PROGRAM_DIR}
echo "Loading TV-Browser:"
echo "Command line is" ${JAVA_DIR}/java '-Xms16m -Xmx512m -Dapple.laf.useScreenMenuBar=true -Dcom.apple.macos.use-file-dialog-packages=true -Dcom.apple.mrj.application.apple.menu.about.name=TV-Browser -Dcom.apple.smallTabs=true -Djava.library.path=\"${PROGRAM_DIR}\" -jar tvbrowser.jar \"$@\"'

$cmdLine
"${JAVA_DIR}/java" -Xms16m -Xmx512m -Dapple.laf.useScreenMenuBar=true -Dcom.apple.macos.use-file-dialog-packages=true -Dcom.apple.smallTabs=true -Djava.library.path="${PROGRAM_DIR}" -jar tvbrowser.jar "$@"
# ensure disk cache is written to drive
sync
echo "TV-Browser will terminate."
