#!/bin/bash

cd java
javac -classpath poi-2.5-final-20040302.jar;PDSRunner.jar SimplePDS.java ExcelPDS.java
cd ..