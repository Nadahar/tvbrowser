#!/bin/bash

# $Id$

# Checking JAVA_HOME
if [ "$JAVA_HOME" = "" ] ; then
  # Trying to figure out the Java home directory
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BINDIR=`dirname $JAVA`
  JAVA_HOME=$JAVA_BINDIR/..
fi

# The library directory. This directory must contain a "lib" subdirectory.
LIB_DIR=$PWD

# Set the CLASSPATH (Unix style)
unset CLASSPATH
CLASSPATH=${LIB_DIR}/lib/ant.jar
CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar

# If we are not using Unix but Cygwin on Windows, we need to convert the
# the Java VM can understand the CLASSPATH.
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ]; then
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

echo Using CLASSPATH: $CLASSPATH

# Run application
$JAVA_HOME/bin/java -cp $CLASSPATH org.apache.tools.ant.Main "$@" -buildfile build.xml

