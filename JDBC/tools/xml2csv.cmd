@echo off

rem Transforms given XML delta file to CSV format
rem Run:
rem   xml2csv <xml file>
rem Needs Xalan and JRE installed (check these paths)

set JAVA_HOME=C:\jdk6
set XALAN_PATH=..\..\tools\xalan-j_2_7_1

rem
rem Resulting CSV file will be placed to <xml file>.csv
rem

set PATH=%PATH%;%JAVA_HOME%\bin
set CLASSPATH=%CLASSPATH%;%XALAN_PATH%\xalan.jar;%XALAN_PATH%\serializer.jar;%XALAN_PATH%\xercesImpl.jar;%XALAN_PATH%\xml-apis.jar

echo Deleting output file...
del /f /q %1.csv
echo File deleted OK. Start transformation...

java org.apache.xalan.xslt.Process -DIAG -IN %1 -XSL xml2csv.xsl -OUT %1.csv
echo Transformation ended OK...
